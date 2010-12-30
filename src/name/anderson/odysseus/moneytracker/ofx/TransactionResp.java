package name.anderson.odysseus.moneytracker.ofx;

public class TransactionResp
{
	public String trnUID;
	public StatusResponse status;
	public String cookie;
	
	public TransactionResp(TransferObject in)
	{
		this.trnUID = in.getAttr("TRNUID");
		this.cookie = in.getAttr("CLTCOOKIE");
		
		TransferObject status = in.getObj("STATUS");
		if(status != null) this.status = new StatusResponse(status);
	}
}
