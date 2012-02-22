package name.anderson.odysseus.moneytracker.prof;

import java.net.*;
import java.util.List;
import javax.net.ssl.*;
import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;
import com.github.ignition.core.tasks.IgnitedAsyncTask;
import name.anderson.odysseus.moneytracker.DisconDialog;
import name.anderson.odysseus.moneytracker.DisconProgress;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class Login extends Activity
{
	private LoginTask loginTask;
	private OfxProfile profile;
	private SignonRealm realm;
	//private SignonMsgReq lastAttempt;
	private int sessionId;
	private final static int GET_REALM = 1011;

	private boolean requireAuthToken;	// set in response to STATUS_AUTHTOKEN_REQUIRED
	
	CharSequence userid;
	CharSequence userpass;
	CharSequence userCred1;
	CharSequence userCred2;
	CharSequence authToken;
	private String sessionCookie;
	
	private DialogInterface.OnClickListener dismissOnOk = new DialogInterface.OnClickListener()
	{
		public void onClick(DialogInterface dialog, int which)
		{
			cancel();
		}
	};

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
*/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Bundle parms = getIntent().getExtras();
		int fi_id = parms.getInt("prof_id");
		this.sessionId = parms.getInt("sess_id");
		String realmName = parms.getString("login_realm");
		
		if(savedInstanceState != null)
		{
			realmName = savedInstanceState.getString("login_realm");
		}
		
		Object passthrough = getLastNonConfigurationInstance();
		if(passthrough != null)
		{
			loginTask = (LoginTask)passthrough;
			loginTask.connect(this);
		}

		LoginSession session = null;
		if(profile == null)
		{
			ProfileTable db = new ProfileTable(this);
			try
			{
				db.openReadable();
				if(this.sessionId != 0)
				{
					session = db.getSession(this.sessionId);
					profile = session.profile;
				} else {
					profile = db.getProfile(fi_id);
				}
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
		}
		
		if(savedInstanceState != null)
		{
			if(this.sessionId == 0) this.sessionId = savedInstanceState.getInt("sessionId");
			this.requireAuthToken = savedInstanceState.getBoolean("requireAuthToken");
			this.userid = savedInstanceState.getString("userid");
			this.userpass = savedInstanceState.getString("userpass");
			this.userCred1 = savedInstanceState.getString("userCred1");
			this.userCred2 = savedInstanceState.getString("userCred2");
			this.authToken = savedInstanceState.getString("authToken");
			this.sessionCookie = savedInstanceState.getString("sessionCookie");
			realmName = savedInstanceState.getString("realmName");
		}
		else if(session != null)
		{
			this.userid = session.userid;
			this.userpass = session.userpass;
			this.userCred1 = session.userCred1;
			this.userCred2 = session.userCred2;
			this.authToken = session.authToken;
			this.sessionCookie = session.sessionCookie;
//			public String sessionkey;
//			public String mfaAnswerKey;
		}
		
		if(this.realm == null && profile.realms != null)
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
				Intent realmProf = new Intent(this, RealmLogin.class);
				realmProf.putExtra("prof_id", profile.ID);
				startActivityForResult(realmProf, GET_REALM);
				return;
			}
		}

		buildView();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == GET_REALM)
		{
			if(resultCode == RESULT_CANCELED)
			{
				cancel();
				return;
			} else {
				String realmName = data.getStringExtra("login_realm");
				if(realmName != null && profile.realms.containsKey(realmName))
				{
					this.realm = profile.realms.get(realmName);
				} else {
					Utilities.buildAlert(this, null, "Unexpected bad realm name received", "Internal Error", dismissOnOk);
					return;
				}
				buildView();
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt("sessionId", this.sessionId);
		outState.putBoolean("requireAuthToken", this.requireAuthToken);
		outState.putString("userid", this.userid.toString());
		outState.putString("userpass", this.userpass.toString());
		outState.putString("userCred1", this.userCred1.toString());
		outState.putString("userCred2", this.userCred2.toString());
		outState.putString("authToken", this.authToken.toString());
		outState.putString("sessionCookie", this.sessionCookie);
		if(this.realm != null) outState.putString("realmName", this.realm.name);
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

	@Override
	public Object onRetainNonConfigurationInstance()
	{
		// we leverage this method to "tunnel" the task object through to the next
		// incarnation of this activity in case of a configuration change
		return loginTask;
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		// always disconnect the activity from the task here, in order to not risk
		// leaking a context reference
		loginTask.disconnect();
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
	
	private void completeLoginRequest(SignonMsgReq son)
	{
		son.reqUserkey = true;
		if(realm != null && realm.clientUidReq)
		{
			son.clientUid = Settings.Secure.ANDROID_ID;
		}
		son.sessCookie = this.sessionCookie;
		//public String accessKey;	// session: mfa response
	}
	
	private void attemptLogin()
	{
		loginTask = new LoginTask(this);
		SignonMsgReq son = buildLoginRequest();
		completeLoginRequest(son);
		loginTask.execute(son);
	}
	
	private void loginSuccessful()
	{
		Intent i = getIntent();
		i.putExtra("sess_id", this.sessionId);
		setResult(RESULT_OK, i);
		finish();
	}
/*
	public String userkey;		// session: login response
	public String accessKey;	// session: mfa response
	public String sessCookie;	// session: tracking
	public List<MfaChallenge> mfaChallenges;
*/	
	private static class LoginTask
		extends IgnitedAsyncTask<Login, SignonMsgReq, Integer, Integer>
	{
		private OfxProfile profile;
		private SignonRealm realm;
		private int sessionId;
		private LoginSession session;
		private DisconProgress prog;

		public LoginTask(Login context)
		{
			super(context);
			profile = context.profile;
			realm = context.realm;
			sessionId = context.sessionId;
		}
		
		@Override
		public void connect(Login context)
		{
			super.connect(context);
			if(prog != null) prog.connect(context);
			context.profile = profile;
			context.realm = realm;
			context.sessionId = sessionId;
		}

		@Override
		public void disconnect()
		{
			super.disconnect();
			if(prog != null) prog.disconnect();
		}
		
		static private class ProgCancelListener implements DisconDialog.OnCancelListener
		{
			public void onCancel(Context ctx, DialogInterface dialog)
			{
				if(ctx != null) ((SelectProfile)ctx).cancel();
			}
		}
		
		private LoginSession buildLoginSession(SignonMsgReq son, SignonMsgResp resp)
		{
			LoginSession req = new LoginSession();
			req.profile = this.profile;
			req.realm = this.realm;
			req.userid = son.userid;
			req.userpass = son.userpass;
			req.userCred1 = son.userCred1;
			req.userCred2 = son.userCred2;
			req.authToken = son.authToken;
			req.ID = this.sessionId;
			req.sessionkey = resp.userKey;
			req.sessionExpire = resp.tsKeyExpire;
			req.sessionCookie = resp.sessCookie;
			req.mfaAnswerKey = resp.accessKey;
			req.sessionCookie = resp.sessCookie;
			return req;
		}

		@Override
		protected void onStart(Login context)
		{
			prog = new DisconProgress(context);
			prog.setMessage(R.string.login_progress);
			prog.setIndeterminate(false);
			prog.setCancelable(true);
			prog.setOnCancelListener(new ProgCancelListener());
			prog.show();
		}

		@Override
		public Integer run(SignonMsgReq... parm) throws Exception
		{
			SignonMsgReq son = parm[0];
	
			OfxRequest req = profile.newRequest();
			req.addRequest(son);
			req.addRequest(profile.newProfRequest(profile.profileIsUser));
	
			List<OfxMessageResp> response = req.submit(this);
	
			if(response != null)
			{
				for(OfxMessageResp resp : response)
				{
					if(resp instanceof SignonMsgResp)
					{
						session = buildLoginSession(son, (SignonMsgResp)resp);
					}
					else if(resp instanceof ProfileMsgResp)
					{
						profile.mergeProfileResponse(req.version, (ProfileMsgResp)resp, true);
					}
				}
				if(session != null)
				{
					ProfileTable db = new ProfileTable(this);
					try
					{
						db.openWritable();
						db.pushSession(session);
						this.sessionId = session.ID;
					}
					finally
					{
						db.close();
					}
				}
			}
		}

		@Override
		protected void onCompleted(Login context, Integer result)
		{
			prog.hide();
			context.sessionId = sessionId;
		}

		private static class EmptyClickListener implements DialogInterface.OnClickListener
		{
			public void onClick(DialogInterface dialog, int which) { }
		};
		private EmptyClickListener emptyClickListener = new EmptyClickListener();

		private void doAlert(Context ctx, Exception e, String msg)
		{
			AlertDialog dlg = Utilities.buildAlert(ctx, e, msg, "Login Error", emptyClickListener);
			dlg.show();
		}

		private void doRetryableAlert(final Context ctx, Exception e, String msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
			dialog.setTitle(msg);
			String dispMsg = msg + "\n\n" + e.getMessage();
			dialog.setMessage(dispMsg);
			dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					((Login)ctx).attemptLogin();
				}
			});
			dialog.setNegativeButton("Cancel", emptyClickListener);
			
			AlertDialog dlg = dialog.create();
			dlg.show();
		}

		@Override
		protected void onError(Login context, Exception error)
		{
			if(error instanceof HttpResponseException)
			{
				doAlert(context, error, OfxProfile.exceptionComment(error));
			}
			else if(error instanceof OfxError)
			{
				OfxError e = (OfxError)error;
				switch(e.getErrorCode())
				{
				//case StatusResponse.STATUS_ERROR: // General error (ERROR)
				//case StatusResponse.STATUS_MFA_REQUIRED: // User credentials are correct, but further authentication required (ERROR)
				//case StatusResponse.STATUS_MFA_INVALID: // MFACHALLENGEA contains invalid information (ERROR)
				case StatusResponse.STATUS_FI_INVALID: // <FI> Missing or Invalid in <SONRQ> (ERROR)
					doAlert(context, e, "Server is rejecting connection details (FI_ID or FI_ORG)");
					break;
				//case StatusResponse.STATUS_PINCH_NEEDED: // Must change USERPASS (INFO)
				case StatusResponse.STATUS_BAD_LOGIN: // Signon invalid (see section 2.5.1) (ERROR)
					doAlert(context, e, "User and/or password is incorrect");
					break;
				case StatusResponse.STATUS_ACCT_BUSY: // Customer account already in use (ERROR)
					doAlert(context, e, "Your account is currently in use");
					break;
				case StatusResponse.STATUS_ACCT_LOCKED: // USERPASS Lockout (ERROR)
					doAlert(context, e, "Your account has been locked");
					break;
				//case StatusResponse.STATUS_EMPTY_REQUEST: // Empty signon transaction not supported (ERROR)
				//case StatusResponse.STATUS_PINCH_REQUIRED: // Signon invalid without supporting pin change request (ERROR)
				//case StatusResponse.STATUS_CLIENTUID_REJECTED: // CLIENTUID error (ERROR)
				case StatusResponse.STATUS_CALL_US: // User should contact financial institution (ERROR)
					doAlert(context, e, "Please contact your financial institution");
					break;
				case StatusResponse.STATUS_AUTHTOKEN_REQUIRED: // OFX server requires AUTHTOKEN in signon during the next session (ERROR)
					{
						context.requireAuthToken = true;
						context.buildView();
						TextView tokenView = (TextView)context.findViewById(R.id.TokenPrompt);
						tokenView.setVisibility(View.VISIBLE);
						tokenView.setText(realm.authTokenLabel);
						tokenView.setFocusableInTouchMode(true);
						tokenView.requestFocus();
						//tokenView.requestRectangleOnScreen(viewRectangle);
						break;
					}
				case StatusResponse.STATUS_AUTHTOKEN_INVALID:// AUTHTOKEN invalid (ERROR)
					doAlert(context, e, "\"" + realm.authTokenLabel + "\" is invalid");
					break;
				default:
					doAlert(context, e, "Server refused the login");
					break;
				}
			}
			else if(error instanceof XmlPullParserException ||
					error instanceof SSLPeerUnverifiedException)
			{
				doAlert(context, error, OfxProfile.exceptionComment(error));
			}
			else if(error instanceof ClientProtocolException ||
					error instanceof ConnectException ||
					error instanceof SocketException || 
					error instanceof SSLException)
			{
				doRetryableAlert(context, error, "Unable to connect to server");
			}
			else
			{
				doAlert(context, error, OfxProfile.exceptionComment(error));
			}
		}

		@Override
		protected void onSuccess(Login context, Integer result)
		{
			if(session == null)
			{
				doAlert(context, null, "Server gave an unexpected response (no signon acknowledgement?)");
			} else {
				context.loginTask = null;
				context.loginSuccessful();
				disconnect();
			}
		}
	}
}
