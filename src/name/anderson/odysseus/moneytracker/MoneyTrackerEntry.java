package name.anderson.odysseus.moneytracker;

import java.io.*;
import java.util.List;
import org.apache.http.*;
import org.xmlpull.v1.XmlPullParserException;
import android.app.Activity;
import android.os.Bundle;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.signon.*;

public class MoneyTrackerEntry extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        try
        {
    		OfxProfile pro = new OfxProfile();
        	pro.ofxVer = 1.6f;
//        	pro.fiOrg = "Whatcom Educational Credit Union";
//        	pro.fiID = "1";
//        	pro.fiURL = "https://emax.wecu.com/ofx/ofx.dll";
        	pro.fiURL = "https://localhost";
        	pro.appId = "QWIN";
        	pro.appVer = 1900;
/*        	
        	OfxRequest req = pro.newRequest();
        	ChallengeMsgReq challenge = new ChallengeMsgReq();
        	challenge.userid = "222781";
        	req.addRequest(challenge);
        	req.security = true;
*/
	        OfxRequest req = pro.newRequest(true);
	        req.addRequest(pro.newProfRequest());
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
        	String str2 = "OFXHEADER:100\nDATA:OFXSGML\nVERSION:160\nSECURITY:NONE\nENCODING:USASCII\nCHARSET:NONE\nCOMPRESSION:NONE\n" +
        		"OLDFILEUID:NONE\nNEWFILEUID:f80ef2c3-b354-4a3f-9179-3a4d378a2d90\n\n<OFX>\n<SIGNONMSGSRSV1>\n<SONRS>\n<STATUS>\n" +
        		"<CODE>0\n<SEVERITY>INFO\n</STATUS>\n<DTSERVER>20101213033606\n<LANGUAGE>ENG\n</SONRS>\n</SIGNONMSGSRSV1>\n<PROFMSGSRSV1>\n" +
        		"<PROFTRNRS>\n<TRNUID>f3e16040-140c-4f05-ba50-683ccdcef80d\n<STATUS>\n<CODE>0\n<SEVERITY>INFO\n<MESSAGE>Profile Set\n" +
        		"</STATUS>\n<PROFRS>\n<MSGSETLIST>\n<PROFMSGSET>\n<PROFMSGSETV1>\n<MSGSETCORE>\n<VER>1\n<URL>https://emax.wecu.com/ofx/ofx.dll\n" +
        		"<OFXSEC>NONE\n<TRANSPSEC>Y\n<SIGNONREALM>ULTDUA\n<LANGUAGE>ENG\n<SYNCMODE>LITE\n<RESPFILEER>Y\n<SPNAME>ULTRADATA Corporation\n" +
        		"</MSGSETCORE>\n</PROFMSGSETV1>\n</PROFMSGSET>\n<SIGNONMSGSET>\n<SIGNONMSGSETV1>\n<MSGSETCORE>\n<VER>1\n" +
        		"<URL>https://emax.wecu.com/ofx/ofx.dll\n<OFXSEC>TYPE1\n<TRANSPSEC>Y\n<SIGNONREALM>ULTDUA\n<LANGUAGE>ENG\n<SYNCMODE>LITE\n" +
        		"<RESPFILEER>Y\n<SPNAME>ULTRADATA Corporation\n</MSGSETCORE>\n</SIGNONMSGSETV1>\n</SIGNONMSGSET>\n<SIGNUPMSGSET>\n" +
        		"<SIGNUPMSGSETV1>\n<MSGSETCORE>\n<VER>1\n<URL>https://emax.wecu.com/ofx/ofx.dll\n<OFXSEC>TYPE1\n<TRANSPSEC>Y\n" +
        		"<SIGNONREALM>ULTDUA\n<LANGUAGE>ENG\n<SYNCMODE>LITE\n<RESPFILEER>Y\n<SPNAME>ULTRADATA Corporation\n</MSGSETCORE>\n" +
        		"<CLIENTENROLL>\n<ACCTREQUIRED>N\n</CLIENTENROLL>\n<CHGUSERINFO>N\n<AVAILACCTS>Y\n<CLIENTACTREQ>N\n</SIGNUPMSGSETV1>\n" +
        		"</SIGNUPMSGSET>\n<BANKMSGSET>\n<BANKMSGSETV1>\n<MSGSETCORE>\n<VER>1\n<URL>https://emax.wecu.com/ofx/ofx.dll\n" +
        		"<OFXSEC>TYPE1\n<TRANSPSEC>Y\n<SIGNONREALM>ULTDUA\n<LANGUAGE>ENG\n<SYNCMODE>LITE\n<RESPFILEER>Y\n<SPNAME>ULTRADATA Corporation\n" +
        		"</MSGSETCORE>\n<CLOSINGAVAIL>N\n<XFERPROF>\n<PROCENDTM>235959\n<CANSCHED>N\n<CANRECUR>N\n<CANMODXFERS>N\n<CANMODMDLS>N\n" +
        		"<MODELWND>0\n<DAYSWITH>0\n<DFLTDAYSTOPAY>0\n</XFERPROF>\n<EMAILPROF>\n<CANEMAIL>N\n<CANNOTIFY>N\n</EMAILPROF>\n" +
        		"</BANKMSGSETV1>\n</BANKMSGSET>\n</MSGSETLIST>\n<SIGNONINFOLIST>\n<SIGNONINFO>\n<SIGNONREALM>ULTDUA\n<MIN>6\n" +
        		"<MAX>10\n<CHARTYPE>ALPHAORNUMERIC\n<CASESEN>Y\n<SPECIAL>Y\n<SPACES>Y\n<PINCH>Y\n<CHGPINFIRST>N\n</SIGNONINFO>\n" +
        		"</SIGNONINFOLIST>\n<DTPROFUP>20080611141713\n<FINAME>Whatcom Educational Credit Union\n<ADDR1>600 E Holly St\n" +
        		"<CITY>Bellingham\n<STATE>WA\n<POSTALCODE>98225\n<COUNTRY>USA\n<CSPHONE>360 676 1168\n<FAXPHONE>360 733 5443\n" +
        		"<URL>https://www.wecu.com\n<EMAIL>memberservice@wecu.com\n</PROFRS>\n</PROFTRNRS>\n</PROFMSGSRSV1>\n</OFX>";
        	Reader reader2 = new StringReader(str2);
        	List<OfxMessageResp> response = req.parseResponse(reader2);

        	String str4 = "hello";
			
		} catch (Exception e) {
			e.printStackTrace();
		}
//        setContentView(R.layout.main);
    }

    private static String convertStreamToString(InputStream is) throws IOException
    {
	    /*
	     * To convert the InputStream to String we use the
	     * Reader.read(char[] buffer) method. We iterate until the
	     * Reader return -1 which means there's no more data to
	     * read. We use the StringWriter class to produce the string.
	     */
    	if (is != null)
    	{
    		Writer writer = new StringWriter();

    		char[] buffer = new char[1024];
    		try {
    			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    			int n;
    			while ((n = reader.read(buffer)) != -1)
    			{
    				writer.write(buffer, 0, n);
    			}
    		} finally {
    			is.close();
    		}
    		return writer.toString();
    	} else {       
    		return "";
    	}
	}
}