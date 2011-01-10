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
	public Date DTServer;
	public String userKey;
	public Date tsKeyExpire;
	public String language;
	public String country;
	public Date DTProfUp;
	public Date DTAcctUp;
	public String fiID;
	public String fiOrg;
	public String sessCookie;
	public String accessKey;

	public static final int STATUS_SUCCESS = 0;
	public static final int STATUS_ERROR = 2000;
	public static final int STATUS_MFA_REQUIRED = 3000;
	public static final int STATUS_MFA_INVALID = 3001;
	public static final int STATUS_FI_INVALID = 13504;
	public static final int STATUS_PINCH_NEEDED = 15000;
	public static final int STATUS_BAD_LOGIN = 15500;
	public static final int STATUS_ACCT_BUSY = 15501;
	public static final int STATUS_ACCT_LOCKED = 15502;
	public static final int STATUS_EMPTY_REQUEST = 15506;
	public static final int STATUS_PINCH_REQUIRED = 15507;
	public static final int STATUS_CLIENTUID_REJECTED = 15510;
	public static final int STATUS_CALL_US = 15511;
	public static final int STATUS_AUTHTOKEN_REQUIRED = 15512;
	public static final int STATUS_AUTHTOKEN_INVALID = 15513;
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
