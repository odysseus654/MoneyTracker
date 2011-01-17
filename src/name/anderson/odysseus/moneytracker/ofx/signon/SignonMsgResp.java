/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class SignonMsgResp extends OfxMessageResp
{
	public StatusResponse status;
	public Date DTServer;				// current server timestamp
	public String userKey;				// session key
	public Date tsKeyExpire;			// expiration of session key
	public String language;				// language
	public String country;				// country
	public Date DTProfUp;				// last profile update date
	public Date DTAcctUp;				// last account list update date
	public String fiID;					// fi ID
	public String fiOrg;				// fi ORG
	public String sessCookie;			// session cookie
	public String accessKey;			// access key

//case StatusResponse.STATUS_SUCCESS: // Success (INFO)
//case StatusResponse.STATUS_ERROR: // General error (ERROR)
//case StatusResponse.STATUS_MFA_REQUIRED: // User credentials are correct, but further authentication required (ERROR)
//case StatusResponse.STATUS_MFA_INVALID: // MFACHALLENGEA contains invalid information (ERROR)
//case StatusResponse.STATUS_FI_INVALID: // <FI> Missing or Invalid in <SONRQ> (ERROR)
//case StatusResponse.STATUS_PINCH_NEEDED: // Must change USERPASS (INFO)
//case StatusResponse.STATUS_BAD_LOGIN: // Signon invalid (see section 2.5.1) (ERROR)
//case StatusResponse.STATUS_ACCT_BUSY: // Customer account already in use (ERROR)
//case StatusResponse.STATUS_ACCT_LOCKED: // USERPASS Lockout (ERROR)
//case StatusResponse.STATUS_EMPTY_REQUEST: // Empty signon transaction not supported (ERROR)
//case StatusResponse.STATUS_PINCH_REQUIRED: // Signon invalid without supporting pin change request (ERROR)
//case StatusResponse.STATUS_CLIENTUID_REJECTED: // CLIENTUID error (ERROR)
//case StatusResponse.STATUS_CALL_US: // User should contact financial institution (ERROR)
//case StatusResponse.STATUS_AUTHTOKEN_REQUIRED: // OFX server requires AUTHTOKEN in signon during the next session (ERROR)
//case StatusResponse.STATUS_AUTHTOKEN_INVALID:// AUTHTOKEN invalid (ERROR)

/*
	public SignonMsgResp()
	{
	}
*/
	public SignonMsgResp(TransferObject in)
	{
		this.status = new StatusResponse(in.getObj("STATUS"));
		this.DTServer = TransferObject.parseDate(in.getAttr("DTSERVER"));
		this.userKey = in.getAttr("USERKEY");
		
		String strValue = in.getAttr("TSKEYEXPIRE");
		if(strValue != null) this.tsKeyExpire = TransferObject.parseDate(strValue);
		
		this.language = in.getAttr("LANGUAGE");
		this.country = in.getAttr("COUNTRY");
		
		strValue = in.getAttr("DTPROFUP");
		if(strValue != null) this.DTProfUp = TransferObject.parseDate(strValue);
		
		strValue = in.getAttr("DTACCTUP");
		if(strValue != null) this.DTAcctUp = TransferObject.parseDate(strValue);
		
		TransferObject fiObj = in.getObj("FI");
		if(fiObj != null)
		{
			this.fiOrg = fiObj.getAttr("ORG");
			this.fiID = fiObj.getAttr("FID");
		}
		
		this.sessCookie = in.getAttr("SESSCOOKIE");
		this.accessKey = in.getAttr("ACCESSKEY");
	}
}
