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
	public String URL;
	public boolean securePass;
	public SignonRealm realm;
//	public String[] langs;
//	public String[] countries;
	public boolean fullSyncMode;
	public boolean supportsRefresh;
	public boolean supportsRecovery;
	public String SpName;

	public MsgSetInfo()
	{
	}

	public MsgSetInfo(OfxMessageReq.MessageSet msgsetId, int ver, TransferObject in, Map<String, SignonRealm> signonList)
	{
		this.ver = ver;

		TransferObject core = in.getObj("MSGSETCORE");
		if(core != null)
		{
			this.URL = core.getAttr("URL");
			if(this.URL == null) this.URL = core.getAttr("URL2");
			
			String strValue = core.getAttr("OFXSEC");
			this.securePass = (strValue != null) && strValue.equals("Y");
			
			strValue = core.getAttr("SIGNONREALM");
			if(strValue != null) this.realm = signonList.get(strValue);
	/*
			LinkedList<String> languages = new LinkedList<String>();
			LinkedList<String> countries = new LinkedList<String>();
	
			for(TransferObject.ObjValue info : core.members)
			{
				if(info.attrValue != null)
				{
					if(info.name.equals("LANGUAGE"))
					{
						languages.add(info.attrValue);
					}
					else if(info.name.equals("COUNTRY"))
					{
						countries.add(info.attrValue);
					}
				}
			}
			this.langs = languages.toArray(new String[languages.size()]);
			this.countries = countries.toArray(new String[countries.size()]);
	*/
			strValue = core.getAttr("SYNCMODE");
			this.fullSyncMode = strValue != null && strValue.equals("FULL");
	
			strValue = core.getAttr("REFRESHSUPT");
			this.supportsRefresh = (strValue != null) && strValue.equals("Y");
	
			strValue = core.getAttr("RESPFILEER");
			this.supportsRecovery = (strValue != null) && strValue.equals("Y");
	
			this.SpName = core.getAttr("SPNAME");
		}
	}
}
