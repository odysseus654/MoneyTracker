/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class ChallengeMsgResp extends OfxMessageResp
{
	public String userId;
	public String nonce;
	public String FIcertID;
/*
	public ChallengeMsgResp()
	{
	}
*/
	public ChallengeMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);

		this.userId = in.getAttr("USERID");
		this.nonce = in.getAttr("NONCE");
		this.FIcertID = in.getAttr("FICERTID");
	}
}
