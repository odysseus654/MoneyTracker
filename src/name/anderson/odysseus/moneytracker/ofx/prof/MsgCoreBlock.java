/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx.prof;

import java.util.*;

import name.anderson.odysseus.moneytracker.ofx.*;

/**
 * @author Erik Anderson
 *
 */
public class MsgCoreBlock
{
//	public enum SyncMode { Full, Lite };
	public static final int SM_FULL = 1;
	public static final int SM_LITE = 2;
	
	public String URL;
	public boolean securePass;
	public SignonRealm realm;
	public String[] langs;
	public String[] countries;
	public int syncMode;
	public boolean supportsRefresh;
	public boolean supportsRecovery;
	public String SpName;
/*
	public MsgCoreBlock()
	{
	}
*/
	public MsgCoreBlock(TransferObject in, Map<String, SignonRealm> signonList)
	{
		this.URL = in.getAttr("URL");
		if(this.URL == null) this.URL = in.getAttr("URL2");
		
		String strValue = in.getAttr("OFXSEC");
		this.securePass = (strValue != null) && strValue.equals("Y");
		
		strValue = in.getAttr("SIGNONREALM");
		if(strValue != null) this.realm = signonList.get(strValue);

		LinkedList<String> languages = new LinkedList<String>();
		LinkedList<String> countries = new LinkedList<String>();

		Iterator<TransferObject.ObjValue> iter = in.members.iterator();
		while(iter.hasNext())
		{
			TransferObject.ObjValue info = iter.next();
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

		strValue = in.getAttr("SYNCMODE");
		if(strValue.equals("FULL"))
		{
			this.syncMode = SM_FULL;
		}
		else if(strValue.equals("LITE"))
		{
			this.syncMode = SM_LITE;
		}

		strValue = in.getAttr("REFRESHSUPT");
		this.supportsRefresh = (strValue != null) && strValue.equals("Y");

		strValue = in.getAttr("RESPFILEER");
		this.supportsRecovery = (strValue != null) && strValue.equals("Y");

		this.SpName = in.getAttr("SPNAME");
	}
}
