/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.*;
import org.apache.http.params.*;

/**
 * @author Erik
 *
 */
public abstract class ForeignDefList
{
	static HttpClient client = buildClient();
	public Date lastModified;
//	public String etag;
	
	static private HttpClient buildClient()
	{
		// build the schema (we should only be using https here anyhow)
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
/*
		SSLSocketFactory sockFact = SSLSocketFactory.getSocketFactory();
		sockFact.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sockFact, 443));
*/
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
	
	protected Reader retrievePage(String endpoint) throws ClientProtocolException, IOException
	{
		HttpGet get = new HttpGet(endpoint);
		if(this.lastModified != null) get.addHeader("If-Modified-Since", DateUtils.formatDate(this.lastModified));
//		if(this.etag != null) get.addHeader("If-None-Match", this.etag);
		HttpResponse resp = client.execute(get);
        StatusLine status = resp.getStatusLine();
        int statusCode = status.getStatusCode();
        if(statusCode == 304)
        {
        	return null;
        }
		String contentType = getSingleHeader(resp, "Content-Type");
		String encoding = null;
		if(contentType != null) encoding = getEncoding(contentType);
        InputStream entity = resp.getEntity().getContent();
        Reader reader = null;
		if(encoding == null)
		{
			reader = new BufferedReader(new InputStreamReader(entity));
		} else {
			reader = new BufferedReader(new InputStreamReader(entity, encoding));
		}
        if(statusCode == 200)
        {
			String strLastModified = getSingleHeader(resp, "Last-Modified");
			if(strLastModified != null)
			{
				try {
					this.lastModified = DateUtils.parseDate(strLastModified);
				}
				catch(DateParseException e)
				{
					this.lastModified = null;
				}
			}
//			this.etag = getSingleHeader(resp, "ETag");
        	return reader;
        }
        else
        {
        	String msg = status.getReasonPhrase();
        	if(msg == null || msg.equals(""))
        	{
        		msg = convertStreamToString(reader);
        	}
        	reader.close();
        	throw new HttpResponseException(statusCode, msg);
        }
	}

    private static String convertStreamToString(Reader reader) throws IOException
    {
	    /*
	     * To convert the InputStream to String we use the
	     * Reader.read(char[] buffer) method. We iterate until the
	     * Reader return -1 which means there's no more data to
	     * read. We use the StringWriter class to produce the string.
	     */
    	if (reader == null) return null;
		Writer writer = new StringWriter();

		char[] buffer = new char[1024];
		int n;
		while ((n = reader.read(buffer)) != -1)
		{
			writer.write(buffer, 0, n);
		}
		return writer.toString();
	}

	public abstract Reader retrieveDefList() throws Exception;
    public abstract List<OfxFiDefinition> parseDefList(Reader reader) throws Exception;
    public abstract void sync(List<OfxFiDefinition> defs, OfxFiDefTable db);
	public abstract String name();

	public OfxFiDefinition completeDef(OfxFiDefinition def) throws Exception
	{
		// default is to do nothing
		return def;
	}
}
