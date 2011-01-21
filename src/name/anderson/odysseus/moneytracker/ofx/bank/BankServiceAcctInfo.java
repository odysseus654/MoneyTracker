/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.bank;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName;

/**
 * @author Erik
 *
 */
public class BankServiceAcctInfo extends ServiceAcctInfo
{
	public ServiceAcctName name;
//	public boolean detailAvail;
//	public boolean xferSource;
//	public boolean xferDest;
//	public int status;

	/**
	 * @param in
	 */
	public BankServiceAcctInfo(TransferObject in)
	{
		super(in);
		
		TransferObject sub = in.getObj("BANKACCTFROM");
		if(sub != null) this.name = new ServiceAcctName(ServiceAcctName.ServiceType.BANK, sub);
	}
}
