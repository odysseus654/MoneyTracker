/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.util.UUID;

/**
 * @author Erik Anderson
 *
 */
public abstract class OfxMessageReq
{
//	public enum RequestType { Default, Modify, Delete, Cancel, Inquiry };
	public enum MessageSet {
		SIGNON, SIGNUP, BANK, CREDITCARD, LOAN, INVSTMT, INTERXFER, WIREXFER, BILLPAY,
		EMAIL, SECLIST, PRESDIR, PRESDLV, PROF, IMAGE
	};

	public final MessageSet messageSet;
	public final String name;
//	public RequestType type;
	protected UUID trnUid;
	
	public OfxMessageReq(MessageSet ms, String n)
	{
		this.messageSet = ms;
//		this.type = RequestType.Default;
		this.name = n;
	}
/*
	public OfxMessageReq(MessageSet ms, RequestType t, String n)
	{
		this.messageSet = ms;
		this.type = t;
		this.name = n;
	}
*/	
	abstract protected void populateRequest(TransferObject obj, float msgsetVer);
	abstract public OfxMessageResp processResponse(TransferObject tran, TransferObject obj);
	
	public boolean isValidResponse(MessageSet msgsetId, int ver, TransferObject tran, TransferObject obj)
	{	// prob need to add transaction-matching later
		return msgsetId.equals(messageSet) && obj.name.equals(this.name + "RS");
	}

	public TransferObject BuildRequest(float msgsetVer)
	{
		TransferObject tran = BuildTransaction();
		TransferObject obj = new TransferObject(this.name + "RQ");
		if(tran != null) tran.put(obj);
		populateRequest(obj, msgsetVer);
		return tran != null ? tran : obj;
	}
	
	protected TransferObject BuildTransaction()
	{
		TransferObject trn = new TransferObject(this.name + "TRNRQ");
		this.trnUid = UUID.randomUUID();
		trn.put("TRNUID", this.trnUid.toString());
		return trn;
	}
	
/*	
	protected static String versionString(float ver)
	{
		return String.format("%04d", (int)(ver * 100));
	}
*/
}
