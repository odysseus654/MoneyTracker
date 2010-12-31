/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.util.*;
import org.apache.http.client.ClientProtocolException;
import org.xmlpull.v1.*;

/**
 * @author Erik
 *
 */
public class OfxHomeDefList extends ForeignDefList
{
	static final String OH_SOURCE = "OFXHOME";
	static final String OH_LIST = "http://www.ofxhome.com/api.php?all=yes";
	
	public Reader retrieveDefList() throws ClientProtocolException, IOException
	{
		return retrieveDefList(OH_LIST);
//		is.close();
	}

    public List<OfxFiDefinition> parseDefList(Reader reader) throws IOException, XmlPullParserException
	{
		XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
		parser.setInput(reader);
		List<OfxFiDefinition> defList = new LinkedList<OfxFiDefinition>();
		int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
        	if(eventType == XmlPullParser.END_TAG)
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
}
