package name.anderson.odysseus.moneytracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class ExceptionAlert
{
	public static AlertDialog showAlert(Context ctx, Exception e, String msg, String title, DialogInterface.OnClickListener listen)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle(title);
		String dispMsg = msg + "\n\n" + e.getMessage();
		dialog.setMessage(dispMsg);
		dialog.setNeutralButton("Ok", listen);
		return dialog.create();
	}
}
