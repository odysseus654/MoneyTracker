package name.anderson.odysseus.moneytracker.acct;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;

public class Account
{
	public int    ID;
	public ServiceAcctInfo service;
	public int    serviceId;
	public String name;
	public Date   lastUpdate;
	public double curBalAmt;
	public Date   curBalDate;
	public double availBalAmt;
	public Date   availBalDate;
	public Date   lastTrans;
}
