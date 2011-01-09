package name.anderson.odysseus.moneytracker.ofx;

public class FiDescr
{
	public String FIName;
	public String Addr1;
	public String Addr2;
	public String Addr3;
	public String City;
	public String State;
	public String PostalCode;
	public String Country;
	public String CSPhone;
	public String TSPhone;
	public String FaxPhone;
	public String URL;
	public String Email;
	
	public FiDescr()
	{
	}
	
	public FiDescr(TransferObject in)
	{
		this.FIName = in.getAttr("FINAME");
		this.Addr1 = in.getAttr("ADDR1");
		this.Addr2 = in.getAttr("ADDR2");
		this.Addr3 = in.getAttr("ADDR3");
		this.City = in.getAttr("CITY");
		this.State = in.getAttr("STATE");
		this.PostalCode = in.getAttr("POSTALCODE");
		this.Country = in.getAttr("COUNTRY");
		this.CSPhone = in.getAttr("CSPHONE");
		this.TSPhone = in.getAttr("TSPHONE");
		this.FaxPhone = in.getAttr("FAXPHONE");
		this.URL = in.getAttr("URL");
		if(this.URL == null) this.URL = in.getAttr("URL2");
		this.Email = in.getAttr("EMAIL");
	}
}
