package name.anderson.odysseus.moneytracker.ofx;

import java.util.Date;

public class BalanceResponse
{
	public static final int BT_DOLLAR = 1;
	public static final int BT_PERCENT = 2;
	public static final int BT_NUMBER = 3;

	public String name;
	public String descr;
	public int baltype;
	public double value;
	public Date effectiveDate;
	public CurrencyBlock currency;
	// currency
	
	public BalanceResponse()
	{
	}
	
	public BalanceResponse(TransferObject in)
	{
		this.name = in.getAttr("NAME");
		this.descr = in.getAttr("DESC");
		
		String balType = in.getAttr("BALTYPE");
		if(balType != null)
		{
			if(balType.equals("DOLLAR"))
			{
				this.baltype = BT_DOLLAR; 
			}
			else if(balType.equals("PERCENT"))
			{
				this.baltype = BT_PERCENT;
			}
			else if(balType.equals("NUMBER"))
			{
				this.baltype = BT_NUMBER;
			}
		}
		
		String strValue = in.getAttr("VALUE");
		this.value = Double.parseDouble(strValue);
		
		String strEffDate = in.getAttr("DTASOF");
		if(strEffDate != null) this.effectiveDate = TransferObject.parseDate(strEffDate);
		
		TransferObject child = in.getObj("CURRENCY");
		if(child != null)
		{
			this.currency = new CurrencyBlock(child);
		}
	}
}
