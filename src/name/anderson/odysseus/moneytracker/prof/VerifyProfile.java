package name.anderson.odysseus.moneytracker.prof;

import android.app.*;
import android.content.Context;
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

import name.anderson.odysseus.moneytracker.DisconDialog;
import name.anderson.odysseus.moneytracker.DisconProgress;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.ofx.*;

import org.apache.http.client.*;
import org.xmlpull.v1.XmlPullParserException;

import com.github.ignition.core.tasks.IgnitedAsyncTask;

/**
 * @author Erik
 *
 */
public class VerifyProfile extends Activity
{
	private NegotiateTask queryTask;
	OfxProfile profile;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
        // try to obtain a reference to a task piped through from the previous
        // activity instance
		Object passthrough = getLastNonConfigurationInstance();
		if(passthrough == null)
		{
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
		else if(passthrough instanceof NegotiateTask)
		{
			queryTask = (NegotiateTask)passthrough;
			queryTask.connect(this);
		}
		else
		{
			profile = (OfxProfile)passthrough;
			buildView();
		}
	}
	
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        // we leverage this method to "tunnel" the task object through to the next
        // incarnation of this activity in case of a configuration change
    	if(queryTask != null)
    	{
    		return queryTask;
    	} else {
    		return profile;
    	}
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // always disconnect the activity from the task here, in order to not risk
        // leaking a context reference
        queryTask.disconnect();
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
		queryTask = new NegotiateTask(this);
		queryTask.execute(profile);
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
			db.openWritable();
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

	private static class NegotiateTask
		extends IgnitedAsyncTask<VerifyProfile, OfxProfile, Integer, Integer>
	{
		private OfxProfile profile;
		private DisconProgress prog;
		
		public NegotiateTask(VerifyProfile context)
		{
			super(context);
		}
		
		@Override
		public void connect(VerifyProfile context)
		{
			super.connect(context);
			if(prog != null) prog.connect(context);
			context.profile = profile;
		}

		@Override
	    public void disconnect()
		{
			super.disconnect();
			if(prog != null) prog.disconnect();
	    }
	    
    	static protected class ProgCancelListener implements DisconDialog.OnCancelListener
		{
			public void onCancel(Context ctx, DialogInterface dialog)
			{
				if(ctx != null) ((VerifyProfile)ctx).cancel();
			}
		}

    	@Override
	    protected void onStart(VerifyProfile context)
	    {
	    	prog = new DisconProgress(context);
	    	prog.setMessage(R.string.negotiate_progress);
	    	prog.setIndeterminate(false);
	    	prog.setCancelable(true);
	    	prog.setOnCancelListener(new ProgCancelListener());
	    	prog.show();
	    }

	    @Override
		public Integer run(OfxProfile... parm) throws Exception
		{
			profile = parm[0];
			profile.negotiate(getContext());
			return 0;
		}

        @Override
        protected void onCompleted(VerifyProfile context, Integer result)
        {
			prog.hide();
        }

        @Override
        protected void onSuccess(VerifyProfile context, Integer result)
        {
			context.buildView();
			context.queryTask = null;
			disconnect();
        }

		private DialogInterface.OnClickListener cancelOnClick = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				VerifyProfile context = NegotiateTask.this.getContext();
				if(context != null) context.cancel();
			}
		};

		private void doAlert(VerifyProfile context, Exception e, String msg)
		{
			AlertDialog dlg = Utilities.buildAlert(context, e, msg, "Negotiation Error", cancelOnClick);
			dlg.show();
		}

		private void doRetryableAlert(final VerifyProfile context, Exception e, String msg)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(msg);
			String dispMsg = msg + "\n\n" + e.getMessage();
			dialog.setMessage(dispMsg);
			dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					context.beginNegotiation();
				}
			});
			dialog.setNegativeButton("Cancel", cancelOnClick);
			
			AlertDialog dlg = dialog.create();
			dlg.show();
		}

		@Override
        protected void onError(VerifyProfile context, Exception error)
		{
			if(error instanceof HttpResponseException)
			{
				HttpResponseException e = (HttpResponseException)error;
				switch(e.getStatusCode())
				{
				case 200:
					doAlert(context, e, "Got a strange response from the server (maybe the location points to a webpage?)");
					break;
				case 400:
					doAlert(context, e, "Server rejected all attempts to negotiate (limitation of this program?)");
					break;
				default:
					doAlert(context, e, "Got a strange response from the server (maybe the location has been removed?)");
					break;
				}
			}
			else if(error instanceof OfxError)
			{
				OfxError e = (OfxError)error;
	        	switch(e.getErrorCode())
	        	{
	        	case StatusResponse.STATUS_FI_INVALID: // <FI> Missing or Invalid in <SONRQ> (ERROR)
					doAlert(context, e, "Server is rejecting connection details (FI_ID or FI_ORG)");
					break;
	        	default:
					doAlert(context, e, "Got a strange response from the server");
					break;
	        	}
			}
			else if(error instanceof XmlPullParserException
					|| error instanceof SSLPeerUnverifiedException)
			{
				doAlert(context, error, OfxProfile.exceptionComment(error));
			}
			else if(error instanceof ClientProtocolException
					|| error instanceof ConnectException
					|| error instanceof SocketException
					|| error instanceof SSLException)
			{
				doRetryableAlert(context, error, "Unable to connect to server");
			}
			else
			{
				doAlert(context, error, OfxProfile.exceptionComment(error));
			}
        }
	}
}
