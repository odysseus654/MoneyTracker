package name.anderson.odysseus.moneytracker.ofx;

import java.util.Date;

import android.content.Context;
import android.provider.Settings;
import name.anderson.odysseus.moneytracker.ofx.prof.SignonRealm;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgReq;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgResp;

public class LoginSession
{
	public OfxProfile profile;
	public SignonRealm realm;
	public int ID;
	
	// specified by user
	public String userid;
	public String userpass;
	public String userCred1;
	public String userCred2;
	public String authToken;

	// session keys
	public String sessionkey;
	public String mfaAnswerKey;
	public String sessionCookie;
	public Date sessionExpire;

	public OfxRequest newRequest()
	{
		OfxRequest req = profile.newRequest();
		req.session = this;
		req.addRequest(createSignon());
    	return req;
	}
	
	public SignonMsgReq createSignon()
	{
        SignonMsgReq son = this.profile.createAnonymousSignon();
        if(this.sessionkey != null)
        {
        	son.userkey = this.sessionkey;
        }
        else if(this.userid != null && this.userpass != null)
        {
        	son.reqUserkey = true;
        	son.userid = this.userid;
        	son.userpass = this.userpass;
        	son.userCred1 = this.userCred1;
        	son.userCred2 = this.userCred2;
        	son.authToken = this.authToken;
        }
    	if(realm != null && realm.clientUidReq)
    	{
    		son.clientUid = Settings.Secure.ANDROID_ID;
    	}
        son.sessCookie = this.sessionCookie;
        return son;
	}

	public void handleSignonResponse(Context ctx, SignonMsgResp resp)
	{
		if(this.ID == 0) return;
		boolean changed = false;
		
		if(resp.userKey != null)
		{
			this.sessionkey = resp.userKey;
			changed = true;
		}
		if(resp.tsKeyExpire != null)
		{
			this.sessionExpire = resp.tsKeyExpire;
			changed = true;
		}
		if(resp.sessCookie != null)
		{
			this.sessionCookie = resp.sessCookie;
			changed = true;
		}
		if(resp.accessKey != null)
		{
			this.mfaAnswerKey = resp.accessKey;
			changed = true;
		}
		
		if(changed)
		{
			ProfileTable db = new ProfileTable(ctx);
			try
			{
				db.openWritable();
				db.pushSession(this);
			}
			finally
			{
				db.close();
			}
		}
	}
}
