/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import org.apache.http.client.ClientProtocolException;

/**
 * @author Erik
 *
 */
public class OfxError extends ClientProtocolException
{
	private static final long serialVersionUID = 1019302124815583349L;
	private int code;

	public OfxError(StatusResponse status)
	{
		super(status.msg);
		this.code = status.code;
	}
	
	public int getErrorCode()
	{
		return this.code;
	}
}
