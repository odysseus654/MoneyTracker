/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.OfxMessageReq;
import name.anderson.odysseus.moneytracker.ofx.OfxMessageResp;
import name.anderson.odysseus.moneytracker.ofx.TransferObject;

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
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new ProfileMsgResp(tran, obj);
	}
}
