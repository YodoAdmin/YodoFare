package co.yodo.fare.ui.notification;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import co.yodo.fare.R;
import co.yodo.fare.helper.PrefUtils;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The context of the activity
     * @param message The message of the dialog
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, Integer message, View layout,
                                      DialogInterface.OnShowListener okClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac, R.style.AppCompatAlertDialogStyle );

        if( message != null )
            builder.setMessage( message );

        builder.setView( layout );
        builder.setCancelable( false );

        builder.setPositiveButton( R.string.ok, null );

        if( okClick != null )
            builder.setNegativeButton( R.string.cancel, null );

        final AlertDialog oDialog = builder.create();

        if( okClick != null ) {
            oDialog.setOnShowListener( okClick );
            oDialog.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE );
        }

        return oDialog;
    }

    /**
     * Creates an alert dialog with a predefined layout, and a click function
     * @param ac The context of the activity
     * @param layout The layout to be displayed
     * @param okClick The click action
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, View layout, DialogInterface.OnShowListener okClick ) {
        return create( ac, null, layout, okClick );
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param ac The context of the application
     * @param message The message of the dialog
     * @param clickListener Action for the selection
     */
    public static AlertDialog create( Context ac, Integer title, Integer message, View layout,
                                      DialogInterface.OnClickListener clickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac, R.style.AppCompatAlertDialogStyle );

        if( title != null )
            builder.setTitle( title );

        if( message != null )
            builder.setMessage( message );

        if( layout != null )
            builder.setView( layout );

        builder.setCancelable( false );

        builder.setPositiveButton( R.string.ok, clickListener );

        if( clickListener != null )
            builder.setNegativeButton( R.string.cancel, null );

        return builder.create();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param ac The context of the application
     * @param title The title of the dialog
     * @param layout The view of the dialog
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, Integer title, View layout ) {
        return create( ac, title, null, layout, null );
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param positiveClick Action for the positive button
     * @param negativeClick Action for the negative button
     * @return The created dialog
     */
    public static AlertDialog showAlertDialog( final Context c, final int message,
                                               final DialogInterface.OnClickListener positiveClick,
                                               final DialogInterface.OnClickListener negativeClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), positiveClick );
        builder.setNegativeButton( c.getString( R.string.cancel ), negativeClick );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param clickListener Action for the selection
     */
    public static AlertDialog showAlertDialog( final Context c, final int message,
                                       final DialogInterface.OnClickListener clickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString(R.string.ok ), clickListener );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param title The title of the dialog
     * @param message A message to show
     * @param clickListener click for the negative button
     */
    public static AlertDialog showAlertDialog(final Context c, final String title, final String message,
                                       final DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setIcon( R.drawable.icon );
        builder.setTitle( title );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.ok ), clickListener );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        if( PrefUtils.isLiveScan( c ) ) {
            final int TIME_TO_DISMISS = PrefUtils.getDismissTime( c ) * 1000;
            final Handler t = new Handler();
            t.postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            }, TIME_TO_DISMISS );
        }

        return alertDialog;
    }
}
