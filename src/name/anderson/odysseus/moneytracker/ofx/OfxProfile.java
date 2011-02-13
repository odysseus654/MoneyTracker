/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import android.content.Context;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import name.anderson.odysseus.moneytracker.prof.OfxFiDefinition;
import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Erik Anderson
 *
 */
public class OfxProfile
{
	public OfxFiDefinition fidef;
	public FiDescr fidescr;
	public Date    profAge;
	public Date    acctListAge;
	public String  lang;
	public X509Certificate lastCert;
	public boolean useExpectContinue;
	public boolean ignoreEncryption;
	public boolean profileIsUser;
	public int parentID;
	public int ID;
	public LoginSession session;

	public Map<String,Endpoint> endpoints;
	public Map<String,SignonRealm> realms;
	public Map<String,Login> realmLogins;
	public Map<OfxMessageReq.MessageSet, MsgSetInfo> msgsetMap;
	
	final static float DEFAULT_OFX_2x = 2.11f;
	final static float DEFAULT_OFX_1x = 1.6f;
	final static String FALLBACK_APP_ID = "QWIN";
	final static int FALLBACK_APP_VER = 1900;
	
	public static class Endpoint
	{
		public Map<OfxMessageReq.MessageSet, MsgSetInfo> msgsetInfo;
		
		public Endpoint()
		{
			msgsetInfo = new TreeMap<OfxMessageReq.MessageSet, MsgSetInfo>();
		}
	}
	
	public static class Login
	{
		public String  userid;
		public String  userpass;
		public String  userkey;
		public String  sessCookie;
	}
	
	public OfxProfile()
	{
		this.useExpectContinue = true;
	}
	
	public OfxProfile(OfxFiDefinition src)
	{
		this.useExpectContinue = true;
		this.fidef = src != null ? src : new OfxFiDefinition();
	}
	
	public void negotiate(Context ctx) throws XmlPullParserException, IOException
	{
		OfxRequest req = new OfxRequest(this);
		req.version = this.fidef.ofxVer == 0 ? DEFAULT_OFX_2x : this.fidef.ofxVer;
		req.anonymous = true;
		SignonMsgReq son = createAnonymousSignon(); 
        req.addRequest(son);
        req.addRequest(newProfRequest(false));

    	List<OfxMessageResp> response;
        for(;;) {
	        try {
	        	response = req.submit(ctx);
	        }
	        catch(HttpResponseException e)
	        {
	        	boolean bIgnoreAndGetOut = false;
	        	switch(e.getStatusCode())
	        	{
	        	case 417: // "Expectation Failed"
		        	if(req.useExpectContinue)
		        	{
		        		// retry without the expect:continue clause
		        		req.useExpectContinue = false;
		        		continue;
		        	}
		        	break;
		        	
	        	case 501: // "Not Implemented"
	        		// I guess the server understood the request enough to reject it, so it's not a version issue?
	        		if(son.appId == null)
	        		{
	        			// we failed either an explicit version or an autodetect scan, start over with an override app
	        			son.appId = FALLBACK_APP_ID;
	        			son.appVer = FALLBACK_APP_VER;
	        			continue;
	        		} else {
	        			// okay, server's not gonna give us a profile.  Let's just continue with what we've got
	        			bIgnoreAndGetOut = true;
	        		}
	        		break;

	        	case 400: // "Bad Request"
	        		if(this.fidef.ofxVer == 0 && req.version == DEFAULT_OFX_2x)
	        		{
	        			// 2.x autodetected request failed, let's try 1.x 
	        			req.version = DEFAULT_OFX_1x;
	        			continue;
	        		}
	        		if(son.appId == null && (this.fidef.ofxVer != 0 || req.version == DEFAULT_OFX_1x))
	        		{
	        			// we failed either an explicit version or an autodetect scan, start over with an override app
	        			son.appId = FALLBACK_APP_ID;
	        			son.appVer = FALLBACK_APP_VER;
	        			req.version = this.fidef.ofxVer == 0 ? DEFAULT_OFX_2x : this.fidef.ofxVer;
	        			continue;
	        		}
	        		break;
	        	}
	        	
	        	// unexpected error or no further actions known
	        	if(bIgnoreAndGetOut)
	        	{
	        		break;
	        	} else {
	        		throw e;
	        	}
	        }
	        catch(OfxError e)
	        {
	        	if(e.getErrorCode() != StatusResponse.STATUS_BAD_LOGIN)
	        	{
	        		// server won't give us a profile without a login, but prob means we're saying the right thing
	        		break;
	        	}
        		throw e;
	        }
	        catch(XmlPullParserException e)
	        {
	        	// did this return a 1.x response to a 2.x query?
        		if(this.fidef.ofxVer == 0 && req.version == DEFAULT_OFX_2x)
        		{
        			req.version = DEFAULT_OFX_1x;
        			continue;
        		} else {
        			throw e;
        		}
	        }
	        
	    	for(OfxMessageResp resp : response)
	    	{
	    		if(resp instanceof ProfileMsgResp)
	    		{
	    			mergeProfileResponse(req.version, (ProfileMsgResp)resp, true);
	    		}
	    	}
	        break;
        }
        this.lastCert = req.getLastServerCert();
        
        // negotiation successful, punch our values
        this.fidef.ofxVer = req.version;
		this.fidef.appId = son.appId;
		this.fidef.appVer = son.appVer;
		// req.useExpectContinue
	}
	
	static public String exceptionComment(Exception e)
	{
		if(e instanceof OfxError)
        {
			return "Server gave an error";
        }
		else if(e instanceof HttpResponseException)
        {
			switch(((HttpResponseException)e).getStatusCode())
			{
			case 200:
				return "Got a strange response from the server (maybe the location points to a webpage?)";
			case 400:
				return "Server rejected the request";
			default:
				return "Got a strange response from the server (maybe the location has been removed?)";
			}
        }
		else if(e instanceof XmlPullParserException)
        {
			return "Couldn't understand the response from the server";
        }
		else if(e instanceof SSLPeerUnverifiedException)
        {
			return "We rejected the server identity (this might not be the bank you think it is)";
        }
		else if(e instanceof ClientProtocolException || e instanceof ConnectException
				|| e instanceof SocketException || e instanceof SSLException)
        {
			return "Unable to connect to server";
        }
		else
        {
			return "Unexpected error";
        }
	}
	
	public OfxRequest newRequest()
	{
		OfxRequest req = new OfxRequest(this);
		req.version = this.fidef.ofxVer;
    	return req;
	}
	
	public ProfileMsgReq newProfRequest(boolean useDate)
	{
		ProfileMsgReq pro = new ProfileMsgReq();
		if(useDate) pro.profAge = this.profAge;
		return pro;
	}
	
	public void mergeProfileResponse(float ofxVer, ProfileMsgResp resp, boolean isUserLogin)
	{
		if(resp.trn.status.code == 1) return;
		
		Map<String,Endpoint> newEPs = new TreeMap<String,Endpoint>();
		Map<String,SignonRealm> newRealms = new TreeMap<String,SignonRealm>();
		Map<OfxMessageReq.MessageSet, MsgSetInfo> newMap = new TreeMap<OfxMessageReq.MessageSet, MsgSetInfo>();

		for(OfxMessageReq.MessageSet thisSet : resp.msgsetList.keySet())
		{
			List<MsgSetInfo> infoList = resp.msgsetList.get(thisSet);
			int verLimit = 99;
			int maxVer;
			for(;;)
			{
				maxVer = 0;
				for(MsgSetInfo info : infoList)
				{
					if(info.ver > maxVer && info.ver < verLimit) maxVer = info.ver;
				}
				float thisVer = ((float)((int)ofxVer)) + ((float)maxVer)/10;
				if(maxVer > 0 && !isVersionAcceptible(thisVer))
				{
					verLimit = maxVer;
					continue;
				} else {
					break;
				}
			}
			if(maxVer == 0) continue;	// we weren't able to find an acceptable version
			
			for(MsgSetInfo info : infoList)
			{
				if(info.ver == maxVer)
				{
					// do we have this endpoint yet?
					Endpoint thisEP;
					if(!newEPs.containsKey(info.URL))
					{
						thisEP = new Endpoint();
						newEPs.put(info.URL, thisEP);
					} else {
						thisEP = newEPs.get(info.URL);
					}
					
					// do we have this realm yet?
					SignonRealm realm = info.realm;
					if(realm != null && !newRealms.containsKey(realm.name))
					{
						newRealms.put(realm.name, realm);
					}
					
					thisEP.msgsetInfo.put(thisSet, info);
					if(thisSet != OfxMessageReq.MessageSet.SIGNON && thisSet != OfxMessageReq.MessageSet.SIGNUP)
					{
						newMap.put(thisSet, info);
					}
				}
			}
		}

		if(isUserLogin && !this.profileIsUser)
		{
			this.parentID = this.ID;
			this.ID = 0;
		}
		else if(!isUserLogin && this.profileIsUser)
		{
			this.ID = this.parentID;
			this.parentID = 0;
		}
		this.profileIsUser = isUserLogin;
		this.fidescr = resp.descr;
		this.profAge = resp.DtProfileUp;
		this.endpoints = newEPs;
		this.realms = newRealms;
		this.msgsetMap = newMap;
	}

	public void handleSignonResponse(Context ctx, SignonMsgResp resp)
	{
		if(this.session == null || this.session.ID == 0) return;
		boolean changed = false;
		
		if(resp.userKey != null)
		{
			this.session.sessionkey = resp.userKey;
			changed = true;
		}
		if(resp.tsKeyExpire != null)
		{
			this.session.sessionExpire = resp.tsKeyExpire;
			changed = true;
		}
		if(resp.sessCookie != null)
		{
			this.session.sessionCookie = resp.sessCookie;
			changed = true;
		}
		if(resp.accessKey != null)
		{
			this.session.mfaAnswerKey = resp.accessKey;
			changed = true;
		}
		
		if(changed)
		{
			ProfileTable db = new ProfileTable(ctx);
			try
			{
				db.open();
				db.pushSession(this.session);
			}
			finally
			{
				db.close();
			}
		}
	}

	private boolean isVersionAcceptible(float msgsetVer)
	{
		return true;
	}

	public float getMsgsetVer(float ofxVer, OfxMessageReq.MessageSet thisSet)
	{
		MsgSetInfo info = getInfo(thisSet);
		return ((float)((int)ofxVer)) + (info == null ? 0.1f : ((float)info.ver) / 10);
	}

	public MsgSetInfo getInfo(OfxMessageReq.MessageSet thisSet)
	{
		if(this.msgsetMap == null || !this.msgsetMap.containsKey(thisSet))
		{
			return null;
		} else {
			return this.msgsetMap.get(thisSet);
		}
	}

	public String getEndpoint(OfxMessageReq.MessageSet thisSet)
	{
		if(this.msgsetMap == null)
		{
			return this.fidef.fiURL;
		}
		else if(!this.msgsetMap.containsKey(thisSet))
		{
			return null;
		}
		else
		{
			return this.msgsetMap.get(thisSet).URL;
		}
	}

	public SignonMsgReq createSignon(OfxMessageReq.MessageSet msgset)
	{
		String realm;
		if(this.msgsetMap == null)
		{
			realm = "";
		}
		else if(!this.msgsetMap.containsKey(msgset))
		{
			return null;
		}
		else
		{
			MsgSetInfo info = this.msgsetMap.get(msgset);
			realm = info.realm == null ? "" : info.realm.name;
		}
		if(this.realmLogins == null || !this.realmLogins.containsKey(realm))
		{
			return createAnonymousSignon();
		} else {
			return createSignon(this.realmLogins.get(realm));
		}
	}

	public SignonMsgReq createSignon(Login login)
	{
        SignonMsgReq son = createAnonymousSignon();
        if(login.userkey != null)
        {
        	son.userkey = login.userkey;
        }
        else if(login.userid != null && login.userpass != null)
        {
        	son.userid = login.userid;
        	son.userpass = login.userpass;
        }
        son.sessCookie = login.sessCookie;
        return son;
	}
	
	public SignonMsgReq createAnonymousSignon()
	{
        SignonMsgReq son = new SignonMsgReq();
        if(this.lang != null) son.lang = this.lang;
        if(this.fidef.appId != null)
        {
        	son.appId = this.fidef.appId;
        	son.appVer = this.fidef.appVer;
        }
        if(this.fidef.fiID != null) son.fiID = this.fidef.fiID;
        if(this.fidef.fiOrg != null) son.fiOrg = this.fidef.fiOrg;
        return son;
	}
	
	public boolean requiresRealmPrompt()
	{
		return this.realms != null && this.realms.size() > 1 && !this.fidef.simpleProf;
	}
}
