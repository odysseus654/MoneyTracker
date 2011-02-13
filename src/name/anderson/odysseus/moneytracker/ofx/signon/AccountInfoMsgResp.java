/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signon;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.acct.*;

/**
 * @author Erik
 *
 */
public class AccountInfoMsgResp extends OfxMessageResp
{
	public Date acctListAge;
	public List<ServiceAcctInfo> accounts;

	public AccountInfoMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);
		
		this.acctListAge = TransferObject.parseDate(in.getAttr("DTACCTUP"));

		this.accounts = new LinkedList<ServiceAcctInfo>();

		for(TransferObject.ObjValue acct : in.members)
		{
			TransferObject info = acct.child;
			if(info != null && info.name.equals("ACCTINFO"))
			{
				String desc = info.getAttr("DESC");
				String phone = info.getAttr("PHONE");

				TransferObject sub = info.getObj("BANKACCTINFO");
				if(sub != null)
				{
					ServiceAcctInfo bankInfo = new ServiceAcctInfo(ServiceAcctName.ServiceType.BANK, sub);
					bankInfo.desc = desc;
					bankInfo.phone = phone;
					this.accounts.add(bankInfo);
				}

				sub = info.getObj("CCACCTINFO");
				if(sub != null)
				{
					ServiceAcctInfo ccInfo = new ServiceAcctInfo(ServiceAcctName.ServiceType.CC, sub);
					ccInfo.desc = desc;
					ccInfo.phone = phone;
					this.accounts.add(ccInfo);
				}

				sub = info.getObj("LOANACCTINFO");
				if(sub != null)
				{
					ServiceAcctInfo loanInfo = new LoanServiceAcctInfo(sub);
					loanInfo.desc = desc;
					loanInfo.phone = phone;
					this.accounts.add(loanInfo);
				}
			}
		}
	}
}
