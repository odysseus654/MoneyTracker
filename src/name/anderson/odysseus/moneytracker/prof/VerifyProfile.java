package name.anderson.odysseus.moneytracker.prof;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.*;
import java.net.*;
import javax.net.ssl.*;
import name.anderson.odysseus.moneytracker.ExceptionAlert;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.ofx.OfxProfile;
import org.apache.http.client.*;

/**
 * @author Erik
 *
 */
public class VerifyProfile extends Activity implements Runnable
{
	ProgressDialog prog;
	private Thread queryThread;
	OfxProfile profile;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		OfxFiDefinition fidef = new OfxFiDefinition(savedInstanceState);
		
		// build this definition into a profile
		this.profile = new OfxProfile(fidef);
		
		beginNegotiation();
	}
	
	void beginNegotiation()
	{
		prog = ProgressDialog.show(this, null, getString(R.string.download_progress),
				false, true, new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				cancel();
			}
		});
		
		queryThread = new Thread(this);
		queryThread.setDaemon(true);
		queryThread.start();
	}

	void buildView()
	{
		// TODO Auto-generated method stub
		int i = 0;
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
	
	private Handler queryHandler = new Handler()
	{
		private void doAlert(Exception e, String msg)
		{
			AlertDialog dlg = ExceptionAlert.buildAlert(VerifyProfile.this, e, msg, "Negotiation Error", new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					cancel();
				}
			});
			dlg.show();
		}

		private void doRetryableAlert(Exception e, String msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(VerifyProfile.this);
			dialog.setTitle(msg);
			String dispMsg = msg + "\n\n" + e.getMessage();
			dialog.setMessage(dispMsg);
			dialog.setPositiveButton("Retry", new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					beginNegotiation();
				}
			});
			dialog.setNegativeButton("Cancel", new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					cancel();
				}
			});
			
			AlertDialog dlg = dialog.create();
			dlg.show();
		}

		public void handleMessage(Message msg)
		{
			if(msg.obj == null)
			{
				buildView();
			}
			else
			{
				switch(msg.what)
				{
				case QH_ERR_STATUS:
					{
						HttpResponseException e = (HttpResponseException)msg.obj;
						switch(e.getStatusCode())
						{
						case 200:
							doAlert(e, "Got a strange response from the server (maybe the location points to a webpage?)");
							break;
						case 400:
							doAlert(e, "Server rejected all attempts to negotiate (limitation of this program?)");
							break;
						default:
							doAlert(e, "Got a strange response from the server (maybe the location has been removed?)");
							break;
						}
						break;
					}
					
				case QH_ERR_SSL_VERIFY:
					doAlert((Exception)msg.obj, "The server identity was rejected (this might not be the bank you think it is)");
					break;

				case QH_ERR_HTTP:
				case QH_ERR_TIMEOUT:
				case QH_ERR_CONN:
				case QH_ERR_SSL:
					doRetryableAlert((Exception)msg.obj, "Unable to connect to server");
					break;

				default:
					doAlert((Exception)msg.obj, "Failed to retrieve profile information");
					break;
				}
			}
		}
	};
	
	private void sendExceptionMsg(int what, Exception e)
	{
		Message msg = Message.obtain();
		msg.obj = e;
		msg.what = what;
		queryHandler.sendMessage(msg);
	}

	@Override
	public void run()
	{
		try {
			profile.negotiate();
		} catch (HttpResponseException e) {
			sendExceptionMsg(QH_ERR_STATUS, e);
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
