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
