package name.anderson.odysseus.moneytracker.prof;

import android.os.Bundle;

public class OfxFiDefinition
{
	public String name;
	public String fiURL;
	public String fiOrg;
	public String fiID;
	public String appId;
	public int appVer;
	public float ofxVer;
	public boolean simpleProf;
	
	public OfxFiDefinition()
	{
	}

	public OfxFiDefinition(Bundle source)
	{
		this.name = source.getString("fi_name");
		this.fiURL = source.getString("fi_url");
		this.fiOrg = source.getString("fi_fiorg");
		this.fiID = source.getString("fi_fiid");
		this.appId = source.getString("fi_appid");
		this.appVer = source.getInt("fi_appver", 0);
		this.ofxVer = source.getFloat("fi_ofxver", 0.0f);
		this.simpleProf = source.getBoolean("fi_simpleProf", false);
	}
	
	public void push(Bundle dest)
	{
		dest.putString("fi_name", this.name);
		dest.putString("fi_url", this.fiURL);
		dest.putString("fi_fiorg", this.fiOrg);
		dest.putString("fi_fiid", this.fiID);
		dest.putString("fi_appid", this.appId);
		dest.putInt("fi_appver", this.appVer);
		dest.putFloat("fi_ofxver", this.ofxVer);
		dest.putBoolean("fi_simpleProf", this.simpleProf);
	}
}
