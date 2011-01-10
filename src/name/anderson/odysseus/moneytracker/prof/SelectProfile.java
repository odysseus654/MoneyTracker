package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.util.List;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.view.View;
import android.widget.*;

public class SelectProfile extends ListActivity implements Runnable
{
	private static final String[] LIST_COL = { "name" };
	private static final int[] LIST_IDS = { android.R.id.text1 };
	private static final int SELECT_PROFILE = 1001;
	private OfxFiDefTable db;
	private String filterText;
	ProgressDialog prog;
	private Thread updateThread;
	ForeignDefList[] deflistProviders;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null)
		{
			this.filterText = savedInstanceState.getString("filterText");
		}
		db = new OfxFiDefTable(this);
		deflistProviders = new ForeignDefList[] { new MoneydanceDefList(), new OfxHomeDefList() };
		
		try
		{
			db.open();
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to open profile store", "Internal Error", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					cancel();
				}
			});
			dlg.show();
			return;
		}
		if(db.requiresUpdate())
		{
			beginUpdate();
		} else {
			buildView();
		}
	}
	
	void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}

	private OfxFiDefinition retrieveDefFromId(int id)
	{
		OfxFiDefinition def = db.getDefById(id);
		if(def.srcName != null)
		{
			try {
				for(ForeignDefList entry : deflistProviders)
				{
					if(def.srcName.equalsIgnoreCase(entry.name()))
					{
						def = entry.completeDef(def);
						break;
					}
				}
			} catch (Exception e) {
				AlertDialog dlg = Utilities.buildAlert(SelectProfile.this, e, "Unable to retrieve details", "Download Error", null);
				dlg.show();
				return null;
			}
		}
		return def;
	}

	void selectDefById(int id)
	{
		OfxFiDefinition def = retrieveDefFromId(id);
		if(def != null)
		{
			Intent verifyProf = new Intent(this, VerifyProfile.class);
			Bundle bdl = new Bundle();
			def.push(bdl);
			verifyProf.putExtras(bdl);
			startActivityForResult(verifyProf, SELECT_PROFILE);
		}
	}

	private void beginUpdate()
	{
		prog = ProgressDialog.show(this, null, getString(R.string.download_progress),
				false, true, new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				cancel();
			}
		});
		
		updateThread = new Thread(this, "Download Thread");
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void buildView()
	{
		Cursor cur = db.defList(null);
		if(cur.moveToNext())
		{
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cur, LIST_COL, LIST_IDS);
			adapter.setFilterQueryProvider(new FilterQueryProvider()
			{
				public Cursor runQuery(CharSequence constraint)
				{
					filterText = constraint.toString();
					return db.defList(filterText);
				}
			});
			setListAdapter(adapter);
	
			ListView lv = getListView();
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setTextFilterEnabled(true);
			if(this.filterText != null)
			{
				lv.setFilterText(this.filterText);
			}
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
				{
					selectDefById((int)id);
				}
			});
		}
		else
		{
			// TODO: fallback
			int a = 0;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(this.filterText != null) outState.putString("filterText", this.filterText);
	}
	
	@Override
	protected void onDestroy()
	{
		if(updateThread != null)
		{
			Thread deadThread = updateThread;
			updateThread = null;
			deadThread.interrupt();
		}
		if(db != null)
		{
			db.close();
			db = null;
		}
		deflistProviders = null;
		super.onDestroy();
	}

	private Handler errHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if(msg.what == 0)
			{
				buildView();
			}
			else if(msg.obj != null)
			{
				String strMsg = null;
				switch(msg.what)
				{
				case 1:
					strMsg = "Unable to retrieve ";
					break;
				case 2:
					strMsg = "Unable to handle response from ";
					break;
				case 3:
					strMsg = "Unable to store response from ";
				}
				if(strMsg != null)
				{
					Object[] msgList = (Object[])msg.obj;
					ForeignDefList fgnDefList = (ForeignDefList)msgList[0];
					Exception e = (Exception)msgList[1];
					strMsg = strMsg + fgnDefList.name();
					AlertDialog dlg = Utilities.buildAlert(SelectProfile.this, e, strMsg, "Download Error", null);
					dlg.show();
				}
			}
		}
	};

	@Override
	public void run()
	{
//		try {
			for(ForeignDefList entry : deflistProviders)
			{
				syncForeignDefList(entry);
			}
			errHandler.sendEmptyMessage(0);
			try {
				prog.dismiss();
			}
			catch(IllegalArgumentException e)
			{	// this may happen due to race conditions on activity shutdown?
				e.printStackTrace();
			}
//		} catch (Throwable e) {
//			// last chance handler
//			e.printStackTrace();
//			//throw(e);
//		}
	}
	
	private void syncForeignDefList(ForeignDefList fgnDefList)
	{
		Reader r;
		try {
			r = fgnDefList.retrieveDefList();
		} catch (Exception e) {
			Message msg = Message.obtain();
			msg.obj = new Object[] { fgnDefList, e };
			msg.what = 1;
			errHandler.sendMessage(msg);
			return;
		}
		try {
			List<OfxFiDefinition> defList;
		    try {
				defList = fgnDefList.parseDefList(r);
			} catch (Exception e) {
				Message msg = Message.obtain();
				msg.obj = new Object[] { fgnDefList, e };
				msg.what = 2;
				errHandler.sendMessage(msg);
				return;
			}
			try {
				fgnDefList.sync(defList, db);
			} catch (Exception e) {
				Message msg = Message.obtain();
				msg.obj = new Object[] { fgnDefList, e };
				msg.what = 3;
				errHandler.sendMessage(msg);
			}
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
