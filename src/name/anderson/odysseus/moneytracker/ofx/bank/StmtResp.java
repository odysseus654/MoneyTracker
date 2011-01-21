package name.anderson.odysseus.moneytracker.ofx.bank;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName;

public class StmtResp extends OfxMessageResp
{
	public String curDef;
	public ServiceAcctName name;
	public Date startDt;
	public Date endDt;
	public List<Transaction> transList;
	public double ledgerBal;
	public Date ledgerBalDate;
	public double availBal;
	public Date availBalDate;
	public List<BalanceResponse> balList;
	public String marketInfo;
	
	public static class Transaction
	{
		public enum EType {
			CREDIT, DEBIT, INT, DIV, FEE, SRVCHG, DEP, ATM, POS, XFER, CHECK, PAYMENT, CASH, DIRECTDEP,
			DIRECTDEBIT, REPEATPMT, OTHER
		};
		
		public static final int CT_REPLACE = 1;
		public static final int CT_DELETE = 2;

		public EType type;
		public Date postDate;
		public Date initDate;
		public Date availDate;
		public double amt;
		public String transID;
		public String correctsID;
		public int correctType;	// CT_*
		public String servTransID;
		public String checkNum;
		public String refNum;
		public String sic;
		public String payeeID;
		public String name;
		public Payee payee;			// name OR payee
		public String extendedName;
		public ServiceAcctName bankName;
		public ServiceAcctName ccName;		// ccName OR bankName
		public String memo;
		//public something imageData;
		public CurrencyBlock currency;
		public CurrencyBlock origCurrency;	// origCurrency OR currency
		//public something inv401Ksource;
		
		public Transaction(TransferObject in)
		{
			
		}
	}
	
	public StmtResp(TransferObject tran, TransferObject in)
	{
	}
}
