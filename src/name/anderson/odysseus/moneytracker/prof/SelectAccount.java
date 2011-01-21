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
import name.anderson.odysseus.moneytracker.ofx.prof.ProfileMsgResp;
import name.anderson.odysseus.moneytracker.ofx.signup.*;
import android.app.*;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

/**
 * @author Erik
 *
 */
public class SelectAccount extends ListActivity implements Runnable
{
	private LoginSession session;
	ProgressDialog prog;
	private Thread queryThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		Bundle parms = getIntent().getExtras();
		int sessionId = parms.getInt("sess_id");
		
//		if(sessionId == 0)
//		{
//		}
		
//		if(savedInstanceState != null)
//		{
//		}
		
		loadContext(sessionId);
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
			session = db.getSession(sessionId);
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
		if(session == null)
		{
			AlertDialog dlg = Utilities.buildAlert(this, null, "Could not find login session", "Internal Error", dismissOnOk);
			dlg.show();
			return;
		}
		
//		if(savedInstanceState != null)
//		{
//		}
//		else if(session != null)
//		{
//		}
		
		buildView();
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

	private void buildView()
	{
	}
	
	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
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
				requestComplete();
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
    	OfxRequest req = session.profile.newRequest();
    	AccountInfoMsgReq acctReq = new AccountInfoMsgReq();
    	acctReq.acctListAge = session.profile.acctListAge;

    	List<OfxMessageResp> response;
        try {
			response = req.submit();

	    	for(OfxMessageResp resp : response)
	    	{
	    		if(resp instanceof AccountInfoMsgResp)
	    		{
	    			AccountInfoMsgResp acctResp = (AccountInfoMsgResp)resp;
	    			session.profile.acctListAge = acctResp.acctListAge;
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
		
		queryHandler.sendEmptyMessage(QH_OK);
	}
}
