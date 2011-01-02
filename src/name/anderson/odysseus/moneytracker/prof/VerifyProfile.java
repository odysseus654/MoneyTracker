package name.anderson.odysseus.moneytracker.prof;

import android.app.*;
import android.content.DialogInterface;
import android.os.*;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import java.net.*;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
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
		OfxFiDefinition fidef = new OfxFiDefinition(getIntent().getExtras());
		
		// build this definition into a profile
		this.profile = new OfxProfile(fidef);
		
		beginNegotiation();
	}
	
	void beginNegotiation()
	{
		prog = ProgressDialog.show(this, null, getString(R.string.negotiate_progress),
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
	
	private static class ParsedPrincipal
	{
		public String CN;
		public String O;
	}
	
	private static ParsedPrincipal parsePrincipal(Principal src)
	{
		String str = src.getName();
		if(str == null) return null;
		ParsedPrincipal result = new ParsedPrincipal();
		String[] values = TextUtils.split(str, ",");
		String partial = null;
		for(String val : values)
		{
			if(val.charAt(val.length()-1) == '\\')
			{
				if(partial == null)
				{
					partial = val.substring(0, val.length()-1) + ',';
				} else {
					partial = partial + val.substring(0, val.length()-1) + ',';
				}
				continue;
			}
			String thisStr = (partial == null) ? val : partial + val;
			partial = null;
			int equalPos = thisStr.indexOf('=');
			if(equalPos != -1)
			{
				if(thisStr.substring(0, equalPos).equals("CN"))
				{
					result.CN = thisStr.substring(equalPos+1);
				}
				else if(thisStr.substring(0, equalPos).equals("O"))
				{
					result.O = thisStr.substring(equalPos+1);
				}
			}
		}
		return result;
	}

	void buildView()
	{
		setContentView(R.layout.verify_profile);

		if(profile.lastCert != null)
		{
			X509Certificate cert = profile.lastCert;
			ParsedPrincipal subject = parsePrincipal(cert.getSubjectDN());
			((TextView)findViewById(R.id.Name)).setText(subject.O);
			((TextView)findViewById(R.id.Site)).setText(subject.CN);
			Date expires = cert.getNotAfter();
			((TextView)findViewById(R.id.Expires)).setText(expires == null ? "UNKNOWN" :
				DateFormat.getDateInstance().format(expires));
			ParsedPrincipal issuer = parsePrincipal(cert.getIssuerDN());
			((TextView)findViewById(R.id.Issuer)).setText(issuer.O);
		}
		else
		{
			((TextView)findViewById(R.id.Name)).setText("UNKNOWN");
			((TextView)findViewById(R.id.Site)).setText("UNKNOWN");
			((TextView)findViewById(R.id.Expires)).setText("UNKNOWN");
			((TextView)findViewById(R.id.Issuer)).setText("UNKNOWN");
		}
		
		// TODO Auto-generated method stub
		int i = 0;
		/*
		<TableLayout android:layout_below="@id/ProfHelp" android:layout_width="fill_parent"
			android:layout_height="wrap_content">
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfNameRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_name" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfName"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfAddrRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_addr" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfAddr"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfCSPhoneRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_csphone" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfCSPhone"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfTSPhoneRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_tsphone" />
				<TextView android:layout_width="fill_parent" android:id="@+id/TSPhone"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfFaxPhoneRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_faxphone" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfFaxPhone"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfUrlRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_url" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfUrl"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
			<TableRow android:layout_width="fill_parent" android:layout_height="wrap_content" android:id="@+id/ProfEmailRow">
				<TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
					android:text="@string/verify_profile_prof_email" />
				<TextView android:layout_width="fill_parent" android:id="@+id/ProfEmail"
					android:layout_height="wrap_content" android:text="'PLACEHOLDER'" />
			</TableRow>
		</TableLayout>

		<!-- end buttons -->
		<LinearLayout android:layout_width="fill_parent" android:layout_height="wrap_content"
			android:layout_alignParentBottom="true">
			<Button android:text="@string/trustme" android:layout_width="fill_parent"
				android:id="@+id/OkButton" android:layout_height="wrap_content" android:layout_weight="1" />
			<Button android:text="@string/cancel" android:layout_width="fill_parent"
				android:id="@+id/CancelButton" android:layout_height="wrap_content" android:layout_weight="1" />
		</LinearLayout>
		*/

		((Button)findViewById(R.id.OkButton)).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				profileSelected();
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

	private void profileSelected()
	{
		// TODO Auto-generated method stub
		int i = 0;
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
			AlertDialog dlg = ExceptionAlert.buildAlert(VerifyProfile.this, e, msg, "Negotiation Error", new DialogInterface.OnClickListener()
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
			dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					beginNegotiation();
				}
			});
			dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
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
			prog.dismiss();
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
		} catch (Throwable e) {
			// last chance handler
			e.printStackTrace();
			//throw(e);
		}
	}
}
