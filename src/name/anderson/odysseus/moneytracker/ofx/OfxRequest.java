/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.io.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.*;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Erik Anderson
 *
 */
public class OfxRequest
{
	public float version;
	public boolean security;
	public UUID fileUid;
	protected List<OfxMessageReq> contents;
	protected OfxProfile profile;
	private SchemeRegistry registry;
	private OfxSSLSocketFactory sockFact;
	
	public OfxRequest(OfxProfile pro)
	{
		this.profile = pro;
		this.version = 0;
		this.security = false;
		this.contents = new LinkedList<OfxMessageReq>();
	}
	
	public void addRequest(OfxMessageReq req)
	{
		this.contents.add(req);
	}
	
	public String Format()
	{
		SortedMap<OfxMessageReq.MessageSet, List<OfxMessageReq> > requestSets
			= new TreeMap<OfxMessageReq.MessageSet, List<OfxMessageReq> >();

		// sort our requests into message sets
		Iterator<OfxMessageReq> iter = this.contents.iterator();
		while(iter.hasNext())
		{
			OfxMessageReq req = iter.next();
			List<OfxMessageReq> reqList;
			if(requestSets.containsKey(req.messageSet))
			{
				reqList = requestSets.get(req.messageSet);
			}
			else
			{
				reqList = new LinkedList<OfxMessageReq>();
				requestSets.put(req.messageSet, reqList);
			}
			reqList.add(req);
		}

		if(!requestSets.containsKey(OfxMessageReq.MessageSet.SIGNON)) return null;
		TransferObject signon = BuildMsgSet(OfxMessageReq.MessageSet.SIGNON, requestSets.get(OfxMessageReq.MessageSet.SIGNON),
				this.profile.getMsgsetVer(this.version, OfxMessageReq.MessageSet.SIGNON));
		requestSets.remove(OfxMessageReq.MessageSet.SIGNON);
		
		TransferObject req = new TransferObject("OFX");
		req.put(signon);

		Iterator<OfxMessageReq.MessageSet> setIter = requestSets.keySet().iterator();
		while(setIter.hasNext())
		{
			OfxMessageReq.MessageSet thisSet = setIter.next();
			req.put(BuildMsgSet(thisSet, requestSets.get(thisSet), this.profile.getMsgsetVer(this.version, thisSet)));
		}

		return FormatHeader(this.version) + req.Format(this.version);
	}

	public Reader submit() throws IOException
	{
		SortedMap<String, TransferObject> requests;
		{
			SortedMap<OfxMessageReq.MessageSet, List<OfxMessageReq> > requestSets
				= new TreeMap<OfxMessageReq.MessageSet, List<OfxMessageReq> >();
	
			// sort our requests into message sets
			Iterator<OfxMessageReq> iter = this.contents.iterator();
			while(iter.hasNext())
			{
				OfxMessageReq req = iter.next();
				List<OfxMessageReq> reqList;
				if(requestSets.containsKey(req.messageSet))
				{
					reqList = requestSets.get(req.messageSet);
				}
				else
				{
					reqList = new LinkedList<OfxMessageReq>();
					requestSets.put(req.messageSet, reqList);
				}
				reqList.add(req);
			}
			
			if(!requestSets.containsKey(OfxMessageReq.MessageSet.SIGNON)) return null;
//			TransferObject signon = BuildMsgSet(OfxMessageReq.MessageSet.Signon, requestSets.get(OfxMessageReq.MessageSet.Signon),
//					this.profile.getMsgsetVer(this.version, OfxMessageReq.MessageSet.Signon));
//			requestSets.remove(OfxMessageReq.MessageSet.Signon);
			
			requests = new TreeMap<String, TransferObject>();
			Iterator<OfxMessageReq.MessageSet> setIter = requestSets.keySet().iterator();
			while(setIter.hasNext())
			{
				OfxMessageReq.MessageSet thisSet = setIter.next();
				String endpoint = this.profile.getEndpoint(thisSet);
				if(endpoint == null) return null;
				float msgsetVer = this.profile.getMsgsetVer(this.version, thisSet);
	
				TransferObject req;
				if(requests.containsKey(endpoint))
				{
					req = requests.get(endpoint);
				} else {
					req = new TransferObject("OFX");
//					req.put(signon);
					requests.put(endpoint, req);
				}
				req.put(BuildMsgSet(thisSet, requestSets.get(thisSet), msgsetVer));
			}
		}

		Iterator<String> reqIter = requests.keySet().iterator();
		while(reqIter.hasNext())
		{
			String endpoint = reqIter.next();
			TransferObject req = requests.get(endpoint);
			return submit(endpoint, req);
		}
		return null;
	}
	
	private HttpClient buildClient()
	{
		// build the schema (we should only be using https here anyhow)
		if(this.registry == null)
		{
			this.registry = new SchemeRegistry();
			//registry(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	//		SSLSocketFactory sockFact = SSLSocketFactory.getSocketFactory();
			try {
				this.sockFact = new OfxSSLSocketFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.sockFact.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
			this.registry.register(new Scheme("https", this.sockFact, 443));
		}

		// sets up parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		SingleClientConnManager manager = new SingleClientConnManager(params, this.registry);
		return new DefaultHttpClient(manager, params);
	}

	private static String getSingleHeader(HttpResponse resp, String name)
	{
		Header[] headers = resp.getHeaders(name);
		if(headers == null || headers.length == 0) return null;
		return headers[0].getValue();
	}
	
	public X509Certificate getLastServerCert()
	{
		if(this.sockFact == null || this.sockFact.lastSock == null) return null;
		SSLSession lastSess = this.sockFact.lastSock.getSession();
		Certificate[] certs;
		try {
			certs = lastSess.getPeerCertificates();
		} catch (SSLPeerUnverifiedException e) {
			return null;
		}
		if(certs.length == 0)
		{
			return null;
		} else {
			try
			{
				return (X509Certificate)certs[0];
			}
			catch(ClassCastException e)
			{
				return null;
			}
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

	private Reader submit(String endpoint, TransferObject reqObj) throws IOException
	{
		HttpClient client = buildClient();
		HttpPost post = new HttpPost(endpoint);
		String strReq = FormatHeader(this.version) + reqObj.Format(this.version);
		StringEntity request = new StringEntity(strReq);
		post.setEntity(request);
		post.addHeader("Content-type", "application/x-ofx");

		HttpResponse result = client.execute(post);

        StatusLine status = result.getStatusLine();
        int statusCode = status.getStatusCode();

    	String contentType = getSingleHeader(result, "Content-Type");
        InputStream entity = result.getEntity().getContent();
        Reader reader = new BufferedReader(new InputStreamReader(entity));

        if(statusCode == 200)
        {
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

	private String FormatHeader(float version)
	{
		UUID newFileUid = UUID.randomUUID();
		StringBuilder out = new StringBuilder();
		if(version < 2.0)
		{
			out.append("OFXHEADER:100\n")
				.append("DATA:OFXSGML\n")
				.append(String.format("VERSION:%03d\n", (int)(version * 100)));
			if(this.security)
			{
				out.append("SECURITY:TYPE1\n");
			} else {
				out.append("SECURITY:NONE\n");
			}
			out.append("ENCODING:UTF-8\n")
				.append("CHARSET:NONE\n")
				.append("COMPRESSION:NONE\n");
			if(this.fileUid != null)
			{
				out.append(String.format("OLDFILEUID:%s\n", this.fileUid.toString()));
			} else {
				out.append("OLDFILEUID:NONE\n");
			}
			out.append(String.format("NEWFILEUID:%s\n", newFileUid.toString()));
			this.fileUid = newFileUid;
			
			out.append("\n");
		} else {
			out.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
				.append("<?OFX ")
				.append("OFXHEADER=\"200\" ")
				.append(String.format("VERSION=\"%03d\" ", (int)(this.version * 100)));
			if(this.security)
			{
				out.append("SECURITY=\"TYPE1\" ");
			} else {
				out.append("SECURITY=\"NONE\" ");
			}
			if(this.fileUid != null)
			{
				out.append(String.format("OLDFILEUID=\"%s\" ", this.fileUid.toString()));
			} else {
				out.append("OLDFILEUID=\"NONE\" ");
			}
			out.append(String.format("NEWFILEUID=\"%s\" ", newFileUid.toString()))
				.append(" ?>");
			this.fileUid = newFileUid;
		}
		return out.toString();
	}
	
	private static TransferObject BuildMsgSet(OfxMessageReq.MessageSet thisSet, List<OfxMessageReq> list, float msgsetVer)
	{
		String setName = thisSet.name();

		TransferObject msgSet = new TransferObject(setName + String.format("MSGSRQV%d", (int)msgsetVer));
		Iterator<OfxMessageReq> iter = list.iterator();
		while(iter.hasNext())
		{
			OfxMessageReq req = iter.next();
			msgSet.put(req.BuildRequest(msgsetVer));
		}
		return msgSet;
	}

	public List<OfxMessageResp> parseResponse(Reader reader) throws XmlPullParserException, IOException
	{
		TransferObject response;
		if(this.version < 2.0)
		{
			response = OfxParser.parseV1(reader);
		} else {
			response = OfxParser.parseV2(reader);
		}
		
		List<OfxMessageResp> respList = new LinkedList<OfxMessageResp>();

		TransferObject root = response.getObj("OFX");
		Iterator<TransferObject.ObjValue> respIter = root.members.iterator();
		while(respIter.hasNext())
		{
			TransferObject child = respIter.next().child;
			if(child != null)
			{
				OfxMessageReq.MessageSet msgsetId =
						OfxMessageReq.MessageSet.valueOf(child.name.substring(0, child.name.length()-8));
				int ver = Integer.parseInt(child.name.substring(child.name.length()-1));
				Iterator<TransferObject.ObjValue> msgIter = child.members.iterator();
				while(msgIter.hasNext())
				{
					TransferObject msg = msgIter.next().child;
					if(msg != null)
					{
						TransferObject tran = null;
						if(msg.name.endsWith("TRNRS"))
						{
							String coreName = msg.name.substring(0, msg.name.length() - 5);
							tran = msg;
							msg = tran.getObj(coreName + "RS");
						}
						Iterator<OfxMessageReq> reqIter = this.contents.iterator();
						while(reqIter.hasNext())
						{
							OfxMessageReq req = reqIter.next();
							if(req.isValidResponse(msgsetId, ver, tran, msg))
							{
								OfxMessageResp resp = req.processResponse(tran, msg);
								if(resp != null)
								{
									respList.add(resp);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		return respList;
	}
}
