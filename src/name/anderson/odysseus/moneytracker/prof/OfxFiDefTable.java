/**
 * 
 */
package name.anderson.odysseus.moneytracker.prof;

import android.content.ContentValues;
import android.content.Context;
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

	static private class OfxFiDefOpenHelper extends SQLiteOpenHelper
	{
		private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
			"( id integer primary key autoincrement, name text not null, " +
			"url text not null, fi_org text, fi_id text, app_id text, app_ver integer, " +
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

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
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
	}
	
	public Cursor openDefs()
	{
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, "name");
		return c;
	}
	
	public long addDef(OfxFiDefinition def)
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
}
