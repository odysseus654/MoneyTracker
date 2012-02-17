package name.anderson.odysseus.moneytracker.acct;

import java.util.*;

public class Transaction
{
	public int ID;
	public Account acct;
	public String type;
	public Date postDate;
	public Date initDate;
	public Date availDate;
	public double amt;
	public String transID;
	public String servTransID;
	public Map<String,Object> attrs;
	
	public Transaction(Account a)
	{
		ID = 0;
		acct = a;
		attrs = new TreeMap<String,Object>();
	}
}
