package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.*;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class ProfileMsgResp extends OfxMessageResp
{
	public Map<OfxMessageReq.MessageSet, List<MsgSetInfo>> msgsetList;
	public Map<String, SignonRealm> signonList;
	public Date DtProfileUp;
	public FiDescr descr;
	
	public ProfileMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);

		this.msgsetList = new TreeMap<OfxMessageReq.MessageSet, List<MsgSetInfo>>();
		this.signonList = new TreeMap<String, SignonRealm>();

		TransferObject child = in.getObj("SIGNONINFOLIST");
		if(child != null)
		{
			for(TransferObject.ObjValue obj : child.members)
			{
				TransferObject info = obj.child;
				if(info != null)
				{
					SignonRealm realm = new SignonRealm(info);
					this.signonList.put(realm.name, realm);
				}
			}
		}

		child = in.getObj("MSGSETLIST");
		if(child != null)
		{
			for(TransferObject.ObjValue obj : child.members)
			{
				TransferObject info = obj.child;
				if(info != null && info.name.endsWith("MSGSET"))
				{
					OfxMessageReq.MessageSet msgsetId =
						OfxMessageReq.MessageSet.valueOf(info.name.substring(0, info.name.length()-6));
					List<MsgSetInfo> infoList = parseMsgSetInfoList(msgsetId, info);
					this.msgsetList.put(msgsetId, infoList);
				}
			}
		}

		this.DtProfileUp = TransferObject.parseDate(in.getAttr("DTPROFUP"));
		this.descr = new FiDescr(in);
	}
	
	private List<MsgSetInfo> parseMsgSetInfoList(OfxMessageReq.MessageSet msgsetId, TransferObject in)
	{
		List<MsgSetInfo> members = new LinkedList<MsgSetInfo>();
//		this.maxVer = 0;
		
		final String prefix = msgsetId.name() + "MSGSETV";
		for(TransferObject.ObjValue obj : in.members)
		{
			TransferObject info = obj.child;
			if(info != null && info.name.startsWith(prefix))
			{
				int ver = Integer.parseInt(info.name.substring(prefix.length()));
//				if(this.maxVer < ver) this.maxVer = ver;
				MsgSetInfo msgset = buildMsgSet(msgsetId, ver, info);
				members.add(msgset);
			}
		}
		return members;
	}
	
	private MsgSetInfo buildMsgSet(OfxMessageReq.MessageSet msgsetId, int ver, TransferObject in)
	{
		switch(msgsetId)
		{
// SIGNUP, BANK, CREDITCARD, LOAN, INVSTMT, INTERXFER, WIREXFER, BILLPAY, EMAIL, SECLIST, PRESDIR, PRESDLV, IMAGE
		default: // SIGNON, PROF
			return new MsgSetInfo(msgsetId, ver, in, signonList);
		}
	}
}
