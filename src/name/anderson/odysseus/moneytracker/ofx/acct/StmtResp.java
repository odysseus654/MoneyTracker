package name.anderson.odysseus.moneytracker.ofx.acct;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName.ServiceType;

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
		public Payee payee;					// name OR payee
		public String extendedName;
		public ServiceAcctName destName;	// ccName OR bankName
		public String memo;
		//public something imageData;
		public CurrencyBlock currency;		// origCurrency OR currency
		//public something inv401Ksource;
		
		public Transaction(TransferObject in)
		{
			String attr = in.getAttr("TRNTYPE");
			if(attr != null)
			{
				this.type = EType.valueOf(attr);
			}
			attr = in.getAttr("DTPOSTED");
			if(attr != null)
			{
				this.postDate = TransferObject.parseDate(attr);
			}
			attr = in.getAttr("DTUSER");
			if(attr != null)
			{
				this.initDate = TransferObject.parseDate(attr);
			}
			attr = in.getAttr("DTAVAIL");
			if(attr != null)
			{
				this.availDate = TransferObject.parseDate(attr);
			}
			attr = in.getAttr("TRNAMT");
			if(attr != null)
			{
				this.amt = Double.parseDouble(attr);
			}
			this.transID = in.getAttr("FITID");
			this.correctsID = in.getAttr("CORRECTFITID");
			attr = in.getAttr("CORRECTACTION");
			this.correctType = 0;
			if(attr != null)
			{
				if(attr.equals("REPLACE"))
				{
					this.correctType = CT_REPLACE;
				}
				else if(attr.equals("DELETE"))
				{
					this.correctType = CT_DELETE;
				}
			}
			this.servTransID = in.getAttr("SRVRTID");
			this.checkNum = in.getAttr("CHECKNUM");
			this.refNum = in.getAttr("REFNUM");
			this.sic = in.getAttr("SIC");
			this.payeeID = in.getAttr("PAYEEID");
			this.name = in.getAttr("NAME");
			TransferObject child = in.getObj("PAYEE");
			if(child != null)
			{
				this.payee = new Payee(child);
			}
			this.extendedName = in.getAttr("EXTDNAME");
			child = in.getObj("BANKACCTTO");
			if(child != null)
			{
				this.destName = new ServiceAcctName(ServiceType.BANK, child);
			} else {
				child = in.getObj("CCACCTTO");
				if(child != null)
				{
					this.destName = new ServiceAcctName(ServiceType.CC, child);
				}
			}
			this.memo = in.getAttr("MEMO");
			// IMAGEDATA/
			child = in.getObj("CURRENCY");
			if(child == null) child = in.getObj("ORIGCURRENCY");
			if(child != null)
			{
				this.currency = new CurrencyBlock(child);
			}
			// INV401KSOURCE
		}
	}
	
	public StmtResp(TransferObject tran, TransferObject in)
	{
		this.curDef = in.getAttr("CURDEF");
		TransferObject child = in.getObj("BANKACCTFROM");
		if(child != null)
		{
			this.name = new ServiceAcctName(ServiceType.BANK, child);
		}
		child = in.getObj("BANKTRANLIST");
		if(child != null)
		{
			this.startDt = TransferObject.parseDate(in.getAttr("DTSTART"));
			this.endDt = TransferObject.parseDate(in.getAttr("DTEND"));
			for(TransferObject.ObjValue stmtList : child.members)
			{
				TransferObject stmt = stmtList.child;
				if(stmt != null && stmt.name.equals("STMTTRN"))
				{
					if(this.transList == null)
					{
						this.transList = new LinkedList<Transaction>();
					}
					this.transList.add(new Transaction(stmt));
				}
			}
		}
		child = in.getObj("LEDGERBAL");
		if(child != null)
		{
			this.ledgerBal = Double.parseDouble(in.getAttr("BALAMT"));
			this.ledgerBalDate = TransferObject.parseDate(in.getAttr("DTASOF"));
		}
		child = in.getObj("AVAILBAL");
		if(child != null)
		{
			this.availBal = Double.parseDouble(in.getAttr("BALAMT"));
			this.availBalDate = TransferObject.parseDate(in.getAttr("DTASOF"));
		}
		child = in.getObj("BALLIST");
		if(child != null)
		{
			this.balList = new LinkedList<BalanceResponse>();
			for(TransferObject.ObjValue balList : child.members)
			{
				TransferObject bal = balList.child;
				if(bal != null && bal.name.equals("BAL"))
				{
					this.balList.add(new BalanceResponse(bal));
				}
			}
		}
		marketInfo = in.getAttr("MKTGINFO");
	}
}
