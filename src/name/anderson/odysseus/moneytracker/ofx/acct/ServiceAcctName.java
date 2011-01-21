package name.anderson.odysseus.moneytracker.ofx.acct;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;

public class ServiceAcctName
{
	public enum ServiceType { BANK, CC, LOAN };
	
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
}
