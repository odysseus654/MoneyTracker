/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signup;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class AccountInfoMsgResp extends OfxMessageResp
{
	public Date acctListAge;
	public List<AccountInfo> accounts;

	public AccountInfoMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);
		
		this.acctListAge = TransferObject.parseDate(in.getAttr("DTACCTUP"));

		this.accounts = new LinkedList<AccountInfo>();

		for(TransferObject.ObjValue acct : in.members)
		{
			TransferObject info = acct.child;
			if(info != null && info.name.equals("ACCTINFO"))
			{
				this.accounts.add(new AccountInfo(info));
			}
		}
	}
}
