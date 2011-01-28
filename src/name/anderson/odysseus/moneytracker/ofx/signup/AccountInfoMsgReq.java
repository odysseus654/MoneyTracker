/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signup;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class AccountInfoMsgReq extends OfxMessageReq
{
	public Date acctListAge;

	public AccountInfoMsgReq()
	{
		super(MessageSet.SIGNUP, "ACCTINFO");
	}

	@Override
	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		if(this.acctListAge == null)
		{
			obj.put("DTPROFUP", "19700101000000");
		} else {
			obj.put("DTPROFUP", this.acctListAge);
		}
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new AccountInfoMsgResp(tran, obj);
	}
}
