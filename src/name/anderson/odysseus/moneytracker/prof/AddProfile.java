/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.acct.AcctTables;
import name.anderson.odysseus.moneytracker.ofx.ProfileTable;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/**
 * @author Erik
 *
 */
public class AddProfile extends ListActivity
{
	private static final int SELECT_PROFILE = 1001;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setListAdapter(ArrayAdapter.createFromResource(this, R.array.add_profile_options, android.R.layout.simple_list_item_1));

		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				switch(arg2)
				{
				case 0:
					startActivityForResult(new Intent(AddProfile.this, SelectProfile.class), SELECT_PROFILE);
					break;
				case 1:
					startActivityForResult(new Intent(AddProfile.this, EnterProfile.class), SELECT_PROFILE);
					break;
				}
			}
		});
	}
    
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		DialogInterface.OnClickListener ignoreOnOk = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
			}
		};

		if(resultCode == RESULT_OK && requestCode == SELECT_PROFILE)
		{
			int acctId = data.getIntExtra("acct_id", 0);
			ProfileTable acctDb = new ProfileTable(this);
			ServiceAcctInfo acct;
			try
			{
				acctDb.openReadable();
				acct = acctDb.getAccount(acctId);
			}
			catch(SQLiteException e)
			{
				AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to retrieve account", "Internal Error", ignoreOnOk);
				dlg.show();
				return;
			}
			
			AcctTables outDb = new AcctTables(this);
			try
			{
				outDb.openWritable();
				outDb.establishAccount(acct);
			}
			catch(SQLiteException e)
			{
				AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to link account", "Internal Error", ignoreOnOk);
				dlg.show();
				return;
			}

			Intent i = getIntent();
			i.putExtra("acct_id", acctId);
			setResult(RESULT_OK, i);
			finish();
		}
	}
}
