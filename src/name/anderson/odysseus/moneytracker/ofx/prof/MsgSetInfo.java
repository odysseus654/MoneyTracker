/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class MsgSetInfo
{
	public Map<String,MsgCoreBlock> core;

	public MsgSetInfo(OfxMessageReq.MessageSet msgsetId, int ver, TransferObject in, Map<String, SignonRealm> signonList)
	{
		this.core = new TreeMap<String,MsgCoreBlock>();

		Iterator<TransferObject.ObjValue> iter = in.members.iterator();
		while(iter.hasNext())
		{
			TransferObject info = iter.next().child;
			if(info != null && info.name.equals("MSGSETCORE"))
			{
				MsgCoreBlock coreBlock = new MsgCoreBlock(info, signonList);
				this.core.put(coreBlock.URL, coreBlock);
			}
		}
	}
}
