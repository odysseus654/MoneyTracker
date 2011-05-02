/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.util.*;

import name.anderson.odysseus.moneytracker.ofx.OfxProfile.Endpoint;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctInfo;
import name.anderson.odysseus.moneytracker.ofx.acct.ServiceAcctName;
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
	private static final int DATABASE_VERSION = 3;

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

	private static final int AC_DETAIL_AVAIL = 1;
	private static final int AC_XFER_SOURCE = 2;
	private static final int AC_XFER_DEST = 4;
	
	private static final String[] FI_COLS =
	{ "lang", "prof_age", "flags", "name", "url", "fi_org", "fi_id", "app_id", "app_ver", "ofx_ver",
		"prof_name", "prof_addr1", "prof_addr2", "prof_addr3", "prof_city", "prof_state", "prof_postal",
		"prof_country", "prof_csphone", "prof_tsphone", "prof_faxphone", "prof_url", "prof_email",
		"parentid", "acct_age" };
	private static final String[] EP_COLS = { "msgset", "ver", "url", "realm", "spname", "flags" };
	private static final String[] RM_COLS =
	{ "name", "min_chars", "max_chars", "char_type", "pass_type", "flags", "user1_label", "user2_label",
		"token_label", "token_url" };
	private static final String[] SS_COLS =
	{ "fi", "userid", "userpass", "user_cred_1", "user_cred_2", "auth_token", "session_key", "session_expire",
		"mfa_answer_key", "session_cookie", "realm" };
	private static final String[] AC_COLS =
	{ "session", "desc", "phone", "type", "flags", "status", "bank_id", "branch_id", "acct_id",
		"acct_type", "acct_key", "_id" };

	static private class OfxFiDefOpenHelper extends SQLiteOpenHelper
	{
		private static final String[] CREATE_TABLE =
		{
			"CREATE TABLE fi(" +
			"_id integer primary key autoincrement, parentid integer, lang text, prof_age text, acct_age text," +
			"flags integer not null, " +
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
			");",
			"CREATE TABLE session (" +
			"_id integer primary key autoincrement, fi integer not null, userid text not null, " +
			"userpass text, user_cred_1 text, user_cred_2 text, auth_token text, session_key text, " +
			"session_expire text, mfa_answer_key text, session_cookie text, realm text" +
			");",
			"CREATE TABLE account (" +
			"_id integer primary key autoincrement, session integer not null, desc text, " +
			"phone text, type text, flags integer, status integer, bank_id text, branch_id text, " +
			"acct_id text, acct_type text, acct_key text" +
			");"
		};

		private static final String[] DROP_TABLE =
		{
			"DROP TABLE IF EXISTS fi;",
			"DROP TABLE IF EXISTS endpoint;",
			"DROP TABLE IF EXISTS realm;",
			"DROP TABLE IF EXISTS session;",
			"DROP TABLE IF EXISTS account;"
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
			profile.parentID = cur.getInt(23);
			profile.lang = cur.getString(0);
			String iAge = cur.getString(1);
			profile.profAge = (iAge != null) ? new Date(Long.parseLong(iAge)) : null;
			iAge = cur.getString(24);
			profile.acctListAge = (iAge != null) ? new Date(Long.parseLong(iAge)) : null;
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
					profile.msgsetMap.put(msgset, info);
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
	
			if(profile.parentID != 0) newValue.put("parentid", profile.parentID);
			newValue.put("lang", profile.lang);
			newValue.put("prof_age", profile.profAge == null ? null : Long.toString(profile.profAge.getTime()));
			newValue.put("acct_age", profile.acctListAge == null ? null : Long.toString(profile.acctListAge.getTime()));

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
	
			if(profile.parentID != 0) newValue.put("parentid", profile.parentID);
			newValue.put("lang", profile.lang);
			newValue.put("prof_age", profile.profAge == null ? null : Long.toString(profile.profAge.getTime()));
			newValue.put("acct_age", profile.acctListAge == null ? null : Long.toString(profile.acctListAge.getTime()));
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

	public void pushSession(LoginSession session)
	{
		if(session.ID == 0)
		{
			addSession(session);
		} else {
			updateSession(session);
		}
	}

	private void updateSession(LoginSession session)
	{
		updateProfile(session.profile);
		db.beginTransaction();
		try
		{
			ContentValues newValue = new ContentValues();

			newValue.put("fi", session.profile.ID);
			if(session.realm != null) newValue.put("realm", session.realm.name);
			newValue.put("userid", session.userid);
			newValue.put("userpass", session.userpass);
			newValue.put("user_cred_1", session.userCred1);
			newValue.put("user_cred_2", session.userCred2);
			newValue.put("auth_token", session.authToken);
			newValue.put("session_key", session.sessionkey);
			if(session.sessionExpire != null) newValue.put("session_expire", Long.toString(session.sessionExpire.getTime()));
			newValue.put("mfa_answer_key", session.mfaAnswerKey);
			newValue.put("session_cookie", session.sessionCookie);
	
			String[] args = { Integer.toString(session.ID) };
			db.update("session", newValue, "_id=?", args);
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
	}

	private int addSession(LoginSession session)
	{
		pushProfile(session.profile);
		db.beginTransaction();
		int sess_id;
		try
		{
			ContentValues newValue = new ContentValues();

			newValue.put("fi", session.profile.ID);
			if(session.realm != null) newValue.put("realm", session.realm.name);
			newValue.put("userid", session.userid);
			newValue.put("userpass", session.userpass);
			newValue.put("user_cred_1", session.userCred1);
			newValue.put("user_cred_2", session.userCred2);
			newValue.put("auth_token", session.authToken);
			newValue.put("session_key", session.sessionkey);
			if(session.sessionExpire != null) newValue.put("session_expire", Long.toString(session.sessionExpire.getTime()));
			newValue.put("mfa_answer_key", session.mfaAnswerKey);
			newValue.put("session_cookie", session.sessionCookie);

			sess_id = (int)db.insertOrThrow("session", "fi", newValue);
			session.ID = sess_id;
			
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		return sess_id;
	}
	
	public LoginSession getSession(OfxProfile profile, int ID)
	{
		String[] args = { Integer.toString(profile.ID), Integer.toString(ID) };
		Cursor cur = db.query("session", SS_COLS, "fi=? and _id=?", args, null, null, null);
		try
		{
			if(!cur.moveToNext()) return null;
			LoginSession session = new LoginSession();
			session.profile = profile;
			session.ID = ID;
			session.userid = cur.getString(1);
			session.userpass = cur.getString(2);
			session.userCred1 = cur.getString(3);
			session.userCred2 = cur.getString(4);
			session.authToken = cur.getString(5);
			session.sessionkey = cur.getString(6);
			String iExpire = cur.getString(7);
			if(iExpire != null) session.sessionExpire = new Date(Long.parseLong(iExpire));
			session.mfaAnswerKey = cur.getString(8);
			session.sessionCookie = cur.getString(9);
			String realmName = cur.getString(10);
			if(realmName != null && profile.realms != null && profile.realms.containsKey(realmName))
			{
				session.realm = profile.realms.get(realmName);
			}

			return session;
		}
		finally
		{
			cur.close();
		}
	}

	public LoginSession getSession(int ID)
	{
		String[] args = { Integer.toString(ID) };
		Cursor cur = db.query("session", SS_COLS, "_id=?", args, null, null, null);
		try
		{
			if(!cur.moveToNext()) return null;
			LoginSession session = new LoginSession();
			OfxProfile profile = getProfile(cur.getInt(0));
			session.profile = profile;
			session.ID = ID;
			session.userid = cur.getString(1);
			session.userpass = cur.getString(2);
			session.userCred1 = cur.getString(3);
			session.userCred2 = cur.getString(4);
			session.authToken = cur.getString(5);
			session.sessionkey = cur.getString(6);
			String iExpire = cur.getString(7);
			if(iExpire != null) session.sessionExpire = new Date(Long.parseLong(iExpire));
			session.mfaAnswerKey = cur.getString(8);
			session.sessionCookie = cur.getString(9);
			String realmName = cur.getString(10);
			if(realmName != null && profile.realms != null && profile.realms.containsKey(realmName))
			{
				session.realm = profile.realms.get(realmName);
			}
			return session;
		}
		finally
		{
			cur.close();
		}
	}

	public void pushAccount(ServiceAcctInfo acct)
	{
		if(acct.ID == 0)
		{
			addAccount(acct);
		} else {
			updateAccount(acct);
		}
	}

	private void updateAccount(ServiceAcctInfo acct)
	{
		updateSession(acct.session);
		db.beginTransaction();
		try
		{
			int flags = 0;
			if(acct.detailAvail) flags = flags | AC_DETAIL_AVAIL;
			if(acct.xferSource) flags = flags | AC_XFER_SOURCE;
			if(acct.xferDest) flags = flags | AC_XFER_DEST;

			ContentValues newValue = new ContentValues();
			newValue.put("session", acct.session.ID);
			newValue.put("desc", acct.desc);
			newValue.put("flags", flags);
			newValue.put("phone", acct.phone);
			newValue.put("type", acct.type.toString());
			newValue.put("status", acct.status);
			if(acct.name != null)
			{
				newValue.put("bank_id", acct.name.bankId);
				newValue.put("branch_id", acct.name.branchId);
				newValue.put("acct_id", acct.name.acctId);
				newValue.put("acct_type", acct.name.acctType);
				newValue.put("acct_key", acct.name.acctKey);
			} else {
				newValue.putNull("bank_id");
				newValue.putNull("branch_id");
				newValue.putNull("acct_id");
				newValue.putNull("acct_type");
				newValue.putNull("acct_key");
			}

			String[] args = { Integer.toString(acct.ID) };
			db.update("account", newValue, "_id=?", args);
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
	}

	private int addAccount(ServiceAcctInfo acct)
	{
		pushSession(acct.session);
		db.beginTransaction();
		int acc_id;
		try
		{
			ContentValues newValue = new ContentValues();

			int flags = 0;
			if(acct.detailAvail) flags = flags | AC_DETAIL_AVAIL;
			if(acct.xferSource) flags = flags | AC_XFER_SOURCE;
			if(acct.xferDest) flags = flags | AC_XFER_DEST;

			newValue.put("session", acct.session.ID);
			newValue.put("desc", acct.desc);
			newValue.put("flags", flags);
			newValue.put("phone", acct.phone);
			newValue.put("type", acct.type.toString());
			newValue.put("status", acct.status);
			if(acct.name != null)
			{
				newValue.put("bank_id", acct.name.bankId);
				newValue.put("branch_id", acct.name.branchId);
				newValue.put("acct_id", acct.name.acctId);
				newValue.put("acct_type", acct.name.acctType);
				newValue.put("acct_key", acct.name.acctKey);
			} else {
				newValue.putNull("bank_id");
				newValue.putNull("branch_id");
				newValue.putNull("acct_id");
				newValue.putNull("acct_type");
				newValue.putNull("acct_key");
			}

			acc_id = (int)db.insertOrThrow("account", "fi", newValue);
			acct.ID = acc_id;
			
			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}
		return acc_id;
	}

	public ServiceAcctInfo getAccount(int ID)
	{
		String[] args = { Integer.toString(ID) };
		Cursor cur = db.query("account", AC_COLS, "_id=?", args, null, null, null);
		try
		{
			if(!cur.moveToNext()) return null;
			ServiceAcctInfo acct = new ServiceAcctInfo();
			LoginSession session = getSession(cur.getInt(0));
			acct.session = session;
			acct.ID = ID;
			acct.desc = cur.getString(1);
			acct.phone = cur.getString(2);
			acct.type = ServiceAcctName.ServiceType.valueOf(cur.getString(3));
			int flags = cur.getInt(4);
			acct.status = cur.getInt(5);
			acct.detailAvail = (flags & AC_DETAIL_AVAIL) != 0;
			acct.xferSource = (flags & AC_XFER_SOURCE) != 0;
			acct.xferDest = (flags & AC_XFER_DEST) != 0;

			acct.name.type = acct.type;
			acct.name.bankId = cur.getString(6);
			acct.name.branchId = cur.getString(7);
			acct.name.acctId = cur.getString(8);
			acct.name.acctType = cur.getString(9);
			acct.name.acctKey = cur.getString(10);

			return acct;
		}
		finally
		{
			cur.close();
		}
	}
	
	public List<ServiceAcctInfo> getAccountsBySession(int ID)
	{
		String[] args = { Integer.toString(ID) };
		Cursor cur = db.query("account", AC_COLS, "session=?", args, null, null, null);
		List<ServiceAcctInfo> results = new LinkedList<ServiceAcctInfo>();
		try
		{
			while(cur.moveToNext())
			{
				ServiceAcctInfo acct = new ServiceAcctInfo();
				LoginSession session = getSession(cur.getInt(0));
				acct.session = session;
				acct.ID = ID;
				acct.desc = cur.getString(1);
				acct.phone = cur.getString(2);
				acct.type = ServiceAcctName.ServiceType.valueOf(cur.getString(3));
				int flags = cur.getInt(4);
				acct.status = cur.getInt(5);
				acct.detailAvail = (flags & AC_DETAIL_AVAIL) != 0;
				acct.xferSource = (flags & AC_XFER_SOURCE) != 0;
				acct.xferDest = (flags & AC_XFER_DEST) != 0;
	
				acct.name.type = acct.type;
				acct.name.bankId = cur.getString(6);
				acct.name.branchId = cur.getString(7);
				acct.name.acctId = cur.getString(8);
				acct.name.acctType = cur.getString(9);
				acct.name.acctKey = cur.getString(10);
				results.add(acct);
			}
			return results.size() == 0 ? null : results;
		}
		finally
		{
			cur.close();
		}
	}

	public void syncAccounts(LoginSession session, List<ServiceAcctInfo> accts)
	{
		// first figure out what we've got
		Map<ServiceAcctName,Integer> vals = new TreeMap<ServiceAcctName,Integer>();
		String[] queryArgs = { Integer.toString(session.ID) };
		Cursor cur = db.query("account", AC_COLS, "session=?", queryArgs, null, null, null);
		while(cur.moveToNext())
		{
			ServiceAcctName name = new ServiceAcctName();
			name.type = ServiceAcctName.ServiceType.valueOf(cur.getString(3));
			name.bankId = cur.getString(6);
			name.branchId = cur.getString(7);
			name.acctId = cur.getString(8);
			name.acctType = cur.getString(9);
			name.acctKey = cur.getString(10);
			int ID = cur.getInt(11);
			if(!vals.containsKey(name)) vals.put(name, ID);
		}
		cur.close();
		
		for(ServiceAcctInfo acct : accts)
		{
			if(acct.name != null)
			{
				if(acct.session == null)
				{
					acct.session = session;
				}
				if(vals.containsKey(acct.name))
				{
					acct.ID = vals.get(acct.name);
					vals.remove(acct.name);
					updateAccount(acct);
				} else {
					addAccount(acct);
				}
			}
		}
		
		for(Integer delAcct : vals.values())
		{
			String[] args = { delAcct.toString() };
			db.execSQL("DELETE FROM account WHERE id=?", args);
		}
	}
}
