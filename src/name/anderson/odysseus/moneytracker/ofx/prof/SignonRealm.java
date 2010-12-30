package name.anderson.odysseus.moneytracker.ofx.prof;

import name.anderson.odysseus.moneytracker.ofx.*;

public class SignonRealm
{
	public enum CharType { Alpha, Numeric, AlphaOrNumeric, AlphaAndNumeric };
	public enum PassType { Fixed, OneTime, HardwareToken };
	
	public String name;
	public int minChars;
	public int maxChars;
	public CharType chartype;
	public boolean caseSensitive;
	public boolean specialAllowed;
	public boolean spacesAllowed;
	public boolean changePassAllowed;
	public boolean changePassFirst;
	public PassType passType;
/*
	public SignonRealm()
	{
	}
*/
	public SignonRealm(TransferObject in)
	{
		this.name = in.getAttr("SIGNONREALM");
		this.minChars = Integer.parseInt(in.getAttr("MIN"));
		this.maxChars = Integer.parseInt(in.getAttr("MAX"));
		
		String strVal = in.getAttr("CHARTYPE");
		if(strVal != null)
		{
			if(strVal.equals("ALPHAONLY"))
			{
				this.chartype = CharType.Alpha;
			}
			else if(strVal.equals("NUMERICONLY"))
			{
				this.chartype = CharType.Numeric;
			}
			else if(strVal.equals("ALPHAORNUMERIC"))
			{
				this.chartype = CharType.AlphaOrNumeric;
			}
			else if(strVal.equals("ALPHAANDNUMERIC"))
			{
				this.chartype = CharType.AlphaAndNumeric;
			}
		}

		strVal = in.getAttr("CASESEN");
		this.caseSensitive = (strVal != null && strVal.equals("Y"));

		strVal = in.getAttr("SPECIAL");
		this.specialAllowed = !(strVal != null && strVal.equals("N"));

		strVal = in.getAttr("SPACES");
		this.spacesAllowed = !(strVal != null && strVal.equals("N"));

		strVal = in.getAttr("PINCH");
		this.changePassAllowed = (strVal != null && strVal.equals("Y"));

		strVal = in.getAttr("CHGPINFIRST");
		this.changePassFirst = !(strVal != null && strVal.equals("N"));

		strVal = in.getAttr("PWTYPE");
		if(strVal != null)
		{
			if(strVal.equals("FIXED"))
			{
				this.passType = PassType.Fixed;
			}
			else if(strVal.equals("ONETIME"))
			{
				this.passType = PassType.OneTime;
			}
			else if(strVal.equals("HWTOKEN"))
			{
				this.passType = PassType.HardwareToken;
			}
		}
	}
}
