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
	public enum StatusTypes { Info, Warn, Error };

	public int code;
	public StatusTypes sev;
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
				this.sev = StatusTypes.Info; 
			}
			else if(statType.equals("WARN"))
			{
				this.sev = StatusTypes.Warn;
			}
			else if(statType.equals("ERROR"))
			{
				this.sev = StatusTypes.Error;
			}
		}
		
		this.msg = in.getAttr("MESSAGE");
		if(this.msg == null) in.getAttr("MESSAGE2");
	}
}
