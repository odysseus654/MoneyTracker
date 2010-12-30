/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.R;
import android.app.Activity;
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
	public CharSequence name;
	public CharSequence fiURL;
	public boolean fiNeeded;
	public CharSequence fiOrg;
	public CharSequence fiID;
	public boolean appNeeded;
	public CharSequence appId;
	public int appVer;
	public int ofxVer;
	public boolean simpleProf;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_profile);
		
		if(savedInstanceState != null)
		{
			OfxFiDefinition def = new OfxFiDefinition(savedInstanceState);
			this.name = def.name;
			this.fiURL = def.fiURL;
			this.fiOrg = def.fiOrg;
			this.fiID = def.fiID;
			this.appId = def.appId;
			this.appVer = def.appVer;
			this.ofxVer = (int) def.ofxVer;
			this.simpleProf = def.simpleProf;
		}
		this.fiNeeded = (this.fiOrg != null) && (this.fiID != null);
		this.appNeeded = (this.appId != null);
		if(!this.appNeeded)
		{
			this.appId = "QWIN";
			this.appVer = 1900;
		}
		
		Spinner ofxVer = (Spinner)findViewById(R.id.OfxVersion);
		ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this, R.array.enter_profile_ver_options, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ofxVer.setAdapter(mAdapter);

		doBindings();
		pushData();
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
				// TODO: something!
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
