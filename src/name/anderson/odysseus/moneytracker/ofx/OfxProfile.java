/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.util.Date;

import name.anderson.odysseus.moneytracker.ofx.OfxMessageReq.MessageSet;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;

/**
 * @author Erik Anderson
 *
 */
public class OfxProfile
{
	public String  userid;
	public String  userpass;
	public String  userkey;
	public String  fiID;
	public String  fiOrg;
	public String  fiURL;
	public String  lang;
	public String  sessCookie;
	public String  appId;
	public float   appVer;
	public float   ofxVer;
	public boolean security;
	public Date    profAge;
	
	public OfxRequest newRequest()
	{
		OfxRequest req = new OfxRequest(this);
		req.version = this.ofxVer;
		req.security = this.security;

        req.addRequest(newSonRequest());

    	return req;
	}
	
	private SignonMsgReq newSonRequest()
	{
        SignonMsgReq son = new SignonMsgReq();
        if(this.userkey != null) son.userkey = this.userkey;
        else if(this.userid != null && this.userpass != null)
        {
        	son.userid = this.userid;
        	son.userpass = this.userpass;
        }
        if(this.lang != null) son.lang = this.lang;
        if(this.appId != null)
        {
        	son.appId = this.appId;
        	son.appVer = this.appVer;
        }
        son.sessCookie = this.sessCookie;
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
