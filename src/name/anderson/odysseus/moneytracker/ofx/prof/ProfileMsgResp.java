package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik
 *
 */
public class ProfileMsgResp extends OfxMessageResp
{
	public Map<OfxMessageReq.MessageSet, MsgSetInfoList> msgsetList;
	public Map<String, SignonRealm> signonList;
	public Date DtProfileUp;
	public String FIName;
	public String Addr1;
	public String Addr2;
	public String Addr3;
	public String City;
	public String State;
	public String PostalCode;
	public String Country;
	public String CSPhone;
	public String TSPhone;
	public String FaxPhone;
	public String URL;
	public String Email;
/*
	public ProfileMsgResp()
	{
	}
*/
	public ProfileMsgResp(TransferObject tran, TransferObject in)
	{
		if(tran != null) this.trn = new TransactionResp(tran);

		this.msgsetList = new TreeMap<OfxMessageReq.MessageSet, MsgSetInfoList>();
		this.signonList = new TreeMap<String, SignonRealm>();

		TransferObject child = in.getObj("SIGNONINFOLIST");
		if(child != null)
		{
			Iterator<TransferObject.ObjValue> iter = child.members.iterator();
			while(iter.hasNext())
			{
				TransferObject info = iter.next().child;
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
			Iterator<TransferObject.ObjValue> iter = child.members.iterator();
			while(iter.hasNext())
			{
				TransferObject info = iter.next().child;
				if(info != null && info.name.endsWith("MSGSET"))
				{
					OfxMessageReq.MessageSet msgsetId =
						OfxMessageReq.MessageSet.valueOf(info.name.substring(0, info.name.length()-6));
					MsgSetInfoList realm = new MsgSetInfoList(msgsetId, info, this.signonList);
					this.msgsetList.put(msgsetId, realm);
				}
			}
		}

		this.DtProfileUp = TransferObject.parseDate(in.getAttr("DTPROFUP"));;
		this.FIName = in.getAttr("FINAME");
		this.Addr1 = in.getAttr("ADDR1");
		this.Addr2 = in.getAttr("ADDR2");
		this.Addr3 = in.getAttr("ADDR3");
		this.City = in.getAttr("CITY");
		this.State = in.getAttr("STATE");
		this.PostalCode = in.getAttr("POSTALCODE");
		this.Country = in.getAttr("COUNTRY");
		this.CSPhone = in.getAttr("CSPHONE");
		this.TSPhone = in.getAttr("TSPHONE");
		this.FaxPhone = in.getAttr("FAXPHONE");
		this.URL = in.getAttr("URL");
		if(this.URL == null) this.URL = in.getAttr("URL2");
		this.Email = in.getAttr("EMAIL");
	}
}
