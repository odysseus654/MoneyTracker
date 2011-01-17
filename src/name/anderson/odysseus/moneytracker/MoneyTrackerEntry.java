package name.anderson.odysseus.moneytracker;

import java.io.*;
import java.util.List;
import org.apache.http.*;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.os.Bundle;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;
import name.anderson.odysseus.moneytracker.prof.OfxFiDefinition;

public class MoneyTrackerEntry extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        try
        {
    		OfxProfile pro = new OfxProfile();
    		OfxFiDefinition def = new OfxFiDefinition();
    		pro.fidef = def;
    		def.ofxVer = 1.6f;
//        	def.fiOrg = "Whatcom Educational Credit Union";
//        	def.fiID = "1";
//        	def.fiURL = "https://emax.wecu.com/ofx/ofx.dll";
    		def.fiURL = "https://localhost";
    		def.appId = "QWIN";
    		def.appVer = 1900;
/*        	
        	OfxRequest req = pro.newRequest();
        	ChallengeMsgReq challenge = new ChallengeMsgReq();
        	challenge.userid = "222781";
        	req.addRequest(challenge);
        	req.security = true;
*/
	        OfxRequest req = pro.newRequest();
	        req.addRequest(pro.newProfRequest(false));
/*
	        HttpResponse resp = req.submit();
	        
	        StatusLine status = resp.getStatusLine();
	        int code = status.getStatusCode();
	        String codeMsg = status.getReasonPhrase();
	        InputStream entity = resp.getEntity().getContent();
	        Header[] headers = resp.getAllHeaders();
	        Reader reader = new InputStreamReader(entity, "UTF-8");
	        String stringResponse = convertStreamToString(entity);
*/
        	String str4 = "hello";
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//        setContentView(R.layout.main);
    }
}