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
public class UserPassChangeMsgResp extends OfxMessageResp
{
	public String userId;
	public Date dtChanged;
/*
	public UserPassChangeMsgResp()
	{
	}
*/
	public UserPassChangeMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);
		this.userId = in.getAttr("USERID");
		
		String strValue = in.getAttr("DTCHANGED");
		this.dtChanged = TransferObject.parseDate(in.getAttr(strValue));
	}
}
