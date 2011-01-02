package name.anderson.odysseus.moneytracker.prof;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.ExceptionAlert;
import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.view.View;
import android.widget.*;

public class SelectProfile extends ListActivity implements Runnable
{
	private static final String[] LIST_COL = { "name" };
	private static final int[] LIST_IDS = { android.R.id.text1 };
	private OfxFiDefTable db;
	ProgressDialog prog;
	private Thread updateThread;
	ForeignDefList[] deflistProviders;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		db = new OfxFiDefTable(this);
		deflistProviders = new ForeignDefList[] { new MoneydanceDefList(), new OfxHomeDefList() };
		
		try
		{
			db.open();
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = ExceptionAlert.buildAlert(this, e, "Unable to open profile store", "Internal Error", new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					cancel();
				}
			});
			dlg.show();
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

	private OfxFiDefinition retrieveDefFromId(long id)
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
				AlertDialog dlg = ExceptionAlert.buildAlert(SelectProfile.this, e, "Unable to retrieve details", "Download Error", null);
				dlg.show();
				return null;
			}
		}
		return def;
	}

	void selectDefById(long id)
	{
		OfxFiDefinition def = retrieveDefFromId(id);
		if(def != null)
		{
			Intent result = new Intent();
			Bundle bdl = new Bundle();
			def.push(bdl);
			result.putExtras(bdl);
			setResult(RESULT_OK, result);
			finish();
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
		
		updateThread = new Thread(this);
		updateThread.setDaemon(true);
		updateThread.start();
	}

	private void buildView()
	{
		Cursor cur = db.defList(null);
		if(cur != null && !cur.isAfterLast())
		{
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cur, LIST_COL, LIST_IDS);
			adapter.setFilterQueryProvider(new FilterQueryProvider()
			{
				public Cursor runQuery(CharSequence constraint)
				{
					return db.defList(constraint.toString());
				}
			});
			setListAdapter(adapter);
	
			ListView lv = getListView();
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
				{
					selectDefById(id);
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
	}

	private Handler errHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
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
					AlertDialog dlg = ExceptionAlert.buildAlert(SelectProfile.this, e, strMsg, "Download Error", null);
					dlg.show();
				}
			}
		}
	};

	@Override
	public void run()
	{
		for(ForeignDefList entry : deflistProviders)
		{
			syncForeignDefList(entry);
		}
		errHandler.sendEmptyMessage(0);
		prog.dismiss();
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
