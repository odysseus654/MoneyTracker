/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signup;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;
import name.anderson.odysseus.moneytracker.ofx.bank.BankServiceAcctInfo;
import name.anderson.odysseus.moneytracker.ofx.cc.CcServiceAcctInfo;
import name.anderson.odysseus.moneytracker.ofx.loan.LoanServiceAcctInfo;

/**
 * @author Erik
 *
 */
public class AccountInfo
{
	public String desc;
	public String phone;
	
	public BankServiceAcctInfo bankInfo;
	public CcServiceAcctInfo ccInfo;
	public LoanServiceAcctInfo loanInfo; 
	
	public AccountInfo(TransferObject in)
	{
		this.desc = in.getAttr("DESC");
		this.phone = in.getAttr("PHONE");

		TransferObject sub = in.getObj("BANKACCTINFO");
		if(sub != null)
		{
			bankInfo = new BankServiceAcctInfo(sub);
		}

		sub = in.getObj("CCACCTINFO");
		if(sub != null)
		{
			ccInfo = new CcServiceAcctInfo(sub);
		}

		sub = in.getObj("LOANACCTINFO");
		if(sub != null)
		{
			loanInfo = new LoanServiceAcctInfo(sub);
		}
	}
}
