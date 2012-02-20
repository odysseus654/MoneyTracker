/**
 * 
 */
package name.anderson.odysseus.moneytracker.acct;

import java.util.*;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.prof.AddProfile;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

/**
 * @author Erik
 *
 */
public class AccountList extends ListActivity 
{
	private static final int ADD_ACCOUNT = 2001;

	private AcctTables db;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		db = new AcctTables(this);
		try
		{
			db.openReadable();
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to open account store", "Internal Error", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					cancel();
				}
			});
			dlg.show();
			return;
		}
		buildView();
	}
	
	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) 
	{
		if(resultCode == RESULT_OK && requestCode == ADD_ACCOUNT)
		{
			buildView();
		}
	}

	private static class AccountAdapter extends BaseAdapter
	{
		private Context context;
		private Account[] accountList;
		
		public AccountAdapter(Context mContext, Account[] accts)
		{
			context = mContext;
			accountList = accts;
		}

		public int getCount()
		{
			return accountList.length;
		}

		public Object getItem(int position)
		{
			return accountList[position];
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			RelativeLayout rowLayout;
			Account rinfo = accountList[position];
			if (convertView == null) {
				rowLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.acct_row, parent, false);
			} else {
				rowLayout = (RelativeLayout) convertView;
			}

			((TextView)rowLayout.findViewById(R.id.Name)).setText(rinfo.name);
			((TextView)rowLayout.findViewById(R.id.CurBal)).setText(Double.toString(rinfo.curBalAmt));
			((TextView)rowLayout.findViewById(R.id.AvailBal)).setText(Double.toString(rinfo.availBalAmt));

			return rowLayout;
		}
	}

	private void buildView()
	{
		Cursor cur = db.acctList(null);
		if(cur.moveToNext())
		{
			final Account[] accountList = buildAccountList(cur);
			AccountAdapter adapter = new AccountAdapter(this, accountList);
			setListAdapter(adapter);

			ListView lv = getListView();
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
				{
					selectAcctById(accountList[pos]);
				}
			});
		}
		else
		{
			startActivityForResult(new Intent(this, AddProfile.class), ADD_ACCOUNT);
		}
	}

	protected void selectAcctById(Account account)
	{
		// TODO Auto-generated method stub
		int _i = 0;
	}

	private Account[] buildAccountList(Cursor cur)
	{
		List<Account> acctList = new LinkedList<Account>();
		if(!cur.moveToFirst()) return null;
		for(;;)
		{
			Account acct = db.getAccountFromCursor(cur);
			acctList.add(acct);
			if(!cur.moveToNext()) break;
		}
		
		Account[] result = new Account[acctList.size()];
		return acctList.toArray(result);
	}
	
}
