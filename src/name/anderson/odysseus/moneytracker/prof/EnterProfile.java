/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Erik
 *
 */
public class EnterProfile extends Activity
{
	private static final int SELECT_PROFILE = 1001;
	private static final int SELECT_ACCOUNT = 1002;
	private static final float[] OFX_VERS = { 0.0f, 1.6f, 2.1f };

	private OfxFiDefinition baseDef; 
	CharSequence name;
	CharSequence fiURL;
	boolean fiNeeded;
	CharSequence fiOrg;
	CharSequence fiID;
	boolean appNeeded;
	CharSequence appId;
	int appVer;
	int ofxVer;
	boolean simpleProf;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_profile);
		
		if(savedInstanceState != null)
		{
			this.baseDef = new OfxFiDefinition(savedInstanceState);
		} else {
			this.baseDef = new OfxFiDefinition(getIntent().getExtras());
		}
		this.name = baseDef.name;
		this.fiURL = baseDef.fiURL;
		this.fiOrg = baseDef.fiOrg;
		this.fiID = baseDef.fiID;
		this.appId = baseDef.appId;
		this.appVer = baseDef.appVer;
		this.ofxVer = (int) baseDef.ofxVer;
		this.simpleProf = baseDef.simpleProf;

		if(savedInstanceState != null)
		{
			this.fiNeeded = savedInstanceState.getBoolean("fiNeeded");
			this.appNeeded = savedInstanceState.getBoolean("appNeeded");
			this.appId = savedInstanceState.getString("appId");
			this.appVer = savedInstanceState.getInt("appVer");
		} else {
			this.fiNeeded = (this.fiOrg != null) && (this.fiID != null);
			this.appNeeded = (this.appId != null);
			if(!this.appNeeded)
			{
				this.appId = "QWIN";
				this.appVer = 1900;
			}
		}
		
		Spinner ofxVer = (Spinner)findViewById(R.id.OfxVersion);
		ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this, R.array.enter_profile_ver_options, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ofxVer.setAdapter(mAdapter);

		doBindings();
		pushData();
	}
	
	private void rebuildProfile()
	{
		// assemble the profile
		if(this.baseDef == null) this.baseDef = new OfxFiDefinition();
		baseDef.name = this.name.toString();
		baseDef.fiURL = this.fiURL.toString();
		if(this.fiNeeded)
		{
			baseDef.fiOrg = this.fiOrg.toString();
			baseDef.fiID = this.fiID.toString();
		} else {
			baseDef.fiOrg = null;
			baseDef.fiID = null;
		}
		if(this.appNeeded)
		{
			baseDef.appId = this.appId.toString();
			baseDef.appVer = this.appVer;
		} else {
			baseDef.appId = null;
		}
		baseDef.ofxVer = OFX_VERS[this.ofxVer];
		baseDef.simpleProf = this.simpleProf;
	}
	
	void profileSelected()
	{
		rebuildProfile();

		// now chain to the verify step
		Intent verifyProf = new Intent(this, VerifyProfile.class);
		Bundle bdl = new Bundle();
		this.baseDef.push(bdl);
		verifyProf.putExtras(bdl);
		startActivityForResult(verifyProf, SELECT_PROFILE);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		if(resultCode == RESULT_OK)
		{
			if(requestCode == SELECT_PROFILE)
			{
				// we have a profile, now select an account
				int profileId = data.getIntExtra("prof_id", 0);
				Intent verifyProf = new Intent(this, SelectAccount.class);
				verifyProf.putExtra("prof_id", profileId);
				startActivityForResult(verifyProf, SELECT_ACCOUNT);
			}
			else if(requestCode == SELECT_ACCOUNT)
			{
				int acctId = data.getIntExtra("acct_id", 0);
				Intent i = getIntent();
				i.putExtra("acct_id", acctId);
				setResult(RESULT_OK, i);
				finish();
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		rebuildProfile();
		this.baseDef.push(outState);
		outState.putBoolean("fiNeeded", this.fiNeeded);
		outState.putBoolean("appNeeded", this.appNeeded);
		outState.putString("appId", this.appId.toString());
		outState.putInt("appVer", this.appVer);
	}
	
	private void doBindings()
	{
		((EditText)findViewById(R.id.NameEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				name = ((EditText)v).getText();
				return false;
			}
		});

		((EditText)findViewById(R.id.UrlEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				fiURL = ((EditText)v).getText();
				return false;
			}
		});

		((CheckBox)findViewById(R.id.FiNeededCheck)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				fiNeeded = ((CheckBox)v).isChecked();
				enableFI();
			}
		});

		((EditText)findViewById(R.id.FiOrgEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				fiOrg = ((EditText)v).getText();
				return false;
			}
		});

		((EditText)findViewById(R.id.FiIdEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				fiID = ((EditText)v).getText();
				return false;
			}
		});

		((CheckBox)findViewById(R.id.AppNeededCheck)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				appNeeded = ((CheckBox)v).isChecked();
				enableApp();
			}
		});

		((EditText)findViewById(R.id.AppNameEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				appId = ((EditText)v).getText();
				return false;
			}
		});

		((EditText)findViewById(R.id.AppVerEdit)).setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				CharSequence appVerStr = ((EditText)v).getText(); 
				appVer = Integer.parseInt(appVerStr.toString());
				return false;
			}
		});

		((Spinner)findViewById(R.id.OfxVersion)).setOnItemSelectedListener(new OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
			{
				ofxVer = position;
			}

			public void onNothingSelected(AdapterView<?> parent)
			{
				ofxVer = 0;
			}
		});

		((CheckBox)findViewById(R.id.SimpleProfCheck)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				simpleProf = ((CheckBox)v).isChecked();
			}
		});

		((Button)findViewById(R.id.OkButton)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				profileSelected();
			}
		});

		((Button)findViewById(R.id.CancelButton)).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
	
	private void pushData()
	{
		((EditText)findViewById(R.id.NameEdit)).setText(name);
		((EditText)findViewById(R.id.UrlEdit)).setText(fiURL);
		((CheckBox)findViewById(R.id.FiNeededCheck)).setChecked(fiNeeded);
		((EditText)findViewById(R.id.FiOrgEdit)).setText(fiOrg);
		((EditText)findViewById(R.id.FiIdEdit)).setText(fiID);
		((CheckBox)findViewById(R.id.AppNeededCheck)).setChecked(appNeeded);
		((EditText)findViewById(R.id.AppNameEdit)).setText(appId);
		((EditText)findViewById(R.id.AppVerEdit)).setText(Integer.toString(appVer));
		((Spinner)findViewById(R.id.OfxVersion)).setSelection(ofxVer);
		((CheckBox)findViewById(R.id.SimpleProfCheck)).setChecked(simpleProf);

		enableFI();
		enableApp();
	}
	
	private void enableFI()
	{
		CheckBox cb = (CheckBox)findViewById(R.id.FiNeededCheck);
		boolean bEnabled = cb.isChecked();
		findViewById(R.id.FiOrgPrompt).setEnabled(bEnabled);
		findViewById(R.id.FiOrgEdit).setEnabled(bEnabled);
		findViewById(R.id.FiIdPrompt).setEnabled(bEnabled);
		findViewById(R.id.FiIdEdit).setEnabled(bEnabled);
	}
	
	private void enableApp()
	{
		CheckBox cb = (CheckBox)findViewById(R.id.AppNeededCheck);
		boolean bEnabled = cb.isChecked();
		findViewById(R.id.AppNamePrompt).setEnabled(bEnabled);
		findViewById(R.id.AppVerPrompt).setEnabled(bEnabled);
		findViewById(R.id.AppNameEdit).setEnabled(bEnabled);
		findViewById(R.id.AppVerEdit).setEnabled(bEnabled);
	}
}
