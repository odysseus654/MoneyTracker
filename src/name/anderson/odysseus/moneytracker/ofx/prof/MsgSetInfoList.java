package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

public class MsgSetInfoList
{
	public Map<Integer, MsgSetInfo> members;
/*
	public MsgSetInfoList()
	{
	}
*/
	public MsgSetInfoList(OfxMessageReq.MessageSet msgsetId, TransferObject in, Map<String, SignonRealm> signonList)
	{
		this.members = new TreeMap<Integer, MsgSetInfo>();
		
		final String prefix = msgsetId.name() + "MSGSETV";
		Iterator<TransferObject.ObjValue> iter = in.members.iterator();
		while(iter.hasNext())
		{
			TransferObject info = iter.next().child;
			if(info != null && info.name.startsWith(prefix))
			{
				int ver = Integer.parseInt(info.name.substring(prefix.length()));
/*				Map<String,MsgSetInfo> verMembers;
				if(this.members.containsKey(ver))
				{
					verMembers = this.members.get(ver);
				} else {
					verMembers = new TreeMap<String,MsgSetInfo>();
					this.members.put(ver, verMembers);
				}*/
				MsgSetInfo msgset = buildMsgSet(msgsetId, ver, info, signonList);
				this.members.put(ver, msgset);
			}
		}
	}
	
	private MsgSetInfo buildMsgSet(OfxMessageReq.MessageSet msgsetId, int ver,
			TransferObject in, Map<String, SignonRealm> signonList)
	{
		switch(msgsetId)
		{
		default:
			return new MsgSetInfo(msgsetId, ver, in, signonList);
		}
	}
}
