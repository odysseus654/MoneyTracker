package name.anderson.odysseus.moneytracker.ofx.acct;

import name.anderson.odysseus.moneytracker.ofx.OfxMessageReq;
import name.anderson.odysseus.moneytracker.ofx.TransferObject;

public class ServiceAcctName implements Comparable<ServiceAcctName>
{
	public enum ServiceType { BANK, CC, LOAN };
	public static final OfxMessageReq.MessageSet[] MSG_MAP
		= { OfxMessageReq.MessageSet.BANK, OfxMessageReq.MessageSet.CREDITCARD, OfxMessageReq.MessageSet.LOAN };
	
	public ServiceType type;
	public String bankId;
	public String branchId;
	public String acctId;
	public String acctType;
	public String acctKey;
	
	public ServiceAcctName(ServiceType t, TransferObject in)
	{
		this.type = t;
		switch(this.type)
		{
		case BANK:
			this.bankId = in.getAttr("BANKID");
			this.branchId = in.getAttr("BRANCHID");
			this.acctType = in.getAttr("ACCTTYPE");
		case CC:
			this.acctId = in.getAttr("ACCTID");
			this.acctKey = in.getAttr("ACCTKEY");
			break;
		case LOAN:
			this.acctId = in.getAttr("LOANACCTID");
			this.acctType = in.getAttr("LOANACCTTYPE");
			break;
		}
	}

	public ServiceAcctName()
	{
	}

	public void populateRequest(TransferObject obj, float msgsetVer)
	{
		switch(this.type)
		{
		case BANK:
			obj.put("BANKID", this.bankId);
			if(this.branchId != null) obj.put("BRANCHID", this.branchId);
			obj.put("ACCTID", this.acctId);
			obj.put("ACCTTYPE", this.acctType);
			if(this.acctKey != null) obj.put("ACCTKEY", this.acctKey);
			break;
		case CC:
			obj.put("ACCTID", this.acctId);
			if(this.acctKey != null) obj.put("ACCTKEY", this.acctKey);
			break;
		case LOAN:
			obj.put("LOANACCTID", this.acctId);
			obj.put("LOANACCTTYPE", this.acctType);
			break;
		}
	}
	
	private <T extends Comparable<T>> int compareObj(T left, T right)
	{
		if(left != null && right != null)
		{
			int test = left.compareTo(right);
			if(test != 0) return test;
		}
		else if(left != null || right != null)
		{
			return left == null ? -1 : +1;
		}
		return 0;
	}

	public int compareTo(ServiceAcctName another)
	{
		int test = compareObj(type, another.type);
		if(test != 0) return test;

		test = compareObj(bankId, another.bankId);
		if(test != 0) return test;

		test = compareObj(branchId, another.branchId);
		if(test != 0) return test;

		test = compareObj(acctId, another.acctId);
		if(test != 0) return test;

		test = compareObj(acctType, another.acctType);
		if(test != 0) return test;

		test = compareObj(acctKey, another.acctKey);
		if(test != 0) return test;

		return 0;
	}
}
