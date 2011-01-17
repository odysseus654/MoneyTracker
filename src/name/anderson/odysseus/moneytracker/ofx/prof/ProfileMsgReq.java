/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik Anderson
 *
 */
public class ProfileMsgReq extends OfxMessageReq
{
	public Date profAge;

	/**
	 * @param ms
	 * @param n
	 */
	public ProfileMsgReq()
	{
		super(MessageSet.PROF, "PROF");
	}

	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		obj.put("CLIENTROUTING", "MSGSET");
		if(this.profAge == null)
		{
			obj.put("DTPROFUP", "19700101000000");
		} else {
			obj.put("DTPROFUP", this.profAge);
		}
	}

	@Override
	public boolean isValidResponse(MessageSet msgsetId, int ver, TransferObject tran, TransferObject obj)
	{	// prob need to add transaction-matching later
		return msgsetId == messageSet && tran != null && tran.name.equals(this.name + "TRNRS");
	}
	
	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new ProfileMsgResp(tran, obj);
	}
}
