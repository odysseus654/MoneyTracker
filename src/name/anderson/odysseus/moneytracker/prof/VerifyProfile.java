package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.ExceptionAlert;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.ofx.OfxProfile;
import android.app.*;
import android.content.DialogInterface;
import android.os.*;

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
	}

	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}

	private Handler queryHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if(msg.obj == null)
			{
				buildView();
			}
			else
			{
				Exception e = (Exception)msg.obj;
				AlertDialog dlg = ExceptionAlert.buildAlert(VerifyProfile.this, e,
						"Failed to retrieve profile information", "Negotiation Error", null);
				dlg.show();
			}
		}

	};

	@Override
	public void run()
	{
		try {
			profile.negotiate();
		} catch (Exception e) {
			Message msg = Message.obtain();
			msg.obj = e;
			msg.what = 1;
			queryHandler.sendMessage(msg);
		}
		queryHandler.sendEmptyMessage(0);
	}
}
