/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

/**
 * @author Erik
 *
 */
public class StatusResponse
{
//	public enum StatusTypes { Info, Warn, Error };
	public static final int ST_INFO = 1;
	public static final int ST_WARN = 2;
	public static final int ST_ERROR = 3;

	public int code;
	public int sev;
	public String msg;
	// currency
	
	public StatusResponse()
	{
	}
	
	public StatusResponse(TransferObject in)
	{
		this.code = Integer.parseInt(in.getAttr("CODE"));
		
		String statType = in.getAttr("SEVERITY");
		if(statType != null)
		{
			if(statType.equals("INFO"))
			{
				this.sev = ST_INFO; 
			}
			else if(statType.equals("WARN"))
			{
				this.sev = ST_WARN;
			}
			else if(statType.equals("ERROR"))
			{
				this.sev = ST_ERROR;
			}
		}
		
		this.msg = in.getAttr("MESSAGE");
		if(this.msg == null) in.getAttr("MESSAGE2");
	}
}
