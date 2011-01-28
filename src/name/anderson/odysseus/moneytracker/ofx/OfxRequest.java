/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.io.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgReq;
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
	public UUID fileUid;
	public boolean useExpectContinue;
	public boolean anonymous;
	public List<OfxMessageReq> contents;
	protected OfxProfile profile;
	private SchemeRegistry registry;
	private OfxSSLSocketFactory sockFact;
	
	public OfxRequest(OfxProfile pro)
	{
		this.useExpectContinue = pro.useExpectContinue;
		this.profile = pro;
		this.version = 0;
		this.anonymous = false;
		this.contents = new LinkedList<OfxMessageReq>();
	}
	
	public void addRequest(OfxMessageReq req)
	{
		this.contents.add(req);
	}
	
	public String Format(boolean noAutoSon)
	{
		SortedMap<OfxMessageReq.MessageSet, List<OfxMessageReq> > requestSets
			= new TreeMap<OfxMessageReq.MessageSet, List<OfxMessageReq> >();

		// sort our requests into message sets
		for(OfxMessageReq req : this.contents)
		{
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

//		TransferObject signon = BuildMsgSet(OfxMessageReq.MessageSet.SIGNON, requestSets.get(OfxMessageReq.MessageSet.SIGNON),
//				this.profile.getMsgsetVer(this.version, OfxMessageReq.MessageSet.SIGNON));
//		requestSets.remove(OfxMessageReq.MessageSet.SIGNON);
		
		TransferObject req = new TransferObject("OFX");
		OfxMessageReq.MessageSet firstMsgSet = requestSets.isEmpty() ? null : requestSets.firstKey();
		
		SignonMsgReq thisSon = null;
		if(!noAutoSon)
		{
			thisSon = this.anonymous ? profile.createAnonymousSignon()
					: profile.createSignon(firstMsgSet == null ? OfxMessageReq.MessageSet.SIGNON : firstMsgSet);
		}
		if(thisSon != null && firstMsgSet != OfxMessageReq.MessageSet.SIGNON)
		{
			req.put(BuildMsgSet(OfxMessageReq.MessageSet.SIGNON, null,
					this.profile.getMsgsetVer(this.version, OfxMessageReq.MessageSet.SIGNON), thisSon));
		}

		for(OfxMessageReq.MessageSet thisSet : requestSets.keySet())
		{
			req.put(BuildMsgSet(thisSet, requestSets.get(thisSet), this.profile.getMsgsetVer(this.version, thisSet), thisSon));
		}

		return FormatHeader(false, this.version) + req.Format(this.version);
	}

	public List<OfxMessageResp> submit() throws IOException, XmlPullParserException
	{
		return submit(null, false);
	}

	public List<OfxMessageResp> submit(OfxMessageReq.MessageSet msgsetHint) throws IOException, XmlPullParserException
	{
		return submit(msgsetHint, false);
	}

	public List<OfxMessageResp> submit(OfxMessageReq.MessageSet msgsetHint, boolean noAutoSon) throws IOException, XmlPullParserException
	{
		SortedMap<OfxMessageReq.MessageSet, List<OfxMessageReq> > requestSets
		= new TreeMap<OfxMessageReq.MessageSet, List<OfxMessageReq> >();
		boolean bHasSon = false;
		boolean bHasNonGlobal = false;

		// sort our requests into message sets
		for(OfxMessageReq req : this.contents)
		{
			List<OfxMessageReq> reqList;
			boolean isGlobal = (req.messageSet == OfxMessageReq.MessageSet.SIGNON || req.messageSet == OfxMessageReq.MessageSet.SIGNUP);
			if(req instanceof SignonMsgReq) bHasSon = true;
			if(!isGlobal) bHasNonGlobal = true;

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
		
		SortedMap<String, TransferObject> requests = new TreeMap<String, TransferObject>();
		Set<String> encrypted = new TreeSet<String>();

		for(OfxMessageReq.MessageSet thisSet : requestSets.keySet())
		{
			boolean isGlobal = (thisSet == OfxMessageReq.MessageSet.SIGNON || thisSet == OfxMessageReq.MessageSet.SIGNUP);
			if(isGlobal && bHasNonGlobal) continue;
			
			String endpoint = this.profile.getEndpoint(isGlobal ? msgsetHint : thisSet);
			if(endpoint == null) return null;
/*
			// we requesting an encrypted connection here?
			if(!profile.ignoreEncryption)
			{
				MsgSetInfo info = profile.getInfo(thisSet);
				if(info != null && info.securePass && !encrypted.contains(endpoint))
				{
					encrypted.add(endpoint);
				}
			}
*/
			TransferObject req;
			SignonMsgReq thisSon = null;
			if(requests.containsKey(endpoint))
			{
				req = requests.get(endpoint);
			} else {
				req = new TransferObject("OFX");
				requests.put(endpoint, req);
				
				if(!bHasSon && !noAutoSon)
				{
					thisSon = this.anonymous ? profile.createAnonymousSignon() : profile.createSignon(thisSet);
					if(thisSon != null && thisSet != OfxMessageReq.MessageSet.SIGNON)
					{
						req.put(BuildMsgSet(OfxMessageReq.MessageSet.SIGNON, null,
								this.profile.getMsgsetVer(this.version, OfxMessageReq.MessageSet.SIGNON), thisSon));
					}
				}

				if(bHasNonGlobal)
				{
					for(OfxMessageReq.MessageSet gblSet : requestSets.keySet())
					{
						if(gblSet == OfxMessageReq.MessageSet.SIGNON || gblSet == OfxMessageReq.MessageSet.SIGNUP)
						{
							req.put(BuildMsgSet(gblSet, requestSets.get(gblSet), 
									this.profile.getMsgsetVer(this.version, gblSet), thisSon));
						}
					}
				}
			}

			float msgsetVer = this.profile.getMsgsetVer(this.version, thisSet);
			req.put(BuildMsgSet(thisSet, requestSets.get(thisSet), msgsetVer, thisSon));
		}

		List<OfxMessageResp> result = null;
		for(String endpoint : requests.keySet())
		{
			TransferObject req = requests.get(endpoint);
			Reader r = submit(encrypted.contains(endpoint), endpoint, req);
			List<OfxMessageResp> list = parseResponse(r);
			for(OfxMessageResp resp : list)
			{
				if(resp.trn != null && resp.trn.status != null && resp.trn.status.sev == StatusResponse.ST_ERROR)
				{
					throw new OfxError(resp.trn.status);
				}
			}
			if(result == null)
			{
				result = list;
			} else {
				result.addAll(list);
			}
		}
		return result;
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
		HttpProtocolParams.setUseExpectContinue(params, this.useExpectContinue);
		
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

	public Reader submit(boolean security, String endpoint, TransferObject reqObj) throws IOException
	{
		HttpClient client = buildClient();
		HttpPost post = new HttpPost(endpoint);
		String strReq = FormatHeader(security, this.version) + reqObj.Format(this.version);
		StringEntity request = new StringEntity(strReq);
		post.setEntity(request);
		post.addHeader("Content-type", "application/x-ofx");

		HttpResponse result = client.execute(post);

        StatusLine status = result.getStatusLine();
        int statusCode = status.getStatusCode();

    	String contentType = getSingleHeader(result, "Content-Type");
    	if(contentType != null)
    	{
			int semiPos = contentType.indexOf(';');
			if(semiPos != -1)
			{
				contentType = contentType.substring(0, semiPos).trim();
			}
    	}

		InputStream entity = result.getEntity().getContent();
        Reader reader = new BufferedReader(new InputStreamReader(entity));

        if(statusCode == 200)
        {
        	if(!contentType.equals("application/x-ofx") && !contentType.equals("text/ofx"))
        	{
            	throw new HttpResponseException(statusCode, "Unexpected Content-Type " + contentType);
        	} else {
        		return reader;
        	}
        }
        else
        {
        	String msg = status.getReasonPhrase();
        	if(msg == null || msg.equals(""))
        	{
        		msg = Utilities.convertStreamToString(reader);
        	}
        	reader.close();
        	throw new HttpResponseException(statusCode, msg);
        }
	}

	private String FormatHeader(boolean security, float version)
	{
		UUID newFileUid = UUID.randomUUID();
		StringBuilder out = new StringBuilder();
		if(version < 2.0)
		{
			out.append("OFXHEADER:100\n")
				.append("DATA:OFXSGML\n")
				.append(String.format("VERSION:%03d\n", (int)(version * 100)));
			if(security)
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
			if(security)
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
	
	public static TransferObject BuildMsgSet(OfxMessageReq.MessageSet thisSet, List<OfxMessageReq> list,
			float msgsetVer, SignonMsgReq thisSon)
	{
		String setName = thisSet.name();

		boolean hasSon = false;
		TransferObject msgSet = new TransferObject(setName + String.format("MSGSRQV%d", (int)msgsetVer));
		if(list != null)
		{
			for(OfxMessageReq req : list)
			{
				if(req.name.equals("SON")) hasSon = true;
				msgSet.put(req.BuildRequest(msgsetVer));
			}
		}
		if(thisSet == OfxMessageReq.MessageSet.SIGNON && thisSon != null && !hasSon)
		{
			msgSet.putHead(thisSon.BuildRequest(msgsetVer));
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
		for(TransferObject.ObjValue respObj : root.members)
		{
			TransferObject child = respObj.child;
			if(child != null)
			{
				OfxMessageReq.MessageSet msgsetId =
						OfxMessageReq.MessageSet.valueOf(child.name.substring(0, child.name.length()-8));
				int ver = Integer.parseInt(child.name.substring(child.name.length()-1));
				for(TransferObject.ObjValue msgObj : child.members)
				{
					TransferObject msg = msgObj.child;
					if(msg != null)
					{
						TransferObject tran = null;
						if(msg.name.endsWith("TRNRS"))
						{
							String coreName = msg.name.substring(0, msg.name.length() - 5);
							tran = msg;
							msg = tran.getObj(coreName + "RS");
						}
						for(OfxMessageReq req : this.contents)
						{
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
