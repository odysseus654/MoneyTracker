/**
 * 
 */
package name.anderson.odysseus.moneytracker.acct;

import java.io.IOException;
import java.util.*;
import name.anderson.odysseus.moneytracker.ofx.*;
import name.anderson.odysseus.moneytracker.ofx.acct.*;
import name.anderson.odysseus.moneytracker.ofx.signon.SignonMsgResp;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;

/**
 * @author Erik
 *
 */
public class AccountWorker
{
	public boolean SyncBalance(Context ctx, LoginSession session, Account acct) throws IOException, XmlPullParserException
	{
    	OfxRequest req = session.newRequest();
    	StmtReq stmtReq = new StmtReq();
    	stmtReq.name = acct.service.name;
    	stmtReq.includeTrans = false;
    	req.addRequest(stmtReq);

    	List<OfxMessageResp> response = req.submit(ctx);
		boolean gotResponse = false;

    	for(OfxMessageResp resp : response)
    	{
    		if(resp instanceof SignonMsgResp)
    		{
    			SignonMsgResp sonResp = (SignonMsgResp)resp;
    			acct.lastUpdate = sonResp.DTServer;
    		}
    		else if(resp instanceof StmtResp)
    		{
    			StmtResp stmtResp = (StmtResp)resp;
    			acct.curBalAmt = stmtResp.ledgerBal;
    			acct.curBalDate = stmtResp.ledgerBalDate;
    			acct.availBalAmt = stmtResp.availBal;
    			acct.availBalDate = stmtResp.availBalDate;
    			gotResponse = true;
//    			public String curDef;
//    			public List<BalanceResponse> balList;
//    			public String marketInfo;
    		}
    	}
 		
    	if(gotResponse)
    	{
    		AcctTables db = new AcctTables(ctx);
    		try
    		{
	    		db.openWritable();
	    		db.pushAccount(acct);
    		}
    		finally
    		{
    			db.close();
    		}
    	}
    	return gotResponse;
	}

	public boolean SyncLedger(Context ctx, LoginSession session, Account acct) throws IOException, XmlPullParserException
	{
    	OfxRequest req = session.newRequest();
    	StmtReq stmtReq = new StmtReq();
    	stmtReq.name = acct.service.name;
    	stmtReq.includeTrans = true;
    	stmtReq.startDt = acct.lastTrans;
    	req.addRequest(stmtReq);

    	List<OfxMessageResp> response = req.submit(ctx);
		boolean gotResponse = false;
		List<StmtResp.Transaction> transList = null;

    	for(OfxMessageResp resp : response)
    	{
    		if(resp instanceof SignonMsgResp)
    		{
    			SignonMsgResp sonResp = (SignonMsgResp)resp;
    			acct.lastUpdate = sonResp.DTServer;
    		}
    		else if(resp instanceof StmtResp)
    		{
    			StmtResp stmtResp = (StmtResp)resp;
    			acct.curBalAmt = stmtResp.ledgerBal;
    			acct.curBalDate = stmtResp.ledgerBalDate;
    			acct.availBalAmt = stmtResp.availBal;
    			acct.availBalDate = stmtResp.availBalDate;
    			acct.lastTrans = stmtResp.endDt;
    			gotResponse = true;
    			transList = stmtResp.transList;
//    			public String curDef;
//    			public List<BalanceResponse> balList;
//    			public String marketInfo;
    		}
    	}
 		
		if(gotResponse)
    	{
    		AcctTables db = new AcctTables(ctx);

    		try
    		{
	    		db.openWritable();

	    		if(transList != null)
	    		{
					for(StmtResp.Transaction entry : transList)
					{
						if(entry.correctType != 0 && entry.correctsID != null)
						{
							db.deleteTran(acct, entry.correctsID);
							if(entry.correctType == StmtResp.Transaction.CT_DELETE)
							{
								continue;
							}
						}
						
						Transaction trans = new Transaction(acct);
						trans.type = entry.type.toString();
						trans.postDate = entry.postDate;
						trans.initDate = entry.initDate;
						trans.availDate = entry.availDate;
						trans.amt = entry.amt;
						trans.transID = entry.transID;
						trans.servTransID = entry.servTransID;
						
						if(entry.checkNum != null && entry.checkNum != "")
						{
							trans.attrs.put("checkNum", entry.checkNum);
						}
						if(entry.refNum != null && entry.refNum != "")
						{
							trans.attrs.put("refNum", entry.refNum);
						}
						if(entry.sic != null && entry.sic != "")
						{
							trans.attrs.put("sic", entry.sic);
						}
						if(entry.payeeID != null && entry.payeeID != "")
						{
							trans.attrs.put("payeeId", entry.payeeID);
						}
						if(entry.extendedName != null && entry.extendedName != "")
						{
							trans.attrs.put("extended", entry.extendedName);
						}
						if(entry.memo != null && entry.memo != "")
						{
							trans.attrs.put("memo", entry.memo);
						}
		/*				public String name;
						public Payee payee;					// name OR payee
						public ServiceAcctName destName;	// ccName OR bankName
						//public something imageData;
						public CurrencyBlock currency;		// origCurrency OR currency
						//public something inv401Ksource;*/
						
						db.pushTran(trans);
					}
				}

	    		db.pushAccount(acct);
    		}
    		finally
    		{
    			db.close();
    		}
    	}
    	return gotResponse;
	}
}
