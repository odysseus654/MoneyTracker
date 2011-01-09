package name.anderson.odysseus.moneytracker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class Utilities
{
	public static AlertDialog buildAlert(Context ctx, Exception e, String msg, String title, DialogInterface.OnClickListener listen)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle(title);
		String dispMsg = msg;
		if(e != null) msg = msg + "\n\n" + e.getMessage();
		dialog.setMessage(dispMsg);
		dialog.setNeutralButton("Ok", listen);
		return dialog.create();
	}
	
    public static String convertStreamToString(Reader reader) throws IOException
    {
	    /*
	     * To convert the InputStream to String we use the
	     * Reader.read(char[] buffer) method. We iterate until the
	     * Reader return -1 which means there's no more data to
	     * read. We use the StringWriter class to produce the string.
	     */
    	if (reader == null) return null;
		Writer writer = new StringWriter();

		char[] buffer = new char[1024];
		int n;
		while ((n = reader.read(buffer)) != -1)
		{
			writer.write(buffer, 0, n);
		}
		return writer.toString();
	}
}
