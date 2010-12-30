/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.*;

/**
 * @author Erik
 *
 */
public class MoneydanceDefList
{
	static HttpClient client = buildClient();
	static final String MD_LIST = "http://moneydance.com/synch/moneydance/fi2004.dict";
	public String lastModified;
	public String etag;
	
	static private HttpClient buildClient()
	{
		// build the schema (we should only be using https here anyhow)
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		SSLSocketFactory sockFact = SSLSocketFactory.getSocketFactory();
		sockFact.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sockFact, 443));
		
		// sets up parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(manager, params);
	}
	
	private static String getSingleHeader(HttpResponse resp, String name)
	{
		Header[] headers = resp.getHeaders(name);
		if(headers == null || headers.length == 0) return null;
		return headers[0].getValue();
	}
	
	private static String getEncoding(String contentType)
	{
		int semiPos = contentType.indexOf(';');
		while(semiPos != -1)
		{
			int equalPos = contentType.indexOf('=', semiPos+1);
			String name = contentType.substring(semiPos+1, equalPos).trim();
			if(name.equals("charset"))
			{
				int endPos = contentType.indexOf(';', equalPos+1);
				if(endPos == -1) endPos = contentType.length();
				return contentType.substring(equalPos+1, endPos).trim();
			}
			semiPos = contentType.indexOf(';', equalPos+1);
		}
		return null;
	}
	
	public List<OfxFiDefinition> getDefList() throws ClientProtocolException, IOException
	{
		HttpGet get = new HttpGet(MD_LIST);
		if(this.lastModified != null) get.addHeader("If-Modified-Since", this.lastModified);
		if(this.etag != null) get.addHeader("If-None-Match", this.etag);
		HttpResponse resp = client.execute(get);
		String contentType = getSingleHeader(resp, "Content-Type");
		String encoding = getEncoding(contentType);
		this.lastModified = getSingleHeader(resp, "Last-Modified");
		this.etag = getSingleHeader(resp, "ETag");
        InputStream entity = resp.getEntity().getContent();
		Reader reader = null;
		if(encoding == null)
		{
			reader = new BufferedReader(new InputStreamReader(entity));
		} else {
			reader = new BufferedReader(new InputStreamReader(entity, encoding));
			
		}
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
		def.srcName = "MD";
		def.srcId = thisDef.get("id");
		return def;
	}
}
