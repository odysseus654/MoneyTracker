package name.anderson.odysseus.moneytracker.ofx.signup;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;

public class ServiceAcctInfo
{
	public static final int STAT_AVAIL = 1;
	public static final int STAT_PEND = 2;
	public static final int STAT_ACTIVE = 3;
	
	public boolean detailAvail;
	public boolean xferSource;
	public boolean xferDest;
	public int status;

	public ServiceAcctInfo(TransferObject in)
	{
		String str = in.getAttr("SVCSTATUS");
		if(str != null)
		{
			if(str.equals("AVAIL"))
			{
				this.status = STAT_AVAIL;
			}
			else if(str.equals("PEND"))
			{
				this.status = STAT_PEND;
			}
			else if(str.equals("ACTIVE"))
			{
				this.status = STAT_ACTIVE;
			}
		}
		
		str = in.getAttr("XFERSRC");
		this.xferSource = (str != null && str.equals("Y"));
		
		str = in.getAttr("XFERDEST");
		this.xferDest = (str != null && str.equals("Y"));
		
		str = in.getAttr("SUPTXDL");
		this.detailAvail = (str != null && str.equals("Y"));
	}
}
