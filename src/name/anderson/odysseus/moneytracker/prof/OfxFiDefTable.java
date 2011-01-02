/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import java.util.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * @author Erik
 *
 */
public class OfxFiDefTable
{
	private SQLiteDatabase db;
	private final Context context;
	private final OfxFiDefOpenHelper dbhelper;
	
	private static final String DATABASE_NAME = "profile";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "def";
	private static final String[] COLS_SRCID = {"src_id"};
	private static final String[] COLS_LIST = {"_id", "name"};
	private static final String[] COLS_DEF =
		{ "name", "url", "fi_org", "fi_id", "app_id", "app_ver", "ofx_ver", "simple_prof", "src_name", "src_id" };

	static private class OfxFiDefOpenHelper extends SQLiteOpenHelper
	{
		private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
			"( _id integer primary key autoincrement, name text not null, " +
			"url text, fi_org text, fi_id text, app_id text, app_ver integer, " +
			"ofx_ver float, simple_prof integer, src_name text, src_id text);";
			
		public OfxFiDefOpenHelper(Context context, String name,	CursorFactory factory, int version)
		{
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_TABLE);
		}
		
		public void wipeTable(SQLiteDatabase db)
		{
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			wipeTable(db);
		}
	}

	public OfxFiDefTable(Context c)
	{
		context = c;
		dbhelper = new OfxFiDefOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void close()
	{
		db.close();
	}
	
	public void open() throws SQLiteException
	{
		try {
			db = dbhelper.getWritableDatabase();
		}
		catch(SQLiteException ex)
		{
			db = dbhelper.getReadableDatabase();
		}
//		dbhelper.wipeTable(db);
	}

	public Cursor defList(String constraint)
	{
		if(constraint == null)
		{
			return db.query(TABLE_NAME, COLS_LIST, null, null, null, null, "name");
		} else {
			return db.query(TABLE_NAME, COLS_LIST, "name like ?", new String[] { "%" + constraint + "%" }, null, null, "name");
		}
	}
	
	public OfxFiDefinition getDefById(int id)
	{
		Cursor cur = db.query(TABLE_NAME, COLS_DEF, "_id=?", new String[] { Long.toString(id) }, null, null, null);
		try
		{
			if(cur == null || cur.isAfterLast()) return null;
			
			OfxFiDefinition def = new OfxFiDefinition();
			def.defID = id;
			def.name = cur.getString(0);
			def.fiURL = cur.getString(1);
			def.fiOrg = cur.getString(2);
			def.fiID = cur.getString(3);
			def.appId = cur.getString(4);
			def.appVer = cur.getInt(5);
			def.ofxVer = cur.getFloat(6);
			def.simpleProf = cur.getInt(7) == 1;
			def.srcName = cur.getString(8);
			def.srcId = cur.getString(9);
	
			return def;
		}
		finally
		{
			cur.close();
		}
	}

	private long addDef(OfxFiDefinition def)
	{
		ContentValues newValue = new ContentValues();
		newValue.put("name", def.name);
		newValue.put("url", def.fiURL);
		newValue.put("fi_org", def.fiOrg);
		newValue.put("fi_id", def.fiID);
		newValue.put("app_id", def.appId);
		newValue.put("app_ver", def.appVer);
		newValue.put("ofx_ver", def.ofxVer);
		newValue.put("simple_prof", def.simpleProf ? 1 : 0);
		newValue.put("src_name", def.srcName);
		newValue.put("src_id", def.srcId);
		return db.insert(TABLE_NAME, null, newValue);
	}
	
	private void updateDefBySource(OfxFiDefinition def)
	{
		ContentValues newValue = new ContentValues();
		newValue.put("name", def.name);
		newValue.put("url", def.fiURL);
		newValue.put("fi_org", def.fiOrg);
		newValue.put("fi_id", def.fiID);
		newValue.put("app_id", def.appId);
		newValue.put("app_ver", def.appVer);
		newValue.put("ofx_ver", def.ofxVer);
		newValue.put("simple_prof", def.simpleProf ? 1 : 0);

		String[] args = { def.srcName, def.srcId };
		db.update(TABLE_NAME, newValue, "src_name=? and src_id=?", args);
	}

	public void sync(List<OfxFiDefinition> defs, String srcName, boolean doUpdates)
	{
		// first figure out what we've got
		Set<String> vals = new TreeSet<String>();
		String[] queryArgs = { srcName };
		Cursor cur = db.query(TABLE_NAME, COLS_SRCID, "src_name=?", queryArgs, null, null, null);
		if(cur != null && !cur.isAfterLast())
		{
			do
			{
				String val = cur.getString(0);
				if(val != null && !vals.contains(val))
				{
					vals.add(val);
				}
			}
			while(cur.moveToNext());
			cur.close();
		}
		
		for(OfxFiDefinition def : defs)
		{
			if(def.srcId != null)
			{
				if(vals.contains(def.srcId))
				{
					vals.remove(def.srcId);
					if(doUpdates)
					{
						updateDefBySource(def);
					}
				} else {
					addDef(def);
				}
			}
		}
		
		for(String delDef : vals)
		{
			String[] args = { srcName, delDef };
			db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE src_name=? and src_id=?", args);
		}
	}

	public boolean requiresUpdate()
	{
		Cursor c = defList(null);
		try
		{
			if(c != null && c.isAfterLast()) return true;
	
			return false;
		}
		finally
		{
			c.close();
		}
	}
}
