/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class UserPassChangeMsgReq extends OfxMessageReq
{
	public String userid;
	public String newuserpass;

	public UserPassChangeMsgReq()
	{
		super(MessageSet.SIGNON, "PINCH");
	}

	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		obj.put("USERID", this.userid);
		obj.put("NEWUSERPASS", this.newuserpass);
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new UserPassChangeMsgResp(tran, obj);
	}
}
