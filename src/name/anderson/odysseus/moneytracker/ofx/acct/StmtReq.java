package name.anderson.odysseus.moneytracker.ofx.acct;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.*;

public class StmtReq extends OfxMessageReq
{
	public ServiceAcctName name;
	public boolean includeTrans;
	//public boolean includeImage;
	public Date startDt;
	public Date endDt;
	
	public StmtReq()
	{
		super(MessageSet.BANK, "STMT");
	}

	@Override
	protected void populateRequest(TransferObject obj, float msgsetVer)
	{
		if(this.name != null)
		{
			TransferObject acctFrom = new TransferObject("BANKACCTFROM");
			name.populateRequest(obj, msgsetVer);
			obj.put(acctFrom);
		}
		
		if(this.includeTrans)
		{
			TransferObject incTrans = new TransferObject("INCTRAN");
			if(this.startDt != null) incTrans.put("DTSTART", this.startDt);
			if(this.endDt != null) incTrans.put("DTEND", this.endDt);
			incTrans.put("INCLUDE", true);
			obj.put(incTrans);
		}
		
		//if(this.includeImage) obj.put("INCTRANIMG", true);
	}

	@Override
	public OfxMessageResp processResponse(TransferObject tran, TransferObject obj)
	{
		return new StmtResp(tran, obj);
	}
}
