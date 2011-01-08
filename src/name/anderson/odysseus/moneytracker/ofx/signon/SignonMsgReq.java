/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import java.util.*;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik Anderson
 *
 */
public class SignonMsgReq extends OfxMessageReq
{
	public String userid;
	public String userpass;
	public boolean reqUserkey;
	public String userkey;
	public String fiID;
	public String fiOrg;
	public String lang;
	public String sessCookie;
	public String appId;
	public int appVer;
	public String clientUid;
	public String userCred1;
	public String userCred2;
	public String authToken;
	public String accessKey;
	public List<MfaChallenge> mfaChallenges;
	
	public class MfaChallenge
	{
		public String phraseID;
		public String answer;
	}
	
	/**
	 * @param ms
	 * @param n
	 */
	public SignonMsgReq()
	{
		super(MessageSet.SIGNON, "SON");
		this.lang = "ENG";
		this.appId = "MONTK";
		this.appVer = 1;
		this.reqUserkey = false;
	}
	
	protected TransferObject BuildTransaction()
	{
		return null;
	}

	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		obj.put("DTCLIENT", new Date());
		if(this.userkey != null)
		{
			obj.put("USERKEY", this.userkey);
		}
		else if(this.userid != null && this.userpass != null)
		{
			obj.put("USERID", this.userid);
			obj.put("USERPASS", this.userpass);
			if(msgsetVer == 1.2 && this.authToken != null)
			{
				obj.put("ONETIMEPASS", this.authToken);
			}
			if(this.reqUserkey)
			{
				obj.put("GENUSERKEY", true);
			}
		}
		else
		{
			obj.put("USERID", "anonymous00000000000000000000000");
			obj.put("USERPASS", "anonymous00000000000000000000000");
		}
		obj.put("LANGUAGE", this.lang);
		if(this.fiID != null || this.fiOrg != null)
		{
			TransferObject fi = new TransferObject("FI");
			if(this.fiID != null) fi.put("ORG", this.fiOrg);
			if(this.fiID != null) fi.put("FID", this.fiID);
			obj.put(fi);
		}
		if(this.sessCookie != null) obj.put("SESSCOOKIE", this.sessCookie);
		obj.put("APPID", this.appId);
		obj.put("APPVER", String.format("%04d", this.appVer));
		if(this.clientUid != null) obj.put("CLIENTUID", this.clientUid);
		if(this.userCred1 != null) obj.put("USERCRED1", this.userCred1);
		if(this.userCred2 != null) obj.put("USERCRED2", this.userCred2);
		if(this.authToken != null) obj.put("AUTHTOKEN", this.authToken);
		if(this.accessKey != null) obj.put("ACCESSKEY", this.accessKey);
		if(this.mfaChallenges != null)
		{
			for(MfaChallenge mfaAnswer : this.mfaChallenges)
			{
				TransferObject mfa = new TransferObject("MFACHALLENGEANSWER");
				mfa.put("MFAPHRASEID", mfaAnswer.phraseID);
				mfa.put("MFAPHRASEA", mfaAnswer.answer);
				obj.put(mfa);
			}
		}
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new SignonMsgResp(obj);
	}
}
