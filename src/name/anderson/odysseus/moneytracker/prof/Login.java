package name.anderson.odysseus.moneytracker.prof;

import java.net.*;
import java.util.List;
import javax.net.ssl.*;
import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class Login extends Activity implements Runnable
{
	ProgressDialog prog;
	private Thread queryThread;
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
		
		ProfileTable db = new ProfileTable(this);
		LoginSession session = null;
		try
		{
			db.open();
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
		
		if(savedInstanceState != null)
		{
			this.sessionId = savedInstanceState.getInt("sessionId");
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

	private void attemptLogin()
	{
		prog = ProgressDialog.show(this, null, getString(R.string.login_progress),
				false, true, new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				cancel();
			}
		});
		
		queryThread = new Thread(this, "Login Thread");
		queryThread.setDaemon(true);
		queryThread.start();
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
	private static final int QH_OK = 0;
	private static final int QH_ERR_STATUS = 1;
	private static final int QH_ERR_HTTP = 2;
	private static final int QH_ERR = 3;
	private static final int QH_ERR_TIMEOUT = 4;
	private static final int QH_ERR_CONN = 5;
	private static final int QH_ERR_SSL = 6;
	private static final int QH_ERR_SSL_VERIFY = 7;
	private static final int QH_ERR_OFX = 8;
	private static final int QH_ERR_PARSE = 9;
	private static final int QH_EMPTY = 10;
	
	private Handler queryHandler = new Handler()
	{
		private DialogInterface.OnClickListener emptyClickListener = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) { }
		};

		private void doAlert(Exception e, String msg)
		{
			AlertDialog dlg = Utilities.buildAlert(Login.this, e, msg, "Login Error", emptyClickListener);
			dlg.show();
		}

		private void doRetryableAlert(Exception e, String msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
			dialog.setTitle(msg);
			String dispMsg = msg + "\n\n" + e.getMessage();
			dialog.setMessage(dispMsg);
			dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					attemptLogin();
				}
			});
			dialog.setNegativeButton("Cancel", emptyClickListener);
			
			AlertDialog dlg = dialog.create();
			dlg.show();
		}

		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);

			try
			{
				prog.dismiss();
			}
			catch(IllegalArgumentException e)
			{	// this may happen due to race conditions on activity shutdown?
				e.printStackTrace();
			}
			switch(msg.what)
			{
			case QH_OK:
				loginSuccessful();
				break;
			case QH_EMPTY:
				doAlert(null, "Server gave an unexpected response (no signon acknowledgement?)");
				break;
			case QH_ERR_OFX:
				{
					OfxError e = (OfxError)msg.obj;
		        	switch(e.getErrorCode())
		        	{
					//case StatusResponse.STATUS_ERROR: // General error (ERROR)
					//case StatusResponse.STATUS_MFA_REQUIRED: // User credentials are correct, but further authentication required (ERROR)
					//case StatusResponse.STATUS_MFA_INVALID: // MFACHALLENGEA contains invalid information (ERROR)
		        	case StatusResponse.STATUS_FI_INVALID: // <FI> Missing or Invalid in <SONRQ> (ERROR)
						doAlert(e, "Server is rejecting connection details (FI_ID or FI_ORG)");
						break;
					//case StatusResponse.STATUS_PINCH_NEEDED: // Must change USERPASS (INFO)
					case StatusResponse.STATUS_BAD_LOGIN: // Signon invalid (see section 2.5.1) (ERROR)
						doAlert(e, "User and/or password is incorrect");
						break;
					case StatusResponse.STATUS_ACCT_BUSY: // Customer account already in use (ERROR)
						doAlert(e, "Your account is currently in use");
						break;
					case StatusResponse.STATUS_ACCT_LOCKED: // USERPASS Lockout (ERROR)
						doAlert(e, "Your account has been locked");
						break;
					//case StatusResponse.STATUS_EMPTY_REQUEST: // Empty signon transaction not supported (ERROR)
					//case StatusResponse.STATUS_PINCH_REQUIRED: // Signon invalid without supporting pin change request (ERROR)
					//case StatusResponse.STATUS_CLIENTUID_REJECTED: // CLIENTUID error (ERROR)
					case StatusResponse.STATUS_CALL_US: // User should contact financial institution (ERROR)
						doAlert(e, "Please contact your financial institution");
						break;
					case StatusResponse.STATUS_AUTHTOKEN_REQUIRED: // OFX server requires AUTHTOKEN in signon during the next session (ERROR)
						{
							requireAuthToken = true;
							buildView();
							TextView tokenView = (TextView)findViewById(R.id.TokenPrompt);
							tokenView.setVisibility(View.VISIBLE);
							tokenView.setText(realm.authTokenLabel);
							tokenView.setFocusableInTouchMode(true);
							tokenView.requestFocus();
							//tokenView.requestRectangleOnScreen(viewRectangle);
							break;
						}
					case StatusResponse.STATUS_AUTHTOKEN_INVALID:// AUTHTOKEN invalid (ERROR)
						doAlert(e, "\"" + realm.authTokenLabel + "\" is invalid");
						break;
		        	default:
						doAlert(e, "Server refused the login");
						break;
		        	}
				}
				
			case QH_ERR_HTTP:
			case QH_ERR_TIMEOUT:
			case QH_ERR_CONN:
			case QH_ERR_SSL:
				doRetryableAlert((Exception)msg.obj, "Unable to connect to server");
				break;

			default:
				doAlert((Exception)msg.obj, OfxProfile.exceptionComment((Exception)msg.obj));
				break;
			}
		}
	};
	
	private void sendExceptionMsg(int what, Exception e)
	{
		e.printStackTrace();
		Message msg = Message.obtain();
		msg.obj = e;
		msg.what = what;
		queryHandler.sendMessage(msg);
	}

	@Override
	public void run()
	{
		SignonMsgReq son = buildLoginRequest();
		completeLoginRequest(son);

		OfxRequest req = profile.newRequest();
        req.addRequest(son);
        req.addRequest(profile.newProfRequest(profile.profileIsUser));

    	List<OfxMessageResp> response;
        try {
			response = req.submit(this);
		} catch (HttpResponseException e) {
			sendExceptionMsg(QH_ERR_STATUS, e);
			return;
		} catch (OfxError e) {
			sendExceptionMsg(QH_ERR_OFX, e);
			return;
		} catch (XmlPullParserException e) {
			sendExceptionMsg(QH_ERR_PARSE, e);
			return;
		} catch (SSLPeerUnverifiedException e) {
			sendExceptionMsg(QH_ERR_SSL_VERIFY, e);
			return;
		} catch (ClientProtocolException e) {
			sendExceptionMsg(QH_ERR_HTTP, e);
			return;
		} catch (ConnectException e) {
			sendExceptionMsg(QH_ERR_TIMEOUT, e);
			return;
		} catch (SocketException e) {
			sendExceptionMsg(QH_ERR_CONN, e);
			return;
		} catch (SSLException e) {
			sendExceptionMsg(QH_ERR_SSL, e);
			return;
		} catch (Exception e) {
			sendExceptionMsg(QH_ERR, e);
			return;
		}

		LoginSession session = null;
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
	    			db.open();
		    		db.pushSession(session);
		    		this.sessionId = session.ID;
	    		}
	    		catch(SQLiteException e)
	    		{
	    			sendExceptionMsg(QH_ERR, e);
	    			return;
	    		}
	    		finally
	    		{
	    			db.close();
	    		}
	    	}
		}
		
		queryHandler.sendEmptyMessage(session == null ? QH_EMPTY : QH_OK);
//	} catch (Throwable e) {
//		// last chance handler
//		e.printStackTrace();
//		//throw(e);
//	}
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
}
