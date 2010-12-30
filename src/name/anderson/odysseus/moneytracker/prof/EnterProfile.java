/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * @author Erik
 *
 */
public class EnterProfile extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_profile);
		
		Spinner ofxVer = (Spinner)findViewById(R.id.OfxVersion);
		ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this, R.array.enter_profile_ver_options, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ofxVer.setAdapter(mAdapter);
	}
/*
    public String  fiID;
	public String  fiOrg;
	public String  appId;
	public float   appVer;
	public float   ofxVer;
*/
}
