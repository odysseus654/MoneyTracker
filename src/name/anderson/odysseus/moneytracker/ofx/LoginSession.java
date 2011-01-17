package name.anderson.odysseus.moneytracker.ofx;

import java.util.Date;
import name.anderson.odysseus.moneytracker.ofx.prof.SignonRealm;

public class LoginSession
{
	public OfxProfile profile;
	public SignonRealm realm;
	public int ID;
	
	// specified by user
	public String userid;
	public String userpass;
	public String userCred1;
	public String userCred2;
	public String authToken;

	// session keys
	public String sessionkey;
	public String mfaAnswerKey;
	public String sessionCookie;
	public Date sessionExpire;
}
