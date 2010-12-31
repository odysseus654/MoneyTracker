package name.anderson.odysseus.moneytracker.prof;

import name.anderson.odysseus.moneytracker.ExceptionAlert;
import android.app.ListActivity;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class SelectProfile extends ListActivity implements Runnable
{
	private static final String[] LIST_COL = { "name" };
	private static final int[] LIST_IDS = { android.R.id.text1 };
	private OfxFiDefTable db;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		db = new OfxFiDefTable(this);
		
		try
		{
			db.open();
		}
		catch(SQLiteException e)
		{
			// TODO: something useful here
			OnClickListener quitOnDismiss = null;
			ExceptionAlert.showAlert(this, e, "Unable to open profile store", "Internal Error", quitOnDismiss);
		}
		buildView();
	}
		
	private void buildView()
	{
		Cursor cur = db.defList();
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cur, LIST_COL, LIST_IDS);
		setListAdapter(adapter);

		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				// TODO: something
			}
		});
	}
	
	@Override
	protected void onDestroy()
	{
		db.close();
		db = null;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
	}
}
