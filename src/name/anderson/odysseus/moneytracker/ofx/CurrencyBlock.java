package name.anderson.odysseus.moneytracker.ofx;

import java.util.Currency;

public class CurrencyBlock
{
	public Currency currency;
	public double rate;
	
	public CurrencyBlock()
	{
	}

	public CurrencyBlock(TransferObject in)
	{
		this.currency = Currency.getInstance(in.getAttr("CURSYM"));
		this.rate = Double.parseDouble(in.getAttr("CURRATE"));
	}
}
