/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class ChallengeMsgReq extends OfxMessageReq
{
	public String userid;
	public String FiCertID;

	public ChallengeMsgReq()
	{
		super(MessageSet.SIGNON, "CHALLENGE");
	}

	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		obj.put("USERID", this.userid);
		if(this.FiCertID != null) obj.put("FICERTID", this.FiCertID);
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new ChallengeMsgResp(tran, obj);
	}
}
