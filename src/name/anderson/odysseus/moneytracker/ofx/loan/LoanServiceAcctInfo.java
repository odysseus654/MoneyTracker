/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.loan;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.TransferObject;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName;

/**
 * @author Erik
 *
 */
public class LoanServiceAcctInfo extends ServiceAcctInfo
{
	public static final int LT_FIXED = 1;
	public static final int LT_REVOLVE = 2;
	public static final int LT_OPEN = 3;
	public static final int LT_COMBO = 4;
	
	public static final int LF_WEEKLY = 1;
	public static final int LF_BIWEEKLY = 2;
	public static final int LF_TWICEMONTHLY = 3;
	public static final int LF_MONTHLY = 4;
	public static final int LF_FOURWEEKS = 5;
	public static final int LF_BIMONTHLY = 6;
	public static final int LF_QUARTERLY = 7;
	public static final int LF_SEMIANUALLY = 8;
	public static final int LF_ANUALLY = 9;
	public static final int LF_MATURITY = 10;

	public ServiceAcctName name;
	public int loanType;
	public int initNumPayments;
	public double initBalance;
	public int loanFreq;
	public Date loanStart;
	public Date loanMature;
//	public ... principalBal;
	public double balloonAmt;
//	public ... interest;
//	public ... rate;
//	public ... payment;
	public int remainPayments;
//	public boolean detailAvail;
//	public boolean xferSource;
//	public boolean xferDest;
//	public int status;
	
	/**
	 * @param in
	 */
	public LoanServiceAcctInfo(TransferObject in)
	{
		super(in);

		TransferObject sub = in.getObj("LOANACCTFROM");
		if(sub != null) this.name = new ServiceAcctName(ServiceAcctName.ServiceType.LOAN, sub);

		String str = in.getAttr("LOANTYPE");
		if(str != null)
		{
			if(str.equals("FIXED"))
			{
				this.loanType = LT_FIXED;
			}
			else if(str.equals("REVOLVE"))
			{
				this.loanType = LT_REVOLVE;
			}
			else if(str.equals("OPEN"))
			{
				this.loanType = LT_OPEN;
			}
			else if(str.equals("COMBO"))
			{
				this.loanType = LT_COMBO;
			}
		}

		str = in.getAttr("LOANINITNUMPMTS");
		this.initNumPayments = (str == null) ? 0 : Integer.parseInt(str);

		str = in.getAttr("LOANINITBAL");
		this.initBalance = (str == null) ? 0.0 : Double.parseDouble(str);
		
		str = in.getAttr("LOANFREQ");
		if(str != null)
		{
			if(str.equals("WEEKLY"))
			{
				this.loanFreq = LF_WEEKLY;
			}
			else if(str.equals("BIWEEKLY"))
			{
				this.loanFreq = LF_BIWEEKLY;
			}
			else if(str.equals("TWICEMONTHLY"))
			{
				this.loanFreq = LF_TWICEMONTHLY;
			}
			else if(str.equals("MONTHLY"))
			{
				this.loanFreq = LF_MONTHLY;
			}
			else if(str.equals("FOURWEEKS"))
			{
				this.loanFreq = LF_FOURWEEKS;
			}
			else if(str.equals("BIMONTHLY"))
			{
				this.loanFreq = LF_BIMONTHLY;
			}
			else if(str.equals("QUARTERLY"))
			{
				this.loanFreq = LF_QUARTERLY;
			}
			else if(str.equals("SEMIANUALLY"))
			{
				this.loanFreq = LF_SEMIANUALLY;
			}
			else if(str.equals("ANUALLY"))
			{
				this.loanFreq = LF_ANUALLY;
			}
			else if(str.equals("MATURITY"))
			{
				this.loanFreq = LF_MATURITY;
			}
		}

		str = in.getAttr("DTLOANSTART");
		this.loanStart = (str == null) ? null : TransferObject.parseDate(str);

		str = in.getAttr("DTLOANMATURITY");
		this.loanMature = (str == null) ? null : TransferObject.parseDate(str);

//		public ... principalBal;

		str = in.getAttr("BALLOONAMT");
		this.balloonAmt = (str == null) ? 0.0 : Double.parseDouble(str);

//		public ... interest;
//		public ... rate;
//		public ... payment;
		
		str = in.getAttr("LOANRMNPMTS");
		this.remainPayments = (str == null) ? 0 : Integer.parseInt(str);
	}
}
