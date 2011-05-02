/**
 * 
 */
package name.anderson.odysseus.moneytracker.acct;

/**
 * @author Erik
 *
 */

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import java.util.*;

/**
 * @author Erik
 *
 */
public class AcctTables
{
	private SQLiteDatabase db;
	private final Context context;
	private final OfxFiDefOpenHelper dbhelper;
	
	private static final String DATABASE_NAME = "account.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String[] AC_COLS =
	{ "_id", "service_id", "name", "last_update", "curbal_amt", "curbal_date", "availbal_amt", "availbal_date" };

	static private class OfxFiDefOpenHelper extends SQLiteOpenHelper
	{
		private static final String[] CREATE_TABLE =
		{
			"CREATE TABLE acct(" +
			"_id integer primary key autoincrement, service_id integer not null primary key, name text not null, " +
			"last_update text, curbal_amt double, curbal_date text, availbal_amt double, availbal_date text " +
			");"
		};

		private static final String[] DROP_TABLE =
		{
			"DROP TABLE IF EXISTS acct;"
		};

		public OfxFiDefOpenHelper(Context context, String name,	CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			for(String cmd : CREATE_TABLE)
			{
				db.execSQL(cmd);
			}
		}
		
		public void wipeTable(SQLiteDatabase db)
		{
			for(String cmd : DROP_TABLE)
			{
				db.execSQL(cmd);
			}
			onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			wipeTable(db);
		}
	}

	public AcctTables(Context c)
	{
		context = c;
		dbhelper = new OfxFiDefOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void close()
	{
		db.close();
	}
	
	public void openReadable() throws SQLiteException
	{
		db = dbhelper.getReadableDatabase();
//		dbhelper.wipeTable(db);
	}

	public void openWritable() throws SQLiteException
	{
		db = dbhelper.getWritableDatabase();
//		dbhelper.wipeTable(db);
	}

	public Cursor acctList(String constraint)
	{
		if(constraint == null)
		{
			return db.query("acct", AC_COLS, null, null, null, null, "name");
		} else {
			return db.query("acct", AC_COLS, "name like ?", new String[] { "%" + constraint + "%" }, null, null, "name");
		}
	}
	
	public void pushAccount(Account acct)
	{
		if(acct.ID == 0)
		{
			addAccount(acct);
		} else {
			updateAccount(acct);
		}
	}
	
	public Account getAccount(int ID)
	{
		Account acct = null;
		String[] args = { Integer.toString(ID) };
		Cursor cur = db.query("acct", AC_COLS, "_id=?", args, null, null, null);
		try
		{
			if(!cur.moveToNext()) return null;
			acct = getAccountFromCursor(cur);
		}
		finally
		{
			cur.close();
		}

		return acct;
	}
	
	public Account getAccountFromCursor(Cursor cur)
	{
		Account acct = new Account();
		acct.ID = cur.getInt(0);
		acct.serviceId = cur.getInt(1);
		acct.name = cur.getString(2);
		String iAge = cur.getString(3);
		acct.lastUpdate = (iAge != null) ? new Date(Long.parseLong(iAge)) : null;
		acct.curBalAmt = cur.getDouble(4);
		iAge = cur.getString(5);
		acct.curBalDate = (iAge != null) ? new Date(Long.parseLong(iAge)) : null;
		acct.availBalAmt = cur.getDouble(6);
		iAge = cur.getString(7);
		acct.availBalDate = (iAge != null) ? new Date(Long.parseLong(iAge)) : null;
		return acct;
	}

	private int addAccount(Account acct)
	{
		db.beginTransaction();
		int acct_id;
		try
		{
			ContentValues newValue = new ContentValues();
	
			if(acct.service != null)
			{
				if(acct.service.ID != 0) newValue.put("acctid", acct.service.ID);
			} else {
				if(acct.serviceId != 0) newValue.put("acctid", acct.serviceId);
			}
 			newValue.put("name", acct.name);
 			newValue.put("last_update", acct.lastUpdate == null ? null : Long.toString(acct.lastUpdate.getTime()));
 			newValue.put("curbal_amt", acct.curBalAmt);
 			newValue.put("curbal_date", acct.curBalDate == null ? null : Long.toString(acct.curBalDate.getTime()));
 			newValue.put("availbal_amt", acct.availBalAmt);
 			newValue.put("availbal_date", acct.availBalDate == null ? null : Long.toString(acct.availBalDate.getTime()));

			acct_id = (int)db.insertOrThrow("acct", "name", newValue);
			acct.ID = acct_id;
			
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		return acct_id;
	}

	private void updateAccount(Account acct)
	{
		db.beginTransaction();
		try
		{
			ContentValues newValue = new ContentValues();
			
			if(acct.service != null)
			{
				if(acct.service.ID != 0) newValue.put("acctid", acct.service.ID);
			} else {
				if(acct.serviceId != 0) newValue.put("acctid", acct.serviceId);
			}
 			newValue.put("name", acct.name);
 			newValue.put("last_update", acct.lastUpdate == null ? null : Long.toString(acct.lastUpdate.getTime()));
 			newValue.put("curbal_amt", acct.curBalAmt);
 			newValue.put("curbal_date", acct.curBalDate == null ? null : Long.toString(acct.curBalDate.getTime()));
 			newValue.put("availbal_amt", acct.availBalAmt);
 			newValue.put("availbal_date", acct.availBalDate == null ? null : Long.toString(acct.availBalDate.getTime()));

			String[] args = { Integer.toString(acct.ID) };
			db.update("acct", newValue, "_id=?", args);
			
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
	}
}
