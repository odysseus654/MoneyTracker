package name.anderson.odysseus.moneytracker.ofx.cc;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;

public class CcServiceAcctInfo extends ServiceAcctInfo
{
	public ServiceAcctName name;
//	public boolean detailAvail;
//	public boolean xferSource;
//	public boolean xferDest;
//	public int status;

	public CcServiceAcctInfo(TransferObject in)
	{
		super(in);
		
		TransferObject sub = in.getObj("CCACCTFROM");
		if(sub != null) this.name = new ServiceAcctName(ServiceAcctName.ServiceType.CC, sub);
	}
}
