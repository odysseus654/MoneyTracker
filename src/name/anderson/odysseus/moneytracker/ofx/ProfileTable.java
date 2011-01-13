/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.OfxProfile.Endpoint;
import name.anderson.odysseus.moneytracker.ofx.prof.*;
import name.anderson.odysseus.moneytracker.prof.OfxFiDefinition;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * @author Erik
 *
 */
public class ProfileTable
{
	private SQLiteDatabase db;
	private final Context context;
	private final OfxFiDefOpenHelper dbhelper;
	
	private static final String DATABASE_NAME = "profiles.db";
	private static final int DATABASE_VERSION = 1;

	private static final int FI_SIMPLE_PROF = 1;
	private static final int FI_USE_EXPECT_CONTINUE = 2;
	private static final int FI_IGNORE_ENCRYPTION = 4;
	private static final int FI_PROFILE_IS_USER = 8;
	
	private static final int EP_SECURE_PASS = 1;
	private static final int EP_SUPPORTS_REFRESH = 2;
	private static final int EP_SUPPORTS_RECOVERY = 4;
	private static final int EP_SUPPORTS_FULL_SYNC = 8;

	private static final int RM_CASE_SENSITIVE = 1;
	private static final int RM_SPECIAL_ALLOWED = 2;
	private static final int RM_SPACES_ALLOWED = 4;
	private static final int RM_CHANGE_PASS_ALLOWED = 8;
	private static final int RM_CHANGE_PASS_FIRST = 16;
	private static final int RM_CLIENT_UID_REQ = 32;
	private static final int RM_AUTH_TOKEN_FIRST = 64;
	private static final int RM_MFA_SUPPORTED = 128;
	private static final int RM_MFA_FIRST = 256;
	
	private static final String[] FI_COLS =
	{ "lang", "prof_age", "flags", "name", "url", "fi_org", "fi_id", "app_id", "app_ver", "ofx_ver",
		"prof_name", "prof_addr1", "prof_addr2", "prof_addr3", "prof_city", "prof_state", "prof_postal",
		"prof_country", "prof_csphone", "prof_tsphone", "prof_faxphone", "prof_url", "prof_email" };
	private static final String[] EP_COLS = { "msgset", "ver", "url", "realm", "spname", "flags" };
	private static final String[] RM_COLS =
	{ "name", "min_chars", "max_chars", "char_type", "pass_type", "flags", "user1_label", "user2_label",
		"token_label", "token_url" };

	static private class OfxFiDefOpenHelper extends SQLiteOpenHelper
	{
		private static final String[] CREATE_TABLE =
		{
			"CREATE TABLE fi(" +
			"_id integer primary key autoincrement, parentid integer, lang text, prof_age text, flags integer not null, " +
			// fidef
			"name text not null, url text not null, fi_org text, fi_id text, app_id text, app_ver integer, " +
			"ofx_ver float not null, " + //, simple_prof integer, " +
			// descr
			"prof_name text, prof_addr1 text, prof_addr2 text, prof_addr3 text, prof_city text, " +
			"prof_state text, prof_postal text, prof_country text, prof_csphone text, prof_tsphone text, " +
			"prof_faxphone text, prof_url text, prof_email text " +
			");",
			"CREATE TABLE endpoint (" +
			"_id integer primary key autoincrement, fi integer not null, msgset text not null, " +
			"ver integer not null, url text not null, flags integer not null, realm text not null, " +
			"spname text" +
			");",
			"CREATE TABLE realm (" +
			"_id integer primary key autoincrement, fi integer not null, name text not null, " +
			"min_chars integer, max_chars integer, char_type integer, flags integer, pass_type integer, " +
			"user1_label text, user2_label text, token_label text, token_url text" +
			");"
		};
		private static final String[] DROP_TABLE =
		{
			"DROP TABLE IF EXISTS fi;",
			"DROP TABLE IF EXISTS endpoint;",
			"DROP TABLE IF EXISTS realm;"
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

	public ProfileTable(Context c)
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
/*
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
			if(!cur.moveToNext()) return null;
			
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
*/
	public void pushProfile(OfxProfile profile)
	{
		if(profile.ID == 0)
		{
			addProfile(profile);
		} else {
			updateProfile(profile);
		}
	}
	
	public OfxProfile getProfile(int ID)
	{
		OfxProfile profile = null;
		String[] args = { Integer.toString(ID) };
		Cursor cur = db.query("fi", FI_COLS, "_id=?", args, null, null, null);
		try
		{
			if(!cur.moveToNext()) return null;
			int flags = cur.getInt(2);
			
			OfxFiDefinition fidef = new OfxFiDefinition();
			fidef.name = cur.getString(3);
			fidef.fiURL = cur.getString(4);
			fidef.fiOrg = cur.getString(5);
			fidef.fiID = cur.getString(6);
			fidef.appId = cur.getString(7);
			fidef.appVer = cur.getInt(8);
			fidef.ofxVer = cur.getFloat(9);
			fidef.simpleProf = (flags & FI_SIMPLE_PROF) != 0;

			profile = new OfxProfile(fidef);
			profile.ID = ID;
			profile.lang = cur.getString(0);
			String iAge = cur.getString(1);
			if(iAge != null) profile.profAge = new Date(Long.parseLong(iAge));
			profile.useExpectContinue = (flags & FI_USE_EXPECT_CONTINUE) != 0;
			profile.ignoreEncryption = (flags & FI_IGNORE_ENCRYPTION) != 0;
			profile.profileIsUser = (flags & FI_PROFILE_IS_USER) != 0;

			FiDescr descr = new FiDescr();
			descr.FIName = cur.getString(10);
			if(descr.FIName != null)
			{
				descr.Addr1 = cur.getString(11);
				descr.Addr2 = cur.getString(12);
				descr.Addr3 = cur.getString(13);
				descr.City = cur.getString(14);
				descr.State = cur.getString(15);
				descr.PostalCode = cur.getString(16);
				descr.Country = cur.getString(17);
				descr.CSPhone = cur.getString(18);
				descr.TSPhone = cur.getString(19);
				descr.FaxPhone = cur.getString(20);
				descr.URL = cur.getString(21);
				descr.Email = cur.getString(22);
				profile.fidescr = descr;
			}
		}
		finally
		{
			cur.close();
		}

		cur = db.query("realm", RM_COLS, "fi=?", args, null, null, null);
		try
		{
			while(cur.moveToNext())
			{
				SignonRealm realm = new SignonRealm();
				realm.name = cur.getString(0);
				realm.minChars = cur.getInt(1);
				realm.maxChars = cur.getInt(2);
				realm.chartype = cur.getInt(3);
				realm.passType = cur.getInt(4);
				realm.userCred1Label = cur.getString(6);
				realm.userCred2Label = cur.getString(7);
				realm.authTokenLabel = cur.getString(8);
				realm.authTokenInfoURL = cur.getString(9);

				int flags = cur.getInt(5);
				realm.caseSensitive = (flags & RM_CASE_SENSITIVE) != 0;
				realm.specialAllowed = (flags & RM_SPECIAL_ALLOWED) != 0;
				realm.spacesAllowed = (flags & RM_SPACES_ALLOWED) != 0;
				realm.changePassAllowed = (flags & RM_CHANGE_PASS_ALLOWED) != 0;
				realm.changePassFirst = (flags & RM_CHANGE_PASS_FIRST) != 0;
				realm.clientUidReq = (flags & RM_CLIENT_UID_REQ) != 0;
				realm.authTokenFirst = (flags & RM_AUTH_TOKEN_FIRST) != 0;
				realm.mfaSupported = (flags & RM_MFA_SUPPORTED) != 0;
				realm.mfaFirst = (flags & RM_MFA_FIRST) != 0;
				if(profile.realms == null) profile.realms = new TreeMap<String,SignonRealm>();
				profile.realms.put(realm.name, realm);
			}
		}
		finally
		{
			cur.close();
		}

		cur = db.query("endpoint", EP_COLS, "fi=?", args, null, null, null);
		try
		{
			while(cur.moveToNext())
			{
				// probably need to support more types at some point
				MsgSetInfo info = new MsgSetInfo();
				info.ver = cur.getInt(1);
				info.URL = cur.getString(2);
				info.SpName = cur.getString(4);
				int flags = cur.getInt(5);
				info.securePass = (flags & EP_SECURE_PASS) != 0;
				info.supportsRefresh = (flags & EP_SUPPORTS_REFRESH) != 0;
				info.supportsRecovery = (flags & EP_SUPPORTS_RECOVERY) != 0;
				info.fullSyncMode = (flags & EP_SUPPORTS_FULL_SYNC) != 0;
				String realmName = cur.getString(3);
				if(profile.realms != null && profile.realms.containsKey(realmName))
				{
					info.realm = profile.realms.get(realmName);
				}
				
				OfxMessageReq.MessageSet msgset = OfxMessageReq.MessageSet.valueOf(cur.getString(0));
				if(msgset != null)
				{
					if(profile.endpoints == null) profile.endpoints = new TreeMap<String,Endpoint>();
					if(profile.msgsetMap == null) profile.msgsetMap = new TreeMap<OfxMessageReq.MessageSet, MsgSetInfo>();

					Endpoint ep;
					if(profile.endpoints.containsKey(info.URL))
					{
						ep = profile.endpoints.get(info.URL);
					} else {
						ep = new Endpoint();
						profile.endpoints.put(info.URL, ep);
					}
					ep.msgsetInfo.put(msgset, info);
					
					if(msgset != OfxMessageReq.MessageSet.SIGNON && msgset != OfxMessageReq.MessageSet.SIGNUP)
					{
						profile.msgsetMap.put(msgset, info);
					}
				}
			}
		}
		finally
		{
			cur.close();
		}

		return profile;
	}

	private int addProfile(OfxProfile profile)
	{
		db.beginTransaction();
		int fi_id;
		try
		{
			ContentValues newValue = new ContentValues();
	//		newValue.put("parentid", ...);
			int flags = 0;
			if(profile.fidef.simpleProf) flags = flags | FI_SIMPLE_PROF;
			if(profile.useExpectContinue) flags = flags | FI_USE_EXPECT_CONTINUE;
			if(profile.ignoreEncryption) flags = flags | FI_IGNORE_ENCRYPTION;
			if(profile.profileIsUser) flags = flags | FI_PROFILE_IS_USER;
	
			newValue.put("lang", profile.lang);
			newValue.put("prof_age", Long.toString(profile.profAge.getTime()));
			newValue.put("flags", flags);
			newValue.put("name", profile.fidef.name);
			newValue.put("url", profile.fidef.fiURL);
			newValue.put("fi_org", profile.fidef.fiOrg);
			newValue.put("fi_id", profile.fidef.fiID);
			newValue.put("app_id", profile.fidef.appId);
			newValue.put("app_ver", profile.fidef.appVer);
			newValue.put("ofx_ver", profile.fidef.ofxVer);
			if(profile.fidescr != null)
			{
				newValue.put("prof_name", profile.fidescr.FIName);
				newValue.put("prof_addr1", profile.fidescr.Addr1);
				newValue.put("prof_addr2", profile.fidescr.Addr2);
				newValue.put("prof_addr3", profile.fidescr.Addr3);
				newValue.put("prof_city", profile.fidescr.City);
				newValue.put("prof_state", profile.fidescr.State);
				newValue.put("prof_postal", profile.fidescr.PostalCode);
				newValue.put("prof_country", profile.fidescr.Country);
				newValue.put("prof_csphone", profile.fidescr.CSPhone);
				newValue.put("prof_tsphone", profile.fidescr.TSPhone);
				newValue.put("prof_faxphone", profile.fidescr.FaxPhone);
				newValue.put("prof_url", profile.fidescr.URL);
				newValue.put("prof_email", profile.fidescr.Email);
			}
			fi_id = (int)db.insertOrThrow("fi", "name", newValue);
			profile.ID = fi_id;
			
			refreshChildData(profile);
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		return fi_id;
	}
	
	private void refreshChildData(OfxProfile profile)
	{
		String[] args = { Integer.toString(profile.ID) };
		db.execSQL("DELETE FROM endpoint WHERE fi=?;", args);
		db.execSQL("DELETE FROM realm WHERE fi=?;", args);
		if(profile.endpoints != null)
		{
			for(OfxProfile.Endpoint endpoint : profile.endpoints.values())
			{
				for(OfxMessageReq.MessageSet msgset : endpoint.msgsetInfo.keySet())
				{
					MsgSetInfo info = endpoint.msgsetInfo.get(msgset);

					ContentValues newValue = new ContentValues();
					newValue.put("fi", profile.ID);
					newValue.put("msgset", msgset.name());
					newValue.put("ver", info.ver);
					newValue.put("url", info.URL);
					if(info.realm != null) newValue.put("realm", info.realm.name);
					newValue.put("spname", info.SpName);

					int flags = 0;
					if(info.securePass) flags = flags | EP_SECURE_PASS;
					if(info.supportsRefresh) flags = flags | EP_SUPPORTS_REFRESH;
					if(info.supportsRecovery) flags = flags | EP_SUPPORTS_RECOVERY;
					if(info.fullSyncMode) flags = flags | EP_SUPPORTS_FULL_SYNC;
					newValue.put("flags", flags);

					db.insertOrThrow("endpoint", "msgset", newValue);
				}
			}
		}

		if(profile.realms != null)
		{
			for(SignonRealm realm : profile.realms.values())
			{
				ContentValues newValue = new ContentValues();
				newValue.put("fi", profile.ID);
				newValue.put("name", realm.name);
				newValue.put("min_chars", realm.minChars);
				newValue.put("max_chars", realm.maxChars);
				newValue.put("char_type", realm.chartype);
				newValue.put("pass_type", realm.passType);
				newValue.put("user1_label", realm.userCred1Label);
				newValue.put("user2_label", realm.userCred2Label);
				newValue.put("token_label", realm.authTokenLabel);
				newValue.put("token_url", realm.authTokenInfoURL);

				int flags = 0;
				if(realm.caseSensitive) flags = flags | RM_CASE_SENSITIVE;
				if(realm.specialAllowed) flags = flags | RM_SPECIAL_ALLOWED;
				if(realm.spacesAllowed) flags = flags | RM_SPACES_ALLOWED;
				if(realm.changePassAllowed) flags = flags | RM_CHANGE_PASS_ALLOWED;
				if(realm.changePassFirst) flags = flags | RM_CHANGE_PASS_FIRST;
				if(realm.clientUidReq) flags = flags | RM_CLIENT_UID_REQ;
				if(realm.authTokenFirst) flags = flags | RM_AUTH_TOKEN_FIRST;
				if(realm.mfaSupported) flags = flags | RM_MFA_SUPPORTED;
				if(realm.mfaFirst) flags = flags | RM_MFA_FIRST;
				newValue.put("flags", flags);

				db.insertOrThrow("realm", "name", newValue);
			}
		}
	}
	
	private void updateProfile(OfxProfile profile)
	{
		db.beginTransaction();
		try
		{
			ContentValues newValue = new ContentValues();
	//		newValue.put("parentid", ...);
			int flags = 0;
			if(profile.fidef.simpleProf) flags = flags | FI_SIMPLE_PROF;
			if(profile.useExpectContinue) flags = flags | FI_USE_EXPECT_CONTINUE;
			if(profile.ignoreEncryption) flags = flags | FI_IGNORE_ENCRYPTION;
			if(profile.profileIsUser) flags = flags | FI_PROFILE_IS_USER;
	
			newValue.put("lang", profile.lang);
			newValue.put("prof_age", profile.profAge.getTime());
			newValue.put("flags", flags);
			newValue.put("name", profile.fidef.name);
			newValue.put("url", profile.fidef.fiURL);
			newValue.put("fi_org", profile.fidef.fiOrg);
			newValue.put("fi_id", profile.fidef.fiID);
			newValue.put("app_id", profile.fidef.appId);
			newValue.put("app_ver", profile.fidef.appVer);
			newValue.put("ofx_ver", profile.fidef.ofxVer);
			if(profile.fidescr != null)
			{
				newValue.put("prof_name", profile.fidescr.FIName);
				newValue.put("prof_addr1", profile.fidescr.Addr1);
				newValue.put("prof_addr2", profile.fidescr.Addr2);
				newValue.put("prof_addr3", profile.fidescr.Addr3);
				newValue.put("prof_city", profile.fidescr.City);
				newValue.put("prof_state", profile.fidescr.State);
				newValue.put("prof_postal", profile.fidescr.PostalCode);
				newValue.put("prof_country", profile.fidescr.Country);
				newValue.put("prof_csphone", profile.fidescr.CSPhone);
				newValue.put("prof_tsphone", profile.fidescr.TSPhone);
				newValue.put("prof_faxphone", profile.fidescr.FaxPhone);
				newValue.put("prof_url", profile.fidescr.URL);
				newValue.put("prof_email", profile.fidescr.Email);
			}
	
			String[] args = { Integer.toString(profile.ID) };
			db.update("fi", newValue, "_id=?", args);
			
			refreshChildData(profile);
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
	}
}
