package name.anderson.odysseus.moneytracker.ofx.prof;

import name.anderson.odysseus.moneytracker.ofx.*;

public class SignonRealm
{
//	public enum CharType { Alpha, Numeric, AlphaOrNumeric, AlphaAndNumeric };
//	public enum PassType { Fixed, OneTime, HardwareToken };
	public static final int CT_ALPHA = 1;
	public static final int CT_NUMERIC = 2;
	public static final int CT_ALORNUM = 3;
	public static final int CT_ALANDNUM = 4;
	
	public static final int PT_FIXED = 0;
	public static final int PT_ONETIME = 1;
	public static final int PT_HWTOKEN = 2;
	
	public String name;
	public int minChars;
	public int maxChars;
	public int chartype;
	public boolean caseSensitive;
	public boolean specialAllowed;
	public boolean spacesAllowed;
	public boolean changePassAllowed;
	public boolean changePassFirst;
	public int passType;
	
	public String userCred1Label;
	public String userCred2Label;
	public boolean clientUidReq;
	public boolean authTokenFirst;
	public String authTokenLabel;
	public String authTokenInfoURL;
	public boolean mfaSupported;
	public boolean mfaFirst;

	public SignonRealm()
	{
	}

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
				this.chartype = CT_ALPHA;
			}
			else if(strVal.equals("NUMERICONLY"))
			{
				this.chartype = CT_NUMERIC;
			}
			else if(strVal.equals("ALPHAORNUMERIC"))
			{
				this.chartype = CT_ALORNUM;
			}
			else if(strVal.equals("ALPHAANDNUMERIC"))
			{
				this.chartype = CT_ALANDNUM;
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
				this.passType = PT_FIXED;
			}
			else if(strVal.equals("ONETIME"))
			{
				this.passType = PT_ONETIME;
			}
			else if(strVal.equals("HWTOKEN"))
			{
				this.passType = PT_HWTOKEN;
			}
		}
		
		this.userCred1Label = in.getAttr("USERCRED1LABEL");
		this.userCred2Label = in.getAttr("USERCRED2LABEL");
		this.authTokenLabel = in.getAttr("AUTHTOKENLABEL");
		this.authTokenInfoURL = in.getAttr("AUTHTOKENINFOURL");
		
		strVal = in.getAttr("CLIENTUIDREQ");
		this.clientUidReq = (strVal != null && strVal.equals("Y"));

		strVal = in.getAttr("AUTHTOKENFIRST");
		this.authTokenFirst = (strVal != null && strVal.equals("Y"));

		strVal = in.getAttr("MFACHALLENGESUPT");
		this.mfaSupported = (strVal != null && strVal.equals("Y"));

		strVal = in.getAttr("MFACHALLENGEFIRST");
		this.mfaFirst = (strVal != null && strVal.equals("Y"));
	}
}
