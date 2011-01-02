/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.*;

/**
 * @author Erik
 *
 */
public class OfxHomeDefList extends ForeignDefList
{
	static final String OH_SOURCE = "OFXHome";
	static final String OH_LIST = "http://www.ofxhome.com/api.php?all=yes";
	static final String OH_DETAIL = "http://www.ofxhome.com/api.php?lookup=";
	static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	
	public Reader retrieveDefList() throws ClientProtocolException, IOException
	{
		return retrievePage(OH_LIST);
	}

	public String name()
	{
		return OH_SOURCE;
	}

    public List<OfxFiDefinition> parseDefList(Reader reader) throws IOException, XmlPullParserException
	{
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(reader);
		List<OfxFiDefinition> defList = new LinkedList<OfxFiDefinition>();
		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
        	if(eventType == XmlPullParser.START_TAG)
        	{
        		int numAttr = parser.getAttributeCount();
        		if(numAttr > 0)
        		{
        			OfxFiDefinition def = new OfxFiDefinition();
    				def.srcName = OH_SOURCE;
	        		for(int idx = 0; idx < numAttr; idx++)
	        		{
	        			String name = parser.getAttributeName(idx);
	        			String value = parser.getAttributeValue(idx);
	        			if(name.equalsIgnoreCase("name"))
	        			{
	        				def.name = value;
	        			}
	        			else if(name.equalsIgnoreCase("id"))
	        			{
	        				def.srcId = value;
	        			}
	        		}
	        		defList.add(def);
        		}
        	}
        	eventType = parser.next();
        }
		return defList;
	}
    
    public void sync(List<OfxFiDefinition> defs, OfxFiDefTable db)
    {
    	db.sync(defs, OH_SOURCE, false);
    }
    
	public OfxFiDefinition completeDef(OfxFiDefinition def) throws Exception
	{
		Reader reader = retrievePage(OH_DETAIL + def.srcId);

		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(reader);
		StringBuffer word = new StringBuffer();
		int eventType = parser.getEventType();
		boolean ofxFailed = false, sslFailed = false;
		Date ofxVerified = null, sslVerified = null;
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
        	switch(eventType)
        	{
        	case XmlPullParser.START_TAG:
        		word.setLength(0);
        		break;
        	case XmlPullParser.TEXT:
        		word.append(parser.getText());
        		break;
        	case XmlPullParser.END_TAG:
	        	{
	        		String tag = parser.getName();
	        		String value = word.toString();
	        		if(tag.equalsIgnoreCase("name"))
	        		{
	        			def.name = value;
	        		}
	        		else if(tag.equalsIgnoreCase("fid"))
	        		{
	        			def.fiID = value;
	        		}
	        		else if(tag.equalsIgnoreCase("org"))
	        		{
	        			def.fiOrg = value;
	        		}
	        		else if(tag.equalsIgnoreCase("url"))
	        		{
	        			def.fiURL = value;
	        		}
	        		else if(tag.equalsIgnoreCase("ofxfail") && value.equals("1"))
	        		{
	        			ofxFailed = true;
	        		}
	        		else if(tag.equalsIgnoreCase("sslfail") && value.equals("1"))
	        		{
	        			sslFailed = true;
	        		}
	        		else if(tag.equalsIgnoreCase("lastofxvalidation"))
	        		{
	        			ofxVerified = DATE_PARSER.parse(value);
	        		}
	        		else if(tag.equalsIgnoreCase("lastsslvalidation"))
	        		{
	        			sslVerified = DATE_PARSER.parse(value);
	        		}
	        	}
        	}
       		eventType = parser.next();
        }
        
        // deal with the verification results
        Date lastSuccess = null, lastFailure = null;
        if(ofxFailed)
        {
        	lastFailure = ofxVerified;
        } else {
        	lastSuccess = ofxVerified;
        }
        if(sslFailed)
        {
        	if(lastFailure == null || lastFailure.before(sslVerified)) lastFailure = sslVerified;
        } else {
        	if(lastSuccess == null || lastSuccess.before(sslVerified)) lastSuccess = sslVerified;
        }
		return def;
	}
}

// Date lastSuccess = null, lastFailure = null;
