package name.anderson.odysseus.moneytracker.ofx.acct;

import name.anderson.odysseus.moneytracker.ofx.TransferObject;

public class Payee
{
	public String FIName;
	public String Addr1;
	public String Addr2;
	public String Addr3;
	public String City;
	public String State;
	public String PostalCode;
	public String Country;
	public String Phone;

	public Payee(TransferObject in)
	{
		this.FIName = in.getAttr("NAME");
		this.Addr1 = in.getAttr("ADDR1");
		this.Addr2 = in.getAttr("ADDR2");
		this.Addr3 = in.getAttr("ADDR3");
		this.City = in.getAttr("CITY");
		this.State = in.getAttr("STATE");
		this.PostalCode = in.getAttr("POSTALCODE");
		this.Country = in.getAttr("COUNTRY");
		this.Phone = in.getAttr("PHONE");
	}
}
