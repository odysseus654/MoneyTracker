/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.io.*;
import java.security.cert.Certificate;
import java.util.*;

import org.apache.http.client.HttpResponseException;
import org.xmlpull.v1.XmlPullParserException;
import name.anderson.odysseus.moneytracker.ofx.OfxMessageReq.MessageSet;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import name.anderson.odysseus.moneytracker.prof.OfxFiDefinition;

/**
 * @author Erik Anderson
 *
 */
public class OfxProfile extends OfxFiDefinition
{
//	public String name;
//	public int defID;
//	public String fiURL;
//	public String fiOrg;
//	public String fiID;
//	public String appId;
//	public int appVer;
//	public float ofxVer;
//	public boolean simpleProf;
//	public String srcName;
//	public String srcId;
	public String  userid;
	public String  userpass;
	public String  userkey;
	public String  lang;
	public String  sessCookie;
	public boolean security;
	public Date    profAge;
	
	final static float DEFAULT_OFX_2x = 2.1f;
	final static float DEFAULT_OFX_1x = 1.6f;
	final static String DEFAULT_APP_ID = "QWIN";
	final static int DEFAULT_APP_VER = 1900;
	
	public OfxProfile()
	{
	}
	
	public OfxProfile(OfxFiDefinition src)
	{
		name = src.name;
		defID = src.defID;
		fiURL = src.fiURL;
		fiOrg = src.fiOrg;
		fiID = src.fiID;
		appId = src.appId;
		appVer = src.appVer;
		ofxVer = src.ofxVer;
		simpleProf = src.simpleProf;
		srcName = src.srcName;
		srcId = src.srcId;
	}
	
	public void negotiate() throws XmlPullParserException, IOException
	{
		OfxRequest req = new OfxRequest(this);
		req.version = this.ofxVer == 0 ? DEFAULT_OFX_2x : this.ofxVer;
		req.security = false;
		SignonMsgReq son = newSonRequest(true); 
        req.addRequest(son);
        req.addRequest(newProfRequest());

        Reader resp = null;
    	List<OfxMessageResp> response;
        do {
	        try {
	        	resp = req.submit();
	        }
	        catch(HttpResponseException e)
	        {
	        	if(e.getStatusCode() == 400)
	        	{
	        		if(this.ofxVer == 0 && req.version == DEFAULT_OFX_2x)
	        		{
	        			// 2.x autodetected request failed, let's try 1.x 
	        			req.version = DEFAULT_OFX_1x;
	        			continue;
	        		}
	        		if(son.appId == null && (this.ofxVer != 0 || req.version == DEFAULT_OFX_1x))
	        		{
	        			// we failed either an explicit version or an autodetect scan, start over with an override app
	        			son.appId = DEFAULT_APP_ID;
	        			son.appVer = DEFAULT_APP_VER;
	        			req.version = this.ofxVer == 0 ? DEFAULT_OFX_2x : this.ofxVer;
	        			continue;
	        		}
	        	}
	        	
	        	// unexpected error or no further actions known
	        	throw(e);
	        }
        } while(false);

        response = req.parseResponse(resp);
    	Certificate[] certs = req.getLastServerCert();
        
        // negotiation successful, punch our values
        this.ofxVer = req.version;
		this.appId = son.appId;
		this.appVer = son.appVer;
	}
	
	public OfxRequest newRequest(boolean anon)
	{
		OfxRequest req = new OfxRequest(this);
		req.version = this.ofxVer;
		req.security = this.security;

        req.addRequest(newSonRequest(anon));

    	return req;
	}
	
	private SignonMsgReq newSonRequest(boolean anon)
	{
        SignonMsgReq son = new SignonMsgReq();
        if(!anon)
        {
	        if(this.userkey != null)
	        {
	        	son.userkey = this.userkey;
	        }
	        else if(this.userid != null && this.userpass != null)
	        {
	        	son.userid = this.userid;
	        	son.userpass = this.userpass;
	        }
	        son.sessCookie = this.sessCookie;
        }
        if(this.lang != null) son.lang = this.lang;
        if(this.appId != null)
        {
        	son.appId = this.appId;
        	son.appVer = this.appVer;
        }
        if(this.fiID != null) son.fiID = this.fiID;
        if(this.fiOrg != null) son.fiOrg = this.fiOrg;
        return son;
	}
	
	public ProfileMsgReq newProfRequest()
	{
		ProfileMsgReq pro = new ProfileMsgReq();
		pro.profAge = this.profAge;
		return pro;
	}

	public String getEndpoint(OfxMessageReq.MessageSet thisSet)
	{
		// default behavior: Prof is the endpoint, no other sets permitted without a profile
		// (we should never be called with Signon)
//		if(thisSet == OfxMessageReq.MessageSet.Prof)
//		{
			return this.fiURL;
//		} else {
//			return null;
//		}
	}

	public float getMsgsetVer(float ofxVer, MessageSet thisSet)
	{
		// default behavior: report V1
		return ((float)((int)ofxVer)) + 0.1f;
	}
}
