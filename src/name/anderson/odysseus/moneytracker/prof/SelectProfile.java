package name.anderson.odysseus.moneytracker.prof;

import java.io.*;
import java.util.List;

import name.anderson.odysseus.moneytracker.DisconDialog;
import name.anderson.odysseus.moneytracker.DisconProgress;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.*;
import android.view.View;
import android.widget.*;
import com.github.ignition.core.tasks.IgnitedAsyncTask;

public class SelectProfile extends ListActivity
{
	private static final String[] LIST_COL = { "name" };
	private static final int[] LIST_IDS = { android.R.id.text1 };
	private static final int SELECT_PROFILE = 1001;
	private static final int SELECT_ACCOUNT = 1002;
	private OfxFiDefTable db;
	private String filterText;
	private DefRetrievalTask retrieveTask;
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
		
        // try to obtain a reference to a task piped through from the previous
        // activity instance
		retrieveTask = (DefRetrievalTask) getLastNonConfigurationInstance();
		if(retrieveTask != null)
		{
			retrieveTask.connect(this);
		}

        try
		{
			db.openReadable();
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
	
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        // we leverage this method to "tunnel" the task object through to the next
        // incarnation of this activity in case of a configuration change
        return retrieveTask;
    }

    void cancel()
	{
    	if(retrieveTask != null)
    	{
    		retrieveTask.cancel(true);
    		try {
				retrieveTask.get();
			} catch (Exception e) {}
    		retrieveTask = null;
    	}
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

	private void beginUpdate()
	{
		retrieveTask = new DefRetrievalTask(this);
		retrieveTask.execute(deflistProviders);
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
		if(retrieveTask != null)
		{
			retrieveTask.disconnect();
			retrieveTask = null;
		}
		if(db != null)
		{
			db.close();
			db = null;
		}
		deflistProviders = null;
		super.onDestroy();
	}

	private static class ErrorHandler extends Handler
	{
		private SelectProfile context;
		public static final int STATE_RETRIEVE = 1;
		public static final int STATE_PARSE = 2;
		public static final int STATE_SYNC = 3;
		
		public ErrorHandler(SelectProfile context)
		{
			this.context = context;
		}
		
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if(msg.obj != null)
			{
				String strMsg = null;
				switch(msg.what)
				{
				case STATE_RETRIEVE:
					strMsg = "Unable to retrieve ";
					break;
				case STATE_PARSE:
					strMsg = "Unable to handle response from ";
					break;
				case STATE_SYNC:
					strMsg = "Unable to store response from ";
				}
				if(strMsg != null)
				{
					Object[] msgList = (Object[])msg.obj;
					ForeignDefList fgnDefList = (ForeignDefList)msgList[0];
					Exception e = (Exception)msgList[1];
					strMsg = strMsg + fgnDefList.name();
					AlertDialog dlg = Utilities.buildAlert(context, e, strMsg, "Download Error", null);
					dlg.show();
				}
			}
		}
	};

	private static class DefRetrievalTask
		extends IgnitedAsyncTask<SelectProfile, ForeignDefList, Integer, Integer>
	{
		private ErrorHandler errHandler;
		private OfxFiDefTable db;
		private DisconProgress prog;
		
		public DefRetrievalTask(final SelectProfile context)
		{
			super(context);
			db = new OfxFiDefTable(context);
			try
			{
				db.openWritable();
			}
			catch(SQLiteException e)
			{
				AlertDialog dlg = Utilities.buildAlert(context, e, "Unable to open profile store", "Internal Error", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						context.cancel();
					}
				});
				dlg.show();
				db = null;
			}
		}
		
		@Override
		public void connect(SelectProfile context)
		{
			super.connect(context);
			if(prog != null) prog.connect(context);
			this.errHandler = new ErrorHandler(context);
			db = new OfxFiDefTable(context);
			db.openWritable();
	    }

		@Override
	    public void disconnect()
		{
			super.disconnect();
			if(prog != null) prog.disconnect();
			this.errHandler = null;
	    }
		
		static private class ProgCancelListener implements DisconDialog.OnCancelListener
		{
			public void onCancel(Context ctx, DialogInterface dialog)
			{
				if(ctx != null) ((SelectProfile)ctx).cancel();
			}
		}
	    
	    @Override
	    protected void onStart(SelectProfile context)
	    {
	    	prog = new DisconProgress(context);
	    	prog.setMessage(R.string.download_progress);
	    	prog.setIndeterminate(false);
	    	prog.setCancelable(true);
	    	prog.setOnCancelListener(new ProgCancelListener());
	    	prog.show();
	    }

	    @Override
		public Integer run(ForeignDefList... deflistProviders)
		{
			for(ForeignDefList entry : deflistProviders)
			{
				syncForeignDefList(entry);
			}
			return 0;
		}
		
        @Override
        protected void onCompleted(SelectProfile context, Integer result)
        {
			prog.hide();
        }

        @Override
        protected void onSuccess(SelectProfile context, Integer result)
        {
			context.buildView();
        }

        private void syncForeignDefList(ForeignDefList fgnDefList)
		{
			Reader r;
			try {
				r = fgnDefList.retrieveDefList();
			} catch (Exception e) {
				if(isCancelled()) return;
				Message msg = Message.obtain();
				msg.obj = new Object[] { fgnDefList, e };
				msg.what = ErrorHandler.STATE_RETRIEVE;
				errHandler.sendMessage(msg);
				return;
			}
			if(isCancelled()) return;
			try {
				List<OfxFiDefinition> defList;
			    try {
					defList = fgnDefList.parseDefList(r);
				} catch (Exception e) {
					if(isCancelled()) return;
					Message msg = Message.obtain();
					msg.obj = new Object[] { fgnDefList, e };
					msg.what = ErrorHandler.STATE_PARSE;
					errHandler.sendMessage(msg);
					return;
				}
				if(isCancelled()) return;
				try {
					fgnDefList.sync(defList, db);
				} catch (Exception e) {
					if(isCancelled()) return;
					Message msg = Message.obtain();
					msg.obj = new Object[] { fgnDefList, e };
					msg.what = ErrorHandler.STATE_SYNC;
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
}
