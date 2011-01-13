/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.signup;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;

/**
 * @author Erik
 *
 */
public class AccountInfo
{
	public enum Service { BANK, CC, LOAN };

	public String desc;
	public String phone;
	
	public AccountInfo(TransferObject in)
	{
		this.desc = in.getAttr("DESC");
		this.phone = in.getAttr("PHONE");

		// TODO Auto-generated constructor stub
	}

}
