package co.yodo.pos.fare.helper;

import co.yodo.pos.fare.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by luis on 24/07/13.
 */
public class CreateAlertDialog {
    public static void showAlertDialog(final Context context, View layout, EditText input, String title, String message,
                                       DialogInterface.OnClickListener okButtonClickListener,
                                       DialogInterface.OnClickListener cancelButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(layout);

        if(title != null)
            builder.setTitle(title);

        if(message != null)
            builder.setMessage(message);

        builder.setCancelable(true);

        if(okButtonClickListener != null)
            builder.setPositiveButton(context.getString(R.string.ok), okButtonClickListener);

        if(cancelButtonClickListener == null) {
            builder.setNegativeButton(context.getString(R.string.cancel), null);
        } else {
            builder.setNegativeButton(context.getString(R.string.cancel), cancelButtonClickListener);
        }

        final AlertDialog alertDialog = builder.create();
        
        alertDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
		        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
        });
        
        alertDialog.show();
    }

    public static void showAlertDialog(final Context context, String title, String message,
                                       DialogInterface.OnClickListener okButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(title != null)
            builder.setTitle(title);

        if(message != null)
            builder.setMessage(message);

        builder.setCancelable(true);

        if(okButtonClickListener != null)
            builder.setPositiveButton(context.getString(R.string.ok), okButtonClickListener);

        final AlertDialog alertDialog = builder.create();
        
        alertDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
		        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
        });
        
        
        alertDialog.show();
    }
}
