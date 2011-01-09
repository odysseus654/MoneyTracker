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
	public int ver;
	public MsgCoreBlock core;

	public MsgSetInfo(OfxMessageReq.MessageSet msgsetId, int ver, TransferObject in, Map<String, SignonRealm> signonList)
	{
		this.ver = ver;
		TransferObject coreObj = in.getObj("MSGSETCORE");
		if(coreObj != null) this.core = new MsgCoreBlock(coreObj, signonList);
	}
}
