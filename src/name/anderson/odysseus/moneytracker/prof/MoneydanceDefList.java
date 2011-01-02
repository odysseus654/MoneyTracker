/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.nio.CharBuffer;
import java.util.*;
import org.apache.http.client.ClientProtocolException;

/**
 * @author Erik
 *
 */
public class MoneydanceDefList extends ForeignDefList
{
	static final String MD_SOURCE = "MoneyDance";
	static final String MD_LIST = "http://moneydance.com/synch/moneydance/fi2004.dict";
	
	public Reader retrieveDefList() throws ClientProtocolException, IOException
	{
		return retrievePage(MD_LIST);
	}
	
	public String name()
	{
		return MD_SOURCE;
	}

    public List<OfxFiDefinition> parseDefList(Reader reader) throws IOException
	{
		int state = 0;
		StringBuffer word = new StringBuffer();
		String name = null;
		Map<String,String> thisDef = new TreeMap<String,String>();
		List<OfxFiDefinition> defList = new LinkedList<OfxFiDefinition>();
		CharBuffer buffer = CharBuffer.allocate(1024);
		while (reader.read(buffer) > -1)
		{
			buffer.flip();
			int pos = 0;
			int wordStart = 0;
			int limit = buffer.limit();
			
			while(pos < limit)
			{
				switch(state)
				{
				case 0:
				case 1:
					// have not reached first / second brace yet
					while(pos < limit && buffer.charAt(pos) != '{') pos++;
					if(pos < limit)
					{
						thisDef.clear();
						state++;
						pos++;
					}
					break;
				case 2:
				case 5:
					// have not reached the first quote
					while(pos < limit && buffer.charAt(pos) != '"' && buffer.charAt(pos) != '}') pos++;
					if(pos < limit)
					{
						if(buffer.charAt(pos) == '}')
						{
							pos++;
							state = 8;
						} else {
							pos++;
							state++;
							word.setLength(0);
							wordStart = pos;
						}
					}
					break;
				case 3:
				case 6:
					// have not reached the second quote
					while(pos < limit && buffer.charAt(pos) != '"') pos++;
					word.append(buffer.subSequence(wordStart, pos));
					if(pos < limit)
					{
						state++;
						pos++;
					}
					break;
				case 4:
					// we have a keyword
					name = word.toString();
					while(pos < limit && buffer.charAt(pos) != '=') pos++;
					if(pos < limit)
					{
						state++;
						pos++;
					}
					break;
				case 7:
					// we have a value
					thisDef.put(name, word.toString());
					state = 2;
					break;
				case 8:
					// we have closed a brace
					OfxFiDefinition def = translateDef(thisDef);
					if(def != null) defList.add(def);
					state = 1;
					break;
				}
			}
			buffer.clear();
		}

		return defList;
	}

	private static OfxFiDefinition translateDef(Map<String, String> thisDef)
	{
		OfxFiDefinition def = new OfxFiDefinition();
		def.name = thisDef.get("fi_name");
		def.fiURL = thisDef.get("bootstrap_url");
		def.fiOrg = thisDef.get("fi_org");
		def.fiID = thisDef.get("fi_id");
		def.appId = thisDef.get("app_id");
		String appVer = thisDef.get("app_ver");
		if(appVer != null) def.appVer = Integer.parseInt(appVer);
		def.srcName = MD_SOURCE;
		def.srcId = thisDef.get("id");
		
		// there is way too many uses of "QWIN/1900" here, let's wipe them out and attempt to autodetect
		if(def.appId != null && def.appId.equals("QWIN") && def.appVer == 1900)
		{
			def.appId = null;
			def.appVer = 0;
		}
		return def;
	}
	
    public void sync(List<OfxFiDefinition> defs, OfxFiDefTable db)
    {
    	db.sync(defs, MD_SOURCE, true);
    }
}
