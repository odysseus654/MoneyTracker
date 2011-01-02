/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.io.*;
import java.util.*;
import org.xmlpull.v1.*;

/**
 * @author Erik Anderson
 *
 */
class OfxParser
{
	static private class Pair
	{
		public String key;
		public String value;
		public String lastPlainToken;
		public List<TransferObject.ObjValue> children;
	}

	public static TransferObject parseV1(Reader in) throws XmlPullParserException, IOException
	{
		TransferObject docObj = new TransferObject(null);
		
		// grab the headers
		while(true)
		{
			String thisLine = readLine(in);
			if(thisLine == null)
			{
    	        throw new XmlPullParserException("OfxParser/v1: unexpected end of document reading headers", null, null);
			}

			if(thisLine.length() > 0 && thisLine.charAt(thisLine.length()-1) == '\r')
			{
				thisLine = thisLine.substring(0, thisLine.length()-1);
			}
			if(thisLine.length() == 0) break;
			
			int colnPos = thisLine.indexOf(':');
			if(colnPos < 0)
			{
    	        throw new XmlPullParserException("OfxParser/v1: unexpected header line: \"" + thisLine + "\"", null, null);
			}
			docObj.put(thisLine.substring(0, colnPos).trim(), thisLine.substring(colnPos).trim());
		}

		TransferObject outerObj = parseBodyV1(in);
        if(outerObj == null)
        {
	        throw new XmlPullParserException("OfxParser/v1: received an empty document", null, null);
        }
        docObj.put(outerObj);
		return docObj;
	}
	
	static private String readLine(Reader in) throws IOException
	{
		String out = null;
		while(true)
		{
			int ch = in.read();
			if(ch == -1) return out;
			if(out == null) out = "";
			if(ch == '\n') return out;
			out += (char)ch;
		}
	}

	static private TransferObject parseBodyV1(Reader in) throws IOException, XmlPullParserException
	{
		Pair currentTag = null;
		TransferObject outerObj = null;
		XmlPullParser parser = new SimpleXmlParser();
		parser.setInput(in);
		parser.defineEntityReplacementText("nbsp", " ");
		
		LinkedList<Pair> activeTags = new LinkedList<Pair>();

		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
        	switch(eventType)
        	{
        	case XmlPullParser.START_TAG:
        		if(currentTag != null)
        		{
        			activeTags.addLast(currentTag);
        		}
        		currentTag = new Pair();
        		currentTag.key = parser.getName();
        		break;
        		
        	case XmlPullParser.END_TAG:
        	{
        		String name = parser.getName();
    			TransferObject thisObj = new TransferObject(name);
        		while(currentTag != null && !currentTag.key.equals(name))
        		{
        			if(currentTag.children != null)
        			{
        				currentTag.children.addAll(thisObj.members);
        				thisObj.members = currentTag.children;
        			}
        			thisObj.put(currentTag.key, cleanTail(currentTag.value, currentTag.lastPlainToken));
        			if(activeTags.size() == 0)
        			{
            	        throw new XmlPullParserException("OfxParser/v1: unexpected end tag " + parser.getName(), parser, null);
        			} else {
        				currentTag = activeTags.removeLast();
        			}
        		}
    			if(currentTag != null && currentTag.children != null)
    			{
    				currentTag.children.addAll(thisObj.members);
    				thisObj.members = currentTag.children;
    			}
    			Iterator<TransferObject.ObjValue> objIter = thisObj.members.iterator();
    			while(objIter.hasNext())
    			{
    				TransferObject.ObjValue val = objIter.next();
    				if(!thisObj.memberNames.containsKey(val.name))
    				{
    					thisObj.memberNames.put(val.name, val);
    				}
    			}
    			if(activeTags.size() == 0)
    			{
    				if(outerObj == null)
    				{
    					outerObj = thisObj;
    				} else {
            	        throw new XmlPullParserException("OfxParser/v1: multiple document elements found", parser, null);
    				}
    				currentTag = null;
    			} else {
    				currentTag = activeTags.removeLast();
            		if(currentTag.children == null)
            		{
            			currentTag.children = new LinkedList<TransferObject.ObjValue>();
            		}
            		currentTag.children.add(new TransferObject.ObjValue(thisObj.name, thisObj));
    			}
    			break;
        	}
        	case XmlPullParser.TEXT:
        		if(currentTag != null)
        		{
	        		String val = parser.getText();
	    			if(currentTag.value == null)
	    			{
	    				val = cleanHead(val);
	    				if(val.length() > 0)
	    				{
	    					currentTag.value = val;
	    					currentTag.lastPlainToken = val;
	    				}
	    			} else {
	    				currentTag.value = currentTag.value + val;
	    				if(currentTag.lastPlainToken == null) currentTag.lastPlainToken = "";
	    				currentTag.lastPlainToken += val;
	    			}
        		}
        		break;
        	case XmlPullParser.CDSECT:
        	case XmlPullParser.ENTITY_REF:
        		if(currentTag != null)
        		{
	    			if(currentTag.value == null)
	    			{
	    				currentTag.value = "";
	    			}
	    			currentTag.value = currentTag.value + parser.getText();
	    			currentTag.lastPlainToken = null;
        		}
        		break;
        	}
        	eventType = parser.next();
        }
    	return outerObj;
	}
	
	static public TransferObject parseV2(Reader in) throws IOException, XmlPullParserException
	{
		TransferObject outerObj = null;
		TransferObject documentObj = null;
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(in);
		parser.defineEntityReplacementText("nbsp", " ");
		
		LinkedList<TransferObject> activeTags = new LinkedList<TransferObject>();
		TransferObject currentObj = null;
		String lastKey = null;
		String lastValue = null;
		String lastPlainToken = null;

		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
        	switch(eventType)
        	{
        	case XmlPullParser.START_TAG:
        		if(lastKey != null)
        		{
        			if(currentObj != null)
        			{
        				activeTags.addLast(currentObj);
        			}
        			currentObj = new TransferObject(lastKey);
        		}
        		lastKey = parser.getName();
        		lastValue = null;
        		break;

        	case XmlPullParser.END_TAG:
        		if(currentObj == null)
        		{
        	        throw new XmlPullParserException("OfxParser/v2: parsed an empty document", parser, null);
        		}
        		else if(lastKey != null)
        		{
        			if(lastKey != parser.getName())
        			{
            	        throw new XmlPullParserException("OfxParser/v2: expected \"simple\" end tag " + lastKey + ", got " + parser.getName(), parser, null);
        			}
       				currentObj.put(lastKey, cleanTail(lastValue, lastPlainToken));
       				lastKey = null;
       				lastValue = null;
        		} else {
        			lastValue = null;
        			if(currentObj.name != parser.getName())
        			{
            	        throw new XmlPullParserException("OfxParser/v2: expected end tag " + lastKey + ", got " + parser.getName(), parser, null);
        			}
        			if(activeTags.size() == 0)
        			{
        				if(outerObj != null)
        				{
                	        throw new XmlPullParserException("OfxParser/v2: multiple document elements found", parser, null);
        				} else {
        					outerObj = currentObj;
        				}
        			} else {
        				TransferObject parent = activeTags.removeLast();
        				parent.put(currentObj);
        				currentObj = parent;
        			}
        		}
        		break;

        	case XmlPullParser.TEXT:
	        	{
	        		String val = parser.getText();
	    			if(lastValue == null)
	    			{
	    				val = cleanHead(val);
	    				if(val.length() > 0)
	    				{
	    					lastValue = val;
	    					lastPlainToken = val;
	    				}
	    			} else {
	    				lastValue = lastValue + val;
						lastPlainToken = val;
	    			}
	    			break;
	        	}

        	case XmlPullParser.CDSECT:
        	case XmlPullParser.ENTITY_REF:
    			if(lastValue == null)
    			{
    				lastValue = "";
    			}
    			lastValue = lastValue + parser.getText();
    			lastPlainToken = null;
    			break;

        	case XmlPullParser.PROCESSING_INSTRUCTION:
	        	{
	        		TransferObject piObj = parseV2Header(parser);
	        		if(piObj != null)
	        		{
	        			if(documentObj != null)
	        			{
	            	        throw new XmlPullParserException("OfxParser/v2: too many <?OFX?> tags in this document", parser, null);
	        			} else {
	        				documentObj = piObj;
	        			}
	        		}
	        		break;
	        	}
        	}
        	eventType = parser.nextToken();
        }
        if(documentObj == null || outerObj == null)
        {
	        throw new XmlPullParserException("OfxParser/v2: missing either or received only a <?OFX?> tag", parser, null);
        }
        documentObj.put(outerObj);
    	return documentObj;
	}
	
	private static TransferObject parseV2Header(XmlPullParser parser) throws XmlPullParserException
	{
		String text = parser.getText();
		int pos = text.indexOf(' ');
		if(pos < 0) return null;
		String key = text.substring(0, pos);
		if(!key.equals("OFX")) return null;
		
		// *grumble* *grumble* have to parse our own attributes?
		TransferObject out = new TransferObject(null);
		int len = text.length();
        while (pos < len)
        {
        	while(pos < len && Character.isWhitespace(text.charAt(pos))) pos++;
        	if(pos >= len) break;

            String attrName = "";
            while(pos < len && isIdentiferChar(text.charAt(pos))) attrName += text.charAt(pos++);
        	while(pos < len && Character.isWhitespace(text.charAt(pos))) pos++;
	        if(pos >= len)
	        {
	        	throw new XmlPullParserException("OfxParseHeader/v2: unexpected end of PI after seeing attribute name", parser, null);
	        }

            if(text.charAt(pos++) != '=')
            {
	        	throw new XmlPullParserException("OfxParseHeader/v2: unexpected \"" + text.charAt(pos-1) + "\" in PI after seeing attribute name", parser, null);
            }

        	while(pos < len && Character.isWhitespace(text.charAt(pos))) pos++;
            if(pos >= len) return null;

            char delimiter = text.charAt(pos++);
            if(delimiter != '\'' && delimiter != '"')
            {
	        	throw new XmlPullParserException("OfxParseHeader/v2: attribute in PI missing value delimiter", parser, null);
            }
            
            String attrValue = "";
        	while(pos < len)
        	{
        		char ch = text.charAt(pos++);
        		if(ch == delimiter) break;
        		
        		if(ch == '\\')
        		{
        			if(++pos >= len)
        			{
        	        	throw new XmlPullParserException("OfxParseHeader/v2: unexpected end of PI reading attribute value", parser, null);
        			}
            		ch = text.charAt(pos);
        		}
        		
        		attrValue += ch;
            }
        	out.put(attrName, attrValue);
        }

		return out;
	}

	private static String cleanHead(String val)
	{
		if(val != null)
		{
			while(val.length() > 0 && Character.isWhitespace(val.charAt(0))) val = val.substring(1);
		}
		return val;
	}

	private static String cleanTail(String lastValue, String lastPlainToken)
	{
		if(lastPlainToken != null)
		{
			while(lastPlainToken.length() > 0 && Character.isWhitespace(lastPlainToken.charAt(lastPlainToken.length()-1)))
			{
				lastPlainToken = lastPlainToken.substring(0, lastPlainToken.length()-1);
				lastValue = lastValue.substring(0, lastValue.length()-1);
			}
		}
		return lastValue;
	}
	
	private static boolean isIdentiferChar(char ch)
	{
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.';
	}

}
