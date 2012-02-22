package name.anderson.odysseus.moneytracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;

public class DisconDialog
{
	protected int mTitleId;
	protected CharSequence mTitle;
	protected CharSequence mMessage;
	protected int mMessageID;
	protected Drawable mIcon;
	protected int mIconID;
	protected int mPositiveButtonId;
	protected CharSequence mPositiveButtonText;
	protected OnClickListener mPositiveButtonListener;
	protected int mNegativeButtonId;
	protected CharSequence mNegativeButtonText;
	protected OnClickListener mNegativeButtonListener;
	protected int mNeutralButtonId;
	protected CharSequence mNeutralButtonText;
	protected OnClickListener mNeutralButtonListener;
	protected boolean mForceInverseBackground;
	protected boolean mCancelable;
	protected OnCancelListener mOnCancelListener;
	protected OnKeyListener mOnKeyListener;
	protected boolean mShow;
	protected Context mContext;
	protected AlertDialog mDialog;
	
	public interface OnClickListener
	{
		public abstract void onClick(Context ctx, DialogInterface dialog, int which);
	}
	
	public interface OnCancelListener
	{
		public abstract void onCancel(Context ctx, DialogInterface dialog);
	}
	
	public interface OnKeyListener
	{
		public abstract boolean onKey(Context ctx, DialogInterface dialog, int keyCode, KeyEvent event);
	}
	
	public DisconDialog()
	{
		this(null);
	}
	
	public DisconDialog(Context ctx)
	{
		mContext = ctx;
		mTitleId = -1;
		mMessageID = -1;
		mIconID = -1;
		mPositiveButtonId = -1;
		mNegativeButtonId = -1;
		mNeutralButtonId = -1;
		mForceInverseBackground = false;
		mCancelable = false;
		mShow = false;
	}

	public DisconDialog setCancelable(boolean bCancel)
	{
		mCancelable = bCancel;
		if(mDialog != null)
		{
			mDialog.setCancelable(bCancel);
		}
		return this;
	}
	
	public DisconDialog setIcon(int pIconID)
	{
		mIconID = pIconID;
		if(mDialog != null)
		{
			mDialog.setIcon(pIconID);
		}
		return this;
	}

	public DisconDialog setIcon(Drawable pIcon)
	{
		mIcon = pIcon;
		if(mDialog != null)
		{
			mDialog.setIcon(pIcon);
		}
		return this;
	}
	
	public DisconDialog setInverseBackgroundForced(boolean bForced)
	{
		mForceInverseBackground = bForced;
		if(mDialog != null)
		{
			mDialog.setInverseBackgroundForced(bForced);
		}
		return this;
	}
	
	public DisconDialog setMessage(int msg)
	{
		mMessage = null;
		mMessageID = msg;
		if(mDialog != null && mContext != null)
		{
			mDialog.setMessage(mContext.getText(msg));
		}
		return this;
	}

	public DisconDialog setMessage(CharSequence msg)
	{
		mMessage = msg;
		mMessageID = -1;
		if(mDialog != null)
		{
			mDialog.setMessage(msg);
		}
		return this;
	}
	
	public DisconDialog setPositiveButton(int msg, final OnClickListener listener)
	{
		mPositiveButtonId = msg;
		mPositiveButtonText = null;
		mPositiveButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getText(msg),
					curryClickListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setPositiveButton(CharSequence msg, final OnClickListener listener)
	{
		mPositiveButtonId = -1;
		mPositiveButtonText = msg;
		mPositiveButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_POSITIVE, msg,
					curryClickListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setNegativeButton(int msg, final OnClickListener listener)
	{
		mNegativeButtonId = msg;
		mNegativeButtonText = null;
		mNegativeButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getText(msg),
					curryClickListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setNegativeButton(CharSequence msg, final OnClickListener listener)
	{
		mNegativeButtonId = -1;
		mNegativeButtonText = msg;
		mNegativeButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, msg,
					curryClickListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setNeutralButton(int msg, final OnClickListener listener)
	{
		mNeutralButtonId = msg;
		mNeutralButtonText = null;
		mNeutralButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_NEUTRAL, mContext.getText(msg),
					curryClickListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setNeutralButton(CharSequence msg, final OnClickListener listener)
	{
		mNeutralButtonId = -1;
		mNeutralButtonText = msg;
		mNeutralButtonListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setButton(DialogInterface.BUTTON_NEUTRAL, msg,
					curryClickListener(mContext, listener));
		}
		return this;
	}
	
	public DisconDialog setOnCancelListener(OnCancelListener listener)
	{
		mOnCancelListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setOnCancelListener(curryCancelListener(mContext, listener));
		}
		return this;
	}

	public DisconDialog setOnKeyListener(OnKeyListener listener)
	{
		mOnKeyListener = listener;
		if(mDialog != null && mContext != null)
		{
			mDialog.setOnKeyListener(curryKeyListener(mContext, listener));
		}
		return this;
	}
	
	public DisconDialog setTitle(int title)
	{
		mTitleId = title;
		mTitle = null;
		if(mDialog != null)
		{
			mDialog.setTitle(title);
		}
		return this;
	}

	public DisconDialog setTitle(CharSequence title)
	{
		mTitleId = -1;
		mTitle = title;
		if(mDialog != null)
		{
			mDialog.setTitle(title);
		}
		return this;
	}
	
	public AlertDialog show()
	{
		if(mShow) return mDialog;
		mShow = true;
		if(mContext == null) return null;
		mDialog = createDialog();
		mDialog.show();
		return mDialog;
	}

	public void hide()
	{
		if(mDialog != null)
		{
			mDialog.hide();
			mDialog = null;
			mShow = false;
		}
	}
	
	public void disconnect()
	{
		if(mDialog != null)
		{
			mDialog.hide();
			mDialog = null;
		}
		mContext = null;
	}

	public void connect(Context ctx)
	{
		mContext = ctx;
		if(mShow)
		{
			mDialog = createDialog();
			mDialog.show();
		}
	}

	protected DialogInterface.OnClickListener curryClickListener(final Context ctx, final OnClickListener handler)
	{
		return new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				handler.onClick(ctx, dialog, which);
			}
		};
	}

	protected DialogInterface.OnCancelListener curryCancelListener(final Context ctx, final OnCancelListener handler)
	{
		return new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				handler.onCancel(ctx, dialog);
			}
		};
	}

	protected DialogInterface.OnKeyListener curryKeyListener(final Context ctx, final OnKeyListener handler)
	{
		return new DialogInterface.OnKeyListener()
		{
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{
				return handler.onKey(ctx, dialog, keyCode, event);
			}
		};
	}

	protected AlertDialog createDialog()
	{
		AlertDialog dialog = new AlertDialog.Builder(mContext).create();
		applyDialog(dialog);
		return dialog;
	}
	
	protected void applyDialog(AlertDialog dialog)
	{
		if(mTitleId >= 0)
		{
			dialog.setTitle(mContext.getText(mTitleId));
		}
		else if(mTitle != null)
		{
			dialog.setTitle(mTitle);
		}
		if(mIcon != null)
		{
			dialog.setIcon(mIcon);
		}
		if(mIconID >= 0)
		{
			dialog.setIcon(mIconID);
		}
		if(mMessageID >= 0)
		{
			dialog.setMessage(mContext.getText(mMessageID));
		}
		else if(mMessage != null)
		{
			dialog.setMessage(mMessage);
		}
		if(mPositiveButtonId >= 0)
		{
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, mContext.getText(mPositiveButtonId),
					curryClickListener(mContext, mPositiveButtonListener));
		}
		else if(mPositiveButtonText != null)
		{
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText,
					curryClickListener(mContext, mPositiveButtonListener));
		}
		if(mNegativeButtonId >= 0)
		{
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getText(mNegativeButtonId),
					curryClickListener(mContext, mNegativeButtonListener));
		}
		else if(mNegativeButtonText != null)
		{
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText,
					curryClickListener(mContext, mNegativeButtonListener));
		}
		if(mNeutralButtonId >= 0)
		{
			dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mContext.getText(mNeutralButtonId),
					curryClickListener(mContext, mNeutralButtonListener));
		}
		else if(mNeutralButtonText != null)
		{
			dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText,
					curryClickListener(mContext, mNeutralButtonListener));
		}
		if(mForceInverseBackground)
		{
			dialog.setInverseBackgroundForced(true);
		}
		dialog.setCancelable(mCancelable);
		if(mOnCancelListener != null)
		{
			dialog.setOnCancelListener(curryCancelListener(mContext, mOnCancelListener));
		}
		if(mOnKeyListener != null)
		{
			dialog.setOnKeyListener(curryKeyListener(mContext, mOnKeyListener));
		}
	}
}
