package name.anderson.odysseus.moneytracker.prof;

import java.util.*;
import name.anderson.odysseus.moneytracker.R;
import name.anderson.odysseus.moneytracker.Utilities;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.prof.MsgSetInfo;
import name.anderson.odysseus.moneytracker.ofx.prof.SignonRealm;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class RealmLogin extends ListActivity
{
	private OfxProfile profile;
	
	private static class RealmInfo
	{
		public String name;
		public Set<OfxMessageReq.MessageSet> members;
		
		public RealmInfo(String n)
		{
			name = n;
			members = new TreeSet<OfxMessageReq.MessageSet>();
		}
	}
	
	private static class RealmInfoAdapter extends BaseAdapter
	{
		private Context context;
		private RealmInfo[] realmList;
		
		public RealmInfoAdapter(Context mContext, RealmInfo[] realms)
		{
			context = mContext;
			realmList = realms;
		}

		@Override
		public int getCount()
		{
			return realmList.length;
		}

		@Override
		public Object getItem(int position)
		{
			return realmList[position];
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			RelativeLayout rowLayout;
			RealmInfo rinfo = realmList[position];
			if (convertView == null) {
				rowLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.realm_login_row, parent, false);
			} else {
				rowLayout = (RelativeLayout) convertView;
			}

			String members = null;
			String[] msgsetNames = context.getResources().getStringArray(R.array.message_set);
			for(OfxMessageReq.MessageSet msgset : rinfo.members)
			{
				members = (members == null) ? msgsetNames[msgset.ordinal()] : members + '\n' + msgsetNames[msgset.ordinal()];
			}

			((TextView)rowLayout.findViewById(R.id.Name)).setText(rinfo.name);
			((TextView)rowLayout.findViewById(R.id.TaskList)).setText(members);

			return rowLayout;
		}
	}
	
    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Bundle parms = getIntent().getExtras();
		int fi_id = parms.getInt("prof_id");
		
		DialogInterface.OnClickListener dismissOnOk = new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				cancel();
			}
		};

		ProfileTable db = new ProfileTable(this);
		try
		{
			db.open();
			profile = db.getProfile(fi_id);
		}
		catch(SQLiteException e)
		{
			AlertDialog dlg = Utilities.buildAlert(this, e, "Unable to retrieve profile", "Internal Error", dismissOnOk);
			dlg.show();
			return;
		}
		finally
		{
			db.close();
		}
		if(profile == null)
		{
			AlertDialog dlg = Utilities.buildAlert(this, null, "Could not find profile", "Internal Error", dismissOnOk);
			dlg.show();
			return;
		}
		buildView();
	}
    
	private RealmInfo[] buildRealmList()
	{
		Map<SignonRealm,RealmInfo> realmMembers = new TreeMap<SignonRealm,RealmInfo>();

		for(OfxMessageReq.MessageSet msgset : profile.msgsetMap.keySet())
		{
			MsgSetInfo info = profile.msgsetMap.get(msgset);
			if(info.core.realm != null)
			{
				RealmInfo rinfo;
				if(!realmMembers.containsKey(info.core.realm))
				{
					rinfo = new RealmInfo(info.core.realm.name);
					realmMembers.put(info.core.realm, rinfo);
				} else {
					rinfo = realmMembers.get(info.core.realm);
				}
				rinfo.members.add(msgset);
			}
		}
		
		RealmInfo[] realmList = new RealmInfo[realmMembers.size()];
		int idx = 0;
		for(RealmInfo rinfo : realmMembers.values())
		{
			realmList[idx++] = rinfo;
		}
		return realmList;
	}
	
	private void buildView()
	{
		final RealmInfo[] realmList = buildRealmList();
		RealmInfoAdapter adapter = new RealmInfoAdapter(this, realmList);
		setListAdapter(adapter);

		ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
			{
				realmSelected(realmList[pos]);
			}
		});
	}
		
	private void cancel()
	{
		setResult(RESULT_CANCELED);
		finish();
	}
	
	void realmSelected(RealmInfo realm)
	{
		int _i = 0;
	}
}
