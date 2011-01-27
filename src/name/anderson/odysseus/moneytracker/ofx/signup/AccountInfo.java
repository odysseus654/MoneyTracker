/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signup;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;
import name.anderson.odysseus.moneytracker.ofx.acct.*;

/**
 * @author Erik
 *
 */
public class AccountInfo
{
	public String desc;
	public String phone;
	
	public ServiceAcctInfo bankInfo;
	public ServiceAcctInfo ccInfo;
	public LoanServiceAcctInfo loanInfo; 
	
	public AccountInfo(TransferObject in)
	{
		this.desc = in.getAttr("DESC");
		this.phone = in.getAttr("PHONE");

		TransferObject sub = in.getObj("BANKACCTINFO");
		if(sub != null)
		{
			bankInfo = new ServiceAcctInfo(ServiceAcctName.ServiceType.BANK, sub);
		}

		sub = in.getObj("CCACCTINFO");
		if(sub != null)
		{
			ccInfo = new ServiceAcctInfo(ServiceAcctName.ServiceType.CC, sub);
		}

		sub = in.getObj("LOANACCTINFO");
		if(sub != null)
		{
			loanInfo = new LoanServiceAcctInfo(sub);
		}
	}
}
