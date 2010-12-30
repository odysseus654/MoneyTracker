package name.anderson.odysseus.moneytracker.ofx;

/* Originally using org.kxml2.io.KXmlParser as a template
* Copyright (c) 2002,2003, Stefan Haustein, Oberhausen, Rhld., Germany
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or
* sell copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The  above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
* IN THE SOFTWARE. */

import java.io.*;
import java.util.*;
import org.xmlpull.v1.*;

/** A simple, pull based XML parser. This class replaces the
   XmlParser class and the corresponding event classes. */

class SimpleXmlParser implements XmlPullParser {

   static final private String UNEXPECTED_EOF = "Unexpected EOF";
   static final private String ILLEGAL_TYPE = "Wrong event type";
   static final private int LEGACY = 999;

   // general

   private Map<String,String> entityMap;

   // source

   private Reader reader;
   private char[] srcBuf;

   private int srcPos;
   private int srcCount;

   private int line;
   private int column;

   // txtbuffer

   private char[] txtBuf = new char[128];
   private int txtPos;

   // Event-related

   private int type;
   //private String text;
   private boolean isWhitespace;
   private String name;

//   private boolean degenerated;

   /** 
    * A separate peek buffer seems simpler than managing
    * wrap around in the first level read buffer */

   private int[] peek = new int[2];
   private int peekCount;
   private boolean wasCR;

   private boolean unresolved;
   private boolean token;

   public SimpleXmlParser() {
       srcBuf =
           new char[Runtime.getRuntime().freeMemory() >= 1048576 ? 8192 : 128];
   }

   private final void error(String desc) throws XmlPullParserException {
       exception(desc);
   }

   private final void exception(String desc) throws XmlPullParserException {
       throw new XmlPullParserException(
           desc.length() < 100 ? desc : desc.substring(0, 100) + "\n",
           this,
           null);
   }

   /** 
    * common base for next and nextToken. Clears the state, except from 
    * txtPos and whitespace. Does not set the type variable */

   private final void nextImpl() throws IOException, XmlPullParserException {

       if (reader == null)
           exception("No Input specified");

       while (true) {
			// degenerated needs to be handled before error because of possible
			// processor expectations(!)

           name = null;
           //            text = null;

           type = peekType();

           switch (type) {

               case ENTITY_REF :
                   pushEntity();
                   return;

               case START_TAG :
                   parseStartTag();
                   return;

               case END_TAG :
                   parseEndTag();
                   return;

               case END_DOCUMENT :
                   return;

               case TEXT :
                   pushText('<', !token);
                   return;

               default :
                   type = parseLegacy(token);
                   return;
           }
       }
   }

   private final int parseLegacy(boolean push)
       throws IOException, XmlPullParserException {

       String req = "";
       int term;
       int result;
       int prev = 0;

       read(); // <
       int c = read();

       if (c == '!') {
           if (peek(0) == '-') {
               result = COMMENT;
               req = "--";
               term = '-';
           }
           else if (peek(0) == '[') {
               result = CDSECT;
               req = "[CDATA[";
               term = ']';
               push = true;
           }
           else {
               error("illegal: <!" + c);
               return COMMENT;
           }
       }
       else {
           error("illegal: <" + c);
           return COMMENT;
       }

       for (int i = 0; i < req.length(); i++)
           read(req.charAt(i));

       while (true) {
           c = read();
           if (c == -1){
               error(UNEXPECTED_EOF);
               return COMMENT;
           }

           if (push)
               push(c);

           if ((term == '?' || c == term)
               && peek(0) == term
               && peek(1) == '>')
               break;

           prev = c;
       }

       if (term == '-' && prev == '-')
           error("illegal comment delimiter: --->");

       read();
       read();

       if (push && term != '?')
           txtPos--;

       return result;
   }

   /* precondition: &lt;/ consumed */

   private final void parseEndTag()
       throws IOException, XmlPullParserException {

       read(); // '<'
       read(); // '/'
       name = readName();
       skip();
       read('>');
   }

   private final int peekType() throws IOException {
       switch (peek(0)) {
           case -1 :
               return END_DOCUMENT;
           case '&' :
               return ENTITY_REF;
           case '<' :
               switch (peek(1)) {
                   case '/' :
                       return END_TAG;
                   case '?' :
                   case '!' :
                       return LEGACY;
                   default :
                       return START_TAG;
               }
           default :
               return TEXT;
       }
   }

   private final String get(int pos) {
       return new String(txtBuf, pos, txtPos - pos);
   }

   /*
   private final String pop (int pos) {
   String result = new String (txtBuf, pos, txtPos - pos);
   txtPos = pos;
   return result;
   }
   */

   private final void push(int c) {

       isWhitespace &= c <= ' ';

       if (txtPos == txtBuf.length) {
           char[] bigger = new char[txtPos * 4 / 3 + 4];
           System.arraycopy(txtBuf, 0, bigger, 0, txtPos);
           txtBuf = bigger;
       }

       txtBuf[txtPos++] = (char) c;
   }

   /** Sets name and attributes */

   private final void parseStartTag()
       throws IOException, XmlPullParserException {

       read();
       name = readName();

       skip();

       int c = peek(0);

       if (c == '>') {
           read();
           return;
       }

       if (c == -1) {
           error(UNEXPECTED_EOF);
           //type = COMMENT;
           return;
       }
       
       error("attrs not appropriate here");
   }

   /** result: isWhitespace; if the setName parameter is set,
   the name of the entity is stored in "name" */

   private final void pushEntity()
       throws IOException, XmlPullParserException {

       read(); // &

       int pos = txtPos;

       while (true) {
           int c = read();
           if (c == ';')
               break;
           if (c < 128
               && (c < '0' || c > '9')
               && (c < 'a' || c > 'z')
               && (c < 'A' || c > 'Z')
               && c != '_'
               && c != '-'
               && c != '#') {
               error("unterminated entity ref");
               //; ends with:"+(char)c);           
               if (c != -1)
                   push(c);
               return;
           }

           push(c);
       }

       String code = get(pos);
       txtPos = pos;
       if (token && type == ENTITY_REF)
           name = code;

       if (code.charAt(0) == '#') {
           int c =
               (code.charAt(1) == 'x'
                   ? Integer.parseInt(code.substring(2), 16)
                   : Integer.parseInt(code.substring(1)));
           push(c);
           return;
       }

       String result = (String) entityMap.get(code);

       unresolved = result == null;

       if (unresolved) {
           if (!token)
               error("unresolved: &" + code + ";");
       }
       else {
           for (int i = 0; i < result.length(); i++)
               push(result.charAt(i));
       }
   }

   /** types:
   '<': parse to any token (for nextToken ())
   '"': parse to quote
   ' ': parse to whitespace or '>'
   */

   private final void pushText(int delimiter, boolean resolveEntities)
       throws IOException, XmlPullParserException {

       int next = peek(0);
       int cbrCount = 0;

       while (next != -1 && next != delimiter) { // covers eof, '<', '"'

           if (delimiter == ' ')
               if (next <= ' ' || next == '>')
                   break;

           if (next == '&') {
               if (!resolveEntities)
                   break;

               pushEntity();
           }
           else if (next == '\n' && type == START_TAG) {
               read();
               push(' ');
           }
           else
               push(read());

           if (next == '>' && cbrCount >= 2 && delimiter != ']')
               error("Illegal: ]]>");

           if (next == ']')
               cbrCount++;
           else
               cbrCount = 0;

           next = peek(0);
       }
   }

   private final void read(char c)
       throws IOException, XmlPullParserException {
       int a = read();
       if (a != c)
           error("expected: '" + c + "' actual: '" + ((char) a) + "'");
   }

   private final int read() throws IOException {
       int result;

       if (peekCount == 0)
           result = peek(0);
       else {
           result = peek[0];
           peek[0] = peek[1];
       }
       //		else {
       //			result = peek[0]; 
       //			System.arraycopy (peek, 1, peek, 0, peekCount-1);
       //		}
       peekCount--;

       column++;

       if (result == '\n') {

           line++;
           column = 1;
       }

       return result;
   }

   /** Does never read more than needed */

   private final int peek(int pos) throws IOException {

       while (pos >= peekCount) {

           int nw;

           if (srcBuf.length <= 1)
               nw = reader.read();
           else if (srcPos < srcCount)
               nw = srcBuf[srcPos++];
           else {
               srcCount = reader.read(srcBuf, 0, srcBuf.length);
               if (srcCount <= 0)
                   nw = -1;
               else
                   nw = srcBuf[0];

               srcPos = 1;
           }

           if (nw == '\r') {
               wasCR = true;
               peek[peekCount++] = '\n';
           }
           else {
               if (nw == '\n') {
                   if (!wasCR)
                       peek[peekCount++] = '\n';
               }
               else
                   peek[peekCount++] = nw;

               wasCR = false;
           }
       }

       return peek[pos];
   }

   private final String readName()
       throws IOException, XmlPullParserException {

       int pos = txtPos;
       int c = peek(0);
       if ((c < 'a' || c > 'z')
           && (c < 'A' || c > 'Z')
           && c != '_'
           && c != ':'
           && c < 0x0c0)
           error("name expected");

       do {
           push(read());
           c = peek(0);
       }
       while ((c >= 'a' && c <= 'z')
           || (c >= 'A' && c <= 'Z')
           || (c >= '0' && c <= '9')
           || c == '_'
           || c == '-'
           || c == ':'
           || c == '.'
           || c >= 0x0b7);

       String result = get(pos);
       txtPos = pos;
       return result;
   }

   private final void skip() throws IOException {

       while (true) {
           int c = peek(0);
           if (c > ' ' || c == -1)
               break;
           read();
       }
   }

   //--------------- public part starts here... ---------------

   public void setInput(Reader reader) throws XmlPullParserException {
       this.reader = reader;

       line = 1;
       column = 0;
       type = START_DOCUMENT;
       name = null;

       if (reader == null)
           return;

       srcPos = 0;
       srcCount = 0;
       peekCount = 0;

       entityMap = new HashMap<String,String>();
       entityMap.put("amp", "&");
       entityMap.put("apos", "'");
       entityMap.put("gt", ">");
       entityMap.put("lt", "<");
       entityMap.put("quot", "\"");
   }

   public void setInput(InputStream is, String _enc)
       throws XmlPullParserException {

       srcPos = 0;
       srcCount = 0;
       String enc = _enc;

       if (is == null)
           throw new IllegalArgumentException("kxml: is=null");

       try {

           if (enc == null) {
               // read four bytes 

               int chk = 0;

               while (srcCount < 4) {
                   int i = is.read();
                   if (i == -1)
                       break;
                   chk = (chk << 8) | i;
                   srcBuf[srcCount++] = (char) i;
               }

               if (srcCount == 4) {
                   switch (chk) {
                       case 0x00000FEFF :
                           enc = "UTF-32BE";
                           srcCount = 0;
                           break;

                       case 0x0FFFE0000 :
                           enc = "UTF-32LE";
                           srcCount = 0;
                           break;

                       case 0x03c :
                           enc = "UTF-32BE";
                           srcBuf[0] = '<';
                           srcCount = 1;
                           break;

                       case 0x03c000000 :
                           enc = "UTF-32LE";
                           srcBuf[0] = '<';
                           srcCount = 1;
                           break;

                       case 0x0003c003f :
                           enc = "UTF-16BE";
                           srcBuf[0] = '<';
                           srcBuf[1] = '?';
                           srcCount = 2;
                           break;

                       case 0x03c003f00 :
                           enc = "UTF-16LE";
                           srcBuf[0] = '<';
                           srcBuf[1] = '?';
                           srcCount = 2;
                           break;

                       case 0x03c3f786d :
                           while (true) {
                               int i = is.read();
                               if (i == -1)
                                   break;
                               srcBuf[srcCount++] = (char) i;
                               if (i == '>') {
                                   String s = new String(srcBuf, 0, srcCount);
                                   int i0 = s.indexOf("encoding");
                                   if (i0 != -1) {
                                       while (s.charAt(i0) != '"'
                                           && s.charAt(i0) != '\'')
                                           i0++;
                                       char deli = s.charAt(i0++);
                                       int i1 = s.indexOf(deli, i0);
                                       enc = s.substring(i0, i1);
                                   }
                                   break;
                               }
                           }

                       default :
                           if ((chk & 0x0ffff0000) == 0x0FEFF0000) {
                               enc = "UTF-16BE";
                               srcBuf[0] =
                                   (char) ((srcBuf[2] << 8) | srcBuf[3]);
                               srcCount = 1;
                           }
                           else if ((chk & 0x0ffff0000) == 0x0fffe0000) {
                               enc = "UTF-16LE";
                               srcBuf[0] =
                                   (char) ((srcBuf[3] << 8) | srcBuf[2]);
                               srcCount = 1;
                           }
                           else if ((chk & 0x0ffffff00) == 0x0EFBBBF) {
                               enc = "UTF-8";
                               srcBuf[0] = srcBuf[3];
                               srcCount = 1;
                           }
                   }
               }
           }

           if (enc == null)
               enc = "UTF-8";

           int sc = srcCount;
           setInput(new InputStreamReader(is, enc));
           srcCount = sc;
       }
       catch (Exception e) {
           throw new XmlPullParserException(
               "Invalid stream or encoding: " + e.toString(),
               this,
               e);
       }
   }

   public boolean getFeature(String feature) {
       return false;
   }

   public String getInputEncoding() {
       return null;
   }

   public void defineEntityReplacementText(String entity, String value)
       throws XmlPullParserException {
       if (entityMap == null)
           throw new RuntimeException("entity replacement text must be defined after setInput!");
       entityMap.put(entity, value);
   }

   public Object getProperty(String property) {
		return null;
   }

   public int getNamespaceCount(int depth) {
	   return 0;
   }

   public String getNamespacePrefix(int pos) {
       return null;
   }

   public String getNamespaceUri(int pos) {
       return null;
   }

   public String getNamespace(String prefix) {
       return null;
   }

   public int getDepth() {
       return 0;
   }

   public String getPositionDescription() {

       StringBuffer buf =
           new StringBuffer(type < TYPES.length ? TYPES[type] : "unknown");
       buf.append(' ');

       if (type == START_TAG || type == END_TAG) {
           buf.append('<');
           if (type == END_TAG)
               buf.append('/');

           buf.append(name);

           buf.append('>');
       }
       else if (type == IGNORABLE_WHITESPACE);
       else if (type != TEXT)
           buf.append(getText());
       else if (isWhitespace)
           buf.append("(whitespace)");
       else {
           String text = getText();
           if (text.length() > 16)
               text = text.substring(0, 16) + "...";
           buf.append(text);
       }

		buf.append("@"+line + ":" + column);
		buf.append(" in ");
      	buf.append(reader.toString());
       return buf.toString();
   }

   public int getLineNumber() {
       return line;
   }

   public int getColumnNumber() {
       return column;
   }

   public boolean isWhitespace() throws XmlPullParserException {
       if (type != TEXT && type != IGNORABLE_WHITESPACE && type != CDSECT)
           exception(ILLEGAL_TYPE);
       return isWhitespace;
   }

   public String getText() {
       return type < TEXT
           || (type == ENTITY_REF && unresolved) ? null : get(0);
   }

   public char[] getTextCharacters(int[] poslen) {
       if (type >= TEXT) {
           if (type == ENTITY_REF) {
               poslen[0] = 0;
               poslen[1] = name.length();
               return name.toCharArray();
           }
           poslen[0] = 0;
           poslen[1] = txtPos;
           return txtBuf;
       }

       poslen[0] = -1;
       poslen[1] = -1;
       return null;
   }

   public String getNamespace() {
       return null;
   }

   public String getName() {
       return name;
   }

   public String getPrefix() {
       return null;
   }

   public boolean isEmptyElementTag() throws XmlPullParserException {
       if (type != START_TAG)
           exception(ILLEGAL_TYPE);
       return false;
   }

   public int getAttributeCount() {
       return 0;
   }

   public String getAttributeType(int index) {
       return "CDATA";
   }

   public boolean isAttributeDefault(int index) {
       return false;
   }

   public String getAttributeNamespace(int index) {
       return null;
   }

   public String getAttributeName(int index) {
       return null;
   }

   public String getAttributePrefix(int index) {
       return null;
   }

   public String getAttributeValue(int index) {
       return null;
   }

   public String getAttributeValue(String namespace, String name) {
       return null;
   }

   public int getEventType() throws XmlPullParserException {
       return type;
   }

   public int next() throws XmlPullParserException, IOException {

       txtPos = 0;
       isWhitespace = true;
       int minType = 9999;
       token = false;

       do {
           nextImpl();
           if (type < minType)
               minType = type;
           //	    if (curr <= TEXT) type = curr; 
       }
       while (minType > ENTITY_REF // ignorable
           || (minType >= TEXT && peekType() >= TEXT));

       type = minType;
       if (type > TEXT)
           type = TEXT;

       return type;
   }

   public int nextToken() throws XmlPullParserException, IOException {

       isWhitespace = true;
       txtPos = 0;

       token = true;
       nextImpl();
       return type;
   }

   //----------------------------------------------------------------------
   // utility methods to make XML parsing easier ...

   public int nextTag() throws XmlPullParserException, IOException {

       next();
       if (type == TEXT && isWhitespace)
           next();

       if (type != END_TAG && type != START_TAG)
           exception("unexpected type");

       return type;
   }

   public void require(int type, String namespace, String name)
       throws XmlPullParserException, IOException {

       if (type != this.type
           || (namespace != null && !namespace.equals(getNamespace()))
           || (name != null && !name.equals(getName())))
           exception(
               "expected: " + TYPES[type] + " {" + namespace + "}" + name);
   }

   public String nextText() throws XmlPullParserException, IOException {
       if (type != START_TAG)
           exception("precondition: START_TAG");

       next();

       String result;

       if (type == TEXT) {
           result = getText();
           next();
       }
       else
           result = "";

       if (type != END_TAG)
           exception("END_TAG expected");

       return result;
   }

   public void setFeature(String feature, boolean value)
       throws XmlPullParserException {
       exception("unsupported feature: " + feature);
   }

   public void setProperty(String property, Object value)
       throws XmlPullParserException {
       throw new XmlPullParserException("unsupported property: " + property);
   }

   /**
     * Skip sub tree that is currently porser positioned on.
     * <br>NOTE: parser must be on START_TAG and when funtion returns
     * parser will be positioned on corresponding END_TAG. 
     */

   //	Implementation copied from Alek's mail... 

   public void skipSubTree() throws XmlPullParserException, IOException {
       require(START_TAG, null, null);
       int level = 1;
       while (level > 0) {
           int eventType = next();
           if (eventType == END_TAG) {
               --level;
           }
           else if (eventType == START_TAG) {
               ++level;
           }
       }
   }
}
