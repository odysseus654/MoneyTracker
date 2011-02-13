/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.net.*;
import java.util.List;
import javax.net.ssl.*;
import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.acct.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.view.*;
import android.widget.*;

/**
 * @author Erik
 *
 */
public class SelectAccount extends ListActivity implements Runnable
{
	private final static int GET_SESSION = 1010;
	private LoginSession session;
	ProgressDialog prog;
	private Thread queryThread;
	private int profileId;
	private List<ServiceAcctInfo> accountList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle parms = getIntent().getExtras();
		int sessionId = parms.getInt("sess_id");
		profileId = parms.getInt("prof_id");
		
		if(savedInstanceState != null)
		{
			sessionId = savedInstanceState.getInt("sess_id");
		}
		
		if(sessionId == 0)
		{
			loginFailure();
		} else {
			loadContext(sessionId);
		}
	}
		
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(this.session != null) outState.putInt("sess_id", this.session.ID);
	}
	
	void loginFailure()
	{
		Intent loginProf = new Intent(this, Login.class);
		loginProf.putExtra("prof_id", profileId);
		if(this.session != null) loginProf.putExtra("sess_id", this.session.ID);
		startActivityForResult(loginProf, GET_SESSION);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		if(requestCode == GET_SESSION)
		{
			if(resultCode == RESULT_CANCELED)
			{
				cancel();
			} else {
				int sessionId = data.getIntExtra("sess_id", 0);
				loadContext(sessionId);
			}
		}
	}
	
	private void loadContext(int sessionId)
	{
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
			
			try
			{
				session = db.getSession(sessionId);
			}
			catch(SQLiteException e)
			{
				AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to retrieve profile", "Internal Error", dismissOnOk);
				dlg.show();
				return;
			}

			if(session == null)
			{
				AlertDialog dlg = Utilities.buildAlert(this, null, "Could not find login session", "Internal Error", dismissOnOk);
				dlg.show();
				return;
			}
			
			try
			{
				accountList = db.getAccountsBySession(sessionId);
			}
			catch(SQLiteException e)
			{
				AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to retrieve accounts", "Internal Error", dismissOnOk);
				dlg.show();
				return;
			}
		}
		finally
		{
			db.close();
		}
		if(accountList == null)
		{
			requestAccountList();
		} else {
			buildView();
		}
	}
	
	void requestAccountList()
	{
		prog = ProgressDialog.show(this, null, getString(R.string.account_list_progress),
				false, true, new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				cancel();
			}
		});
		
		queryThread = new Thread(this, "Requestor Thread");
		queryThread.setDaemon(true);
		queryThread.start();
	}
	
	private static class AcctInfoAdapter extends BaseAdapter
	{
		private Context context;
		private ServiceAcctInfo[] acctList;
		private String[] msgsetNames;
		
		public AcctInfoAdapter(Context mContext, ServiceAcctInfo[] accts)
		{
			context = mContext;
			acctList = accts;
			msgsetNames = context.getResources().getStringArray(R.array.message_set);
		}

		@Override
		public int getCount()
		{
			return acctList.length;
		}

		@Override
		public Object getItem(int position)
		{
			return acctList[position];
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			RelativeLayout rowLayout;
			ServiceAcctInfo acct = acctList[position];
			if (convertView == null) {
				rowLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.realm_login_row, parent, false);
			} else {
				rowLayout = (RelativeLayout) convertView;
			}

			String acctType = msgsetNames[ServiceAcctName.MSG_MAP[acct.type.ordinal()].ordinal()];
			((TextView)rowLayout.findViewById(R.id.Name)).setText(acct.desc);
			((TextView)rowLayout.findViewById(R.id.TaskList)).setText(acctType);

			return rowLayout;
		}
	}
	
	private void buildView()
	{
		ServiceAcctInfo[] tempList = new ServiceAcctInfo[accountList.size()];
		final ServiceAcctInfo[] acctList = accountList.toArray(tempList);
		AcctInfoAdapter adapter = new AcctInfoAdapter(this, acctList);
		setListAdapter(adapter);

		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				acctSelected(acctList[pos]);
			}
		});
	}
	
	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}
	
	void acctSelected(ServiceAcctInfo acct)
	{
		DialogInterface.OnClickListener emptyClickListener = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) { }
		};
		AlertDialog dlg = Utilities.buildAlert(SelectAccount.this, null, "Nothing more to do", "Login Error", emptyClickListener);
		dlg.show();
		int _i = 0;
	}

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
			AlertDialog dlg = Utilities.buildAlert(SelectAccount.this, e, msg, "Login Error", emptyClickListener);
			dlg.show();
		}

		private void doRetryableAlert(Exception e, String msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(SelectAccount.this);
			dialog.setTitle(msg);
			String dispMsg = msg + "\n\n" + e.getMessage();
			dialog.setMessage(dispMsg);
			dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					requestAccountList();
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
				buildView();
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
					case StatusResponse.STATUS_AUTHTOKEN_REQUIRED: // OFX server requires AUTHTOKEN in signon during the next session (ERROR)
					case StatusResponse.STATUS_BAD_LOGIN: // Signon invalid (see section 2.5.1) (ERROR)
					case StatusResponse.STATUS_AUTHTOKEN_INVALID:// AUTHTOKEN invalid (ERROR)
						loginFailure();
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
    	OfxRequest req = session.newRequest();
    	AccountInfoMsgReq acctReq = new AccountInfoMsgReq();
    	acctReq.acctListAge = session.profile.acctListAge;
    	req.addRequest(acctReq);

    	List<OfxMessageResp> response;
        try {
			response = req.submit(this);

	    	for(OfxMessageResp resp : response)
	    	{
	    		if(resp instanceof AccountInfoMsgResp)
	    		{
	    			AccountInfoMsgResp acctResp = (AccountInfoMsgResp)resp;
	    			session.profile.acctListAge = acctResp.acctListAge;
	    			accountList = acctResp.accounts;
	    		}
	    	}
        } catch (HttpResponseException e) {
			sendExceptionMsg(QH_ERR_STATUS, e);
		} catch (OfxError e) {
			sendExceptionMsg(QH_ERR_OFX, e);
		} catch (XmlPullParserException e) {
			sendExceptionMsg(QH_ERR_PARSE, e);
		} catch (SSLPeerUnverifiedException e) {
			sendExceptionMsg(QH_ERR_SSL_VERIFY, e);
		} catch (ClientProtocolException e) {
			sendExceptionMsg(QH_ERR_HTTP, e);
		} catch (ConnectException e) {
			sendExceptionMsg(QH_ERR_TIMEOUT, e);
		} catch (SocketException e) {
			sendExceptionMsg(QH_ERR_CONN, e);
		} catch (SSLException e) {
			sendExceptionMsg(QH_ERR_SSL, e);
		} catch (Exception e) {
			sendExceptionMsg(QH_ERR, e);
		}
		
		ProfileTable db = new ProfileTable(this);
		try
		{
			db.open();
			db.syncAccounts(session, accountList);
		}
		catch(SQLiteException e) {
			sendExceptionMsg(QH_ERR, e);
		}
		finally
		{
			db.close();
		}

		queryHandler.sendEmptyMessage(QH_OK);
	}
}
