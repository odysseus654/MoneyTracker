package name.anderson.odysseus.moneytracker.prof;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
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
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.ofx.*;

import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;

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
		if(savedInstanceState != null)
		{
			if(savedInstanceState.containsKey("ofxVer")) this.profile.fidef.ofxVer = savedInstanceState.getFloat("ofxVer");
			if(savedInstanceState.containsKey("appId")) this.profile.fidef.appId = savedInstanceState.getString("appId");
			if(savedInstanceState.containsKey("appVer")) this.profile.fidef.appVer = savedInstanceState.getInt("appVer");
			if(savedInstanceState.containsKey("useExpectContinue")) this.profile.useExpectContinue = savedInstanceState.getBoolean("useExpectContinue");
			if(savedInstanceState.containsKey("prof_id")) this.profile.ID = savedInstanceState.getInt("prof_id");
		}
		
		beginNegotiation();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(this.profile != null && this.profile.fidef != null)
		{
			outState.putFloat("ofxVer", this.profile.fidef.ofxVer);
			outState.putString("appId", this.profile.fidef.appId);
			outState.putInt("appVer", this.profile.fidef.appVer);
			outState.putBoolean("useExpectContinue", this.profile.useExpectContinue);
			outState.putInt("prof_id", this.profile.ID);
		}
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
		
		queryThread = new Thread(this, "Negotiate Thread");
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
		
		if(profile.fidescr != null)
		{
			FiDescr d = profile.fidescr;
			if(d.FIName != null && !d.FIName.equals(""))
			{
				((TextView)findViewById(R.id.ProfName)).setText(d.FIName);
			} else {
				findViewById(R.id.ProfNameRow).setVisibility(View.GONE);
			}
			String addr = "";
			if(d.Addr1 != null) addr = d.Addr1;
			if(d.Addr2 != null) addr = addr + '\n' + d.Addr2;
			if(d.Addr3 != null) addr = addr + '\n' + d.Addr3;
			if(d.City != null || d.State != null || d.PostalCode != null || d.Country != null)
			{
				String lastLine = "";
				if(d.City != null) lastLine = d.City;
				if(d.State != null)
				{
					if(!lastLine.equals("")) lastLine = lastLine + ", ";
					lastLine = lastLine + d.State;
				}
				if(d.PostalCode != null)
				{
					if(!lastLine.equals("")) lastLine = lastLine + ' ';
					lastLine = lastLine + d.PostalCode;
				}
				if(d.Country != null)
				{
					if(!lastLine.equals("")) lastLine = lastLine + ' ';
					lastLine = lastLine + d.Country;
				}
				if(!addr.equals("")) addr = addr + '\n';
				addr = addr + lastLine;
			}
			if(!addr.equals(""))
			{
				((TextView)findViewById(R.id.ProfAddr)).setText(addr);
			} else {
				findViewById(R.id.ProfAddrRow).setVisibility(View.GONE);
			}
			if(d.CSPhone != null && !d.CSPhone.equals(""))
			{
				((TextView)findViewById(R.id.ProfCSPhone)).setText(d.CSPhone);
			} else {
				findViewById(R.id.ProfCSPhoneRow).setVisibility(View.GONE);
			}
			if(d.TSPhone != null && !d.TSPhone.equals(""))
			{
				((TextView)findViewById(R.id.ProfTSPhone)).setText(d.TSPhone);
			} else {
				findViewById(R.id.ProfTSPhoneRow).setVisibility(View.GONE);
			}
			if(d.FaxPhone != null && !d.FaxPhone.equals(""))
			{
				((TextView)findViewById(R.id.ProfFaxPhone)).setText(d.FaxPhone);
			} else {
				findViewById(R.id.ProfFaxPhoneRow).setVisibility(View.GONE);
			}
			if(d.URL != null && !d.URL.equals(""))
			{
				((TextView)findViewById(R.id.ProfUrl)).setText(d.URL);
			} else {
				findViewById(R.id.ProfUrlRow).setVisibility(View.GONE);
			}
			if(d.Email != null && !d.Email.equals(""))
			{
				((TextView)findViewById(R.id.ProfEmail)).setText(d.Email);
			} else {
				findViewById(R.id.ProfEmailRow).setVisibility(View.GONE);
			}
		}
		else
		{
			findViewById(R.id.ProfHelp).setVisibility(View.GONE);
			findViewById(R.id.ProfTable).setVisibility(View.GONE);
		}

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

	void profileSelected()
	{
		ProfileTable db = new ProfileTable(this);
		try
		{
			db.open();
			db.pushProfile(profile);
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to store profile", "Internal Error", null);
			dlg.show();
			return;
		}
		finally
		{
			db.close();
		}
		
		Intent i = getIntent();
		i.putExtra("prof_id", profile.ID);
		setResult(RESULT_OK, i);
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
	
	private Handler queryHandler = new Handler()
	{
		private DialogInterface.OnClickListener cancelOnClick = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				cancel();
			}
		};

		private void doAlert(Exception e, String msg)
		{
			AlertDialog dlg = Utilities.buildAlert(VerifyProfile.this, e, msg, "Negotiation Error", cancelOnClick);
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
			dialog.setNegativeButton("Cancel", cancelOnClick);
			
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
					
				case QH_ERR_OFX:
					{
						OfxError e = (OfxError)msg.obj;
			        	switch(e.getErrorCode())
			        	{
			        	case StatusResponse.STATUS_FI_INVALID: // <FI> Missing or Invalid in <SONRQ> (ERROR)
							doAlert(e, "Server is rejecting connection details (FI_ID or FI_ORG)");
							break;
			        	default:
							doAlert(e, "Got a strange response from the server");
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
//		try {
			try {
				profile.negotiate(this);
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
//		} catch (Throwable e) {
//			// last chance handler
//			e.printStackTrace();
//			//throw(e);
//		}
	}
}
