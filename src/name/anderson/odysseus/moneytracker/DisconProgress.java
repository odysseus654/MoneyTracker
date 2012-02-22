package name.anderson.odysseus.moneytracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class DisconProgress extends DisconDialog
{
	private boolean mIndeterminate;
	private int mProgressVal;
	private int mSecondaryProgressVal;
	private int mMax;
	private Drawable mProgressDrawable;
	private Drawable mIndeterminateDrawable;
	private int mProgressStyle;
	
	public DisconProgress()
	{
		this(null);
	}
	
	public DisconProgress(Context ctx)
	{
		super(ctx);
		mIndeterminate = false;
		mProgressVal = -1;
		mSecondaryProgressVal = -1;
		mMax = -1;
		mProgressStyle = -1;
	}
	
	public static DisconProgress show(CharSequence title, CharSequence message)
	{
		return show(null, title, message, false, false, null);
	}
	
	public static DisconProgress show(Context ctx, CharSequence title, CharSequence message)
	{
		return show(ctx, title, message, false, false, null);
	}

	public static DisconProgress show(CharSequence title, CharSequence message,
			boolean indeterminate)
	{
		return show(null, title, message, indeterminate, false, null);
	}

	public static DisconProgress show(Context ctx, CharSequence title, CharSequence message,
			boolean indeterminate)
	{
		return show(ctx, title, message, indeterminate, false, null);
	}

	public static DisconProgress show(CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable)
	{
		return show(null, title, message, indeterminate, cancelable, null);
	}

	public static DisconProgress show(Context ctx, CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable)
	{
		return show(ctx, title, message, indeterminate, cancelable, null);
	}

	public static DisconProgress show(CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable, DisconDialog.OnCancelListener listener)
	{
		return show(null, title, message, indeterminate, cancelable, listener);
	}

	public static DisconProgress show(Context ctx, CharSequence title, CharSequence message,
			boolean indeterminate, boolean cancelable, DisconDialog.OnCancelListener listener)
	{
		DisconProgress dialog = new DisconProgress(ctx);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(listener);
		dialog.show();
		return dialog;
	}

	public void setProgress(int value)
	{
		mProgressVal = value;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setProgress(value);
		}
	}

	public void setSecondaryProgress(int value)
	{
		mSecondaryProgressVal = value;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setProgress(value);
		}
	}
	
	public int getProgress()
	{
		return mProgressVal;
	}

	public int getSecondaryProgress()
	{
		return mSecondaryProgressVal;
	}

	public int getMax()
	{
		return mMax;
	}

	public void setMax(int max)
	{
		mMax = max;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setMax(max);
		}
	}

	public void incrementProgressBy(int diff)
	{
		if(mDialog != null)
		{
			ProgressDialog prog = (ProgressDialog)mDialog;
			prog.incrementProgressBy(diff);
			mProgressVal = prog.getProgress();
		} else {
			mProgressVal += diff;
		}
	}
	
	public void incrementSecondaryProgressBy(int diff)
	{
		if(mDialog != null)
		{
			ProgressDialog prog = (ProgressDialog)mDialog;
			prog.incrementSecondaryProgressBy(diff);
			mSecondaryProgressVal = prog.getSecondaryProgress();
		} else {
			mSecondaryProgressVal += diff;
		}
	}
	
	public void setProgressDrawable(Drawable d)
	{
		mProgressDrawable = d;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setProgressDrawable(d);
		}
	}

	public void setIndeterminateDrawable(Drawable d)
	{
		mIndeterminateDrawable = d;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setIndeterminateDrawable(d);
		}
	}

	public void setIndeterminate(boolean indeterminate)
	{
		mIndeterminate = indeterminate;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setIndeterminate(indeterminate);
		}
	}
	
	public boolean isIndeterminate()
	{
		return mIndeterminate;
	}
	
	public void setProgressStyle(int style)
	{
		mProgressStyle = style;
		if(mDialog != null)
		{
			((ProgressDialog)mDialog).setProgressStyle(style);
		}
	}
	
	@Override
	protected AlertDialog createDialog()
	{
		ProgressDialog dialog = new ProgressDialog(mContext);
		applyDialog(dialog);
		return dialog;
	}
	
	@Override
	protected void applyDialog(AlertDialog dialog)
	{
		super.applyDialog(dialog);
		ProgressDialog prog = (ProgressDialog)dialog;
		prog.setIndeterminate(mIndeterminate);
		if(mMax >= 0)
		{
			prog.setMax(mMax);
		}
		if(mProgressStyle >= 0)
		{
			prog.setProgressStyle(mProgressStyle);
		}
		if(mProgressVal >= 0)
		{
			prog.setProgress(mProgressVal);
		}
		if(mSecondaryProgressVal >= 0)
		{
			prog.setSecondaryProgress(mSecondaryProgressVal);
		}
		if(mProgressDrawable != null)
		{
			prog.setProgressDrawable(mProgressDrawable);
		}
		if(mIndeterminateDrawable != null)
		{
			prog.setIndeterminateDrawable(mIndeterminateDrawable);
		}
	}
}
