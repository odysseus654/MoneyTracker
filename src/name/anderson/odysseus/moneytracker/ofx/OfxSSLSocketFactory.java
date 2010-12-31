/**
 * 
 */
package name.anderson.odysseus.moneytracker.ofx;

import java.io.IOException;
import java.net.*;
import java.security.*;

import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * @author Erik
 *
 */
public class OfxSSLSocketFactory extends SSLSocketFactory
{

	public OfxSSLSocketFactory()
		throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException
	{
		super(null);
	}
	
    public Socket createSocket() throws IOException
    {
    	SSLSocket sslSocket = (SSLSocket)super.createSocket();
    	prepSocket(sslSocket);
    	return sslSocket;
	}

	// non-javadoc, see interface LayeredSocketFactory
    public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
    	throws IOException, UnknownHostException
    {
    	SSLSocket sslSocket = (SSLSocket)super.createSocket(socket, host, port, autoClose);
    	prepSocket(sslSocket);
        return sslSocket;
    }

    private void prepSocket(SSLSocket sock)
    {
    	final String[] permittedSuites = {
    		"RC4-SHA", "DES-CBC3-SHA", "EDH-RSA-DES-CBC3-SHA", "EDH-DSS-DES-CBC3-SHA",
    		"DES-CBC-SHA", "EDH-RSA-DES-CBC-SHA", "EDH-DSS-DES-CBC-SHA"	};
    	sock.setEnabledCipherSuites(permittedSuites);
	}
}
