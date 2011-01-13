package name.anderson.odysseus.moneytracker.prof;

import java.util.List;

import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.OfxProfile;
import name.anderson.odysseus.moneytracker.ofx.ProfileTable;
import name.anderson.odysseus.moneytracker.ofx.prof.SignonRealm;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgReq;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgReq.MfaChallenge;
import android.app.*;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class Login extends Activity
{
	private OfxProfile profile;
	private SignonRealm realm;
	private SignonMsgReq lastAttempt;

	private boolean requireAuthToken;	// set in response to STATUS_AUTHTOKEN_REQUIRED
	
	CharSequence userid;
	CharSequence userpass;
	CharSequence userCred1;
	CharSequence userCred2;
	CharSequence authToken;
	
/*
	public boolean reqUserkey;	// session: request
	public String userkey;		// session: login response
	public String accessKey;	// session: mfa response
	public String sessCookie;	// session: tracking
	public String clientUid;		// SIGNONINFO: CLIENTUIDREQ

 * 	public String userid;		// auth: always
	public String userpass;		// auth: always
	public String userCred1;		// SIGNONINFO: USERCRED1LABEL
	public String userCred2;		// SIGNONINFO: USERCRED2LABEL
	public String authToken;		// REALM: AUTHTOKENFIRST (AUTHTOKENLABEL,AUTHTOKENURL) or Error 15512 or PWTYPE in (ONETIME,HWTOKEN)
	public List<MfaChallenge> mfaChallenges;

	int STATUS_SUCCESS = 0;					Success (INFO)
	int STATUS_ERROR = 2000;				General error (ERROR)
	int STATUS_MFA_REQUIRED = 3000;			User credentials are correct, but further authentication required (ERROR)
											This notifies client to send <MFACHALLENGERQ>.
	int STATUS_MFA_INVALID = 3001;			MFACHALLENGEA contains invalid information (ERROR)
	int STATUS_FI_INVALID = 13504;			<FI> Missing or Invalid in <SONRQ> (ERROR)
	int STATUS_PINCH_NEEDED = 15000;		Must change USERPASS (INFO)
	int STATUS_BAD_LOGIN = 15500;			Signon invalid (see section 2.5.1) (ERROR)
	int STATUS_ACCT_BUSY = 15501;			Customer account already in use (ERROR)
	int STATUS_ACCT_LOCKED = 15502;			USERPASS Lockout (ERROR)
	int STATUS_EMPTY_REQUEST = 15506;		Empty signon transaction not supported (ERROR)
	int STATUS_PINCH_REQUIRED = 15507;		Signon invalid without supporting pin change request (ERROR)
	int STATUS_CLIENTUID_REJECTED = 15510;	CLIENTUID error (ERROR)
	int STATUS_CALL_US = 15511;				User should contact financial institution (ERROR)
	int STATUS_AUTHTOKEN_REQUIRED = 15512;	OFX server requires AUTHTOKEN in signon during the next session (ERROR)
	int STATUS_AUTHTOKEN_INVALID = 15513;	AUTHTOKEN invalid (ERROR)
*/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle parms = getIntent().getExtras();
		int fi_id = parms.getInt("prof_id");
		String realmName = parms.getString("login_realm");
		
		if(savedInstanceState != null)
		{
			realmName = savedInstanceState.getString("login_realm");
		}
		
		DialogInterface.OnClickListener dismissOnOk = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				cancel();
			}
		};

		ProfileTable db = new ProfileTable(this);
		try
		{
			db.open();
			profile = db.getProfile(fi_id);
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to retrieve profile", "Internal Error", dismissOnOk);
			dlg.show();
			return;
		}
		finally
		{
			db.close();
		}
		if(profile == null)
		{
			AlertDialog dlg = Utilities.buildAlert(this, null, "Could not find profile", "Internal Error", dismissOnOk);
			dlg.show();
			return;
		}
		
		if(profile.realms != null)
		{
			if(realmName != null && profile.realms.containsKey(realmName))
			{
				this.realm = profile.realms.get(realmName);
			}
			else if(profile.realms.size() == 1)
			{
				for(SignonRealm oneRealm : profile.realms.values())
				{
					this.realm = oneRealm;
				}
			}
			else if(profile.requiresRealmPrompt())
			{
				int _i = 0;
			}
		}

		buildView();
	}
	
	private void buildView()
	{
		EditText edit = (EditText)findViewById(R.id.UserEdit);
		edit.setText(userid);
		edit.setOnKeyListener(new View.OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				userid = ((EditText)v).getText();
				return false;
			}
		});
		edit = (EditText)findViewById(R.id.PassEdit);
		edit.setText(userpass);
		edit.setOnKeyListener(new View.OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				userpass = ((EditText)v).getText();
				return false;
			}
		});

		if(this.realm != null && !profile.fidef.simpleProf && this.realm.userCred1Label != null)
		{
			((TextView)findViewById(R.id.Cred1Prompt)).setText(this.realm.userCred1Label);
			edit = (EditText)findViewById(R.id.Cred1Edit);
			edit.setText(userCred1);
			edit.setOnKeyListener(new View.OnKeyListener()
			{
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					userCred1 = ((EditText)v).getText();
					return false;
				}
			});
		} else {
			findViewById(R.id.Cred1Block).setVisibility(View.GONE);
		}
		if(this.realm != null && !profile.fidef.simpleProf && this.realm.userCred2Label != null)
		{
			((TextView)findViewById(R.id.Cred2Prompt)).setText(this.realm.userCred2Label);
			edit = (EditText)findViewById(R.id.Cred2Edit);
			edit.setText(userCred2);
			edit.setOnKeyListener(new View.OnKeyListener()
			{
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					userCred2 = ((EditText)v).getText();
					return false;
				}
			});
		} else {
			findViewById(R.id.Cred2Block).setVisibility(View.GONE);
		}
		boolean bTokenPrompt = true;
		if(this.realm != null && !profile.fidef.simpleProf && this.realm.passType != SignonRealm.PT_FIXED)
		{
			String prompt = null;
			switch(this.realm.passType)
			{
			case SignonRealm.PT_HWTOKEN:
				prompt = getString(R.string.login_pwtype_hwtoken);
				break;
			case SignonRealm.PT_ONETIME:
				prompt = getString(R.string.login_pwtype_onetime);
				break;
			}
			((TextView)findViewById(R.id.TokenPrompt)).setText(prompt);
		}
		else if(this.realm != null && !profile.fidef.simpleProf && this.realm.authTokenLabel != null && (this.requireAuthToken || this.realm.authTokenFirst))
		{
			((TextView)findViewById(R.id.TokenPrompt)).setText(this.realm.authTokenLabel);
		}
		else
		{
			bTokenPrompt = false;
			findViewById(R.id.TokenBlock).setVisibility(View.GONE);
		}
		if(bTokenPrompt)
		{
			edit = (EditText)findViewById(R.id.TokenEdit);
			edit.setText(authToken);
			edit.setOnKeyListener(new View.OnKeyListener()
			{
				public boolean onKey(View v, int keyCode, KeyEvent event)
				{
					authToken = ((EditText)v).getText();
					return false;
				}
			});
		}
		((Button)findViewById(R.id.OkButton)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				attemptLogin();
			}
		});

		((Button)findViewById(R.id.CancelButton)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				cancel();
			}
		});
	}

	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}
	
	private SignonMsgReq buildLoginRequest()
	{
		SignonMsgReq req = profile.createAnonymousSignon();
		req.userid = (userid == null) ? null : userid.toString();
		req.userpass = (userpass == null) ? null : userpass.toString();
		req.userCred1 = (userCred1 == null) ? null : userCred1.toString();
		req.userCred2 = (userCred2 == null) ? null : userCred2.toString();
		req.authToken = (authToken == null) ? null : authToken.toString();
		return req;
	}
	
/*
	public boolean reqUserkey;	// session: request
	public String userkey;		// session: login response
	public String accessKey;	// session: mfa response
	public String sessCookie;	// session: tracking
	public String clientUid;		// SIGNONINFO: CLIENTUIDREQ

	public List<MfaChallenge> mfaChallenges;
*/
	
	void attemptLogin()
	{
		int _i = 0;
	}

}
