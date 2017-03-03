package co.yodo.fare.ui.notification;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;

import co.yodo.fare.R;
import co.yodo.fare.helper.PrefUtils;

/**
 * Created by luis on 16/12/14.
 * Helper to create alert dialogs
 */
public class AlertDialogHelper {
    /**
     * Shows a dialog for alert messages
     * @param c The context of the application
     * @param message A message to show
     * @param onClick click for the positive button
     */
    public static void create( Context c, String message, DialogInterface.OnClickListener onClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.text_ok ), onClick );

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText with two buttons (permission)
     * @param ac The context of the application
     * @param message The message of the dialog
     * @param onClick Action for the selection
     */
    public static AlertDialog create( Context ac, Integer title, Integer message, View layout,
                                      DialogInterface.OnClickListener onClick, boolean cancel ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ac );

        if( title != null )
            builder.setTitle( title );

        if( message != null )
            builder.setMessage( message );

        if( layout != null )
            builder.setView( layout );

        builder.setCancelable( false );

        builder.setPositiveButton( R.string.text_ok, onClick );

        if( cancel )
            builder.setNegativeButton( R.string.text_cancel, null );

        return builder.create();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param ac The context of the application
     * @param title The title of the dialog
     * @param layout The view of the dialog
     * @return The AlertDialog object
     */
    public static AlertDialog create( Context ac, int title, View layout ) {
        // Creates a dialog only with ok
        return create( ac, title, null, layout, null, false );
    }


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
        // Creates a dialog with ok and cancel
        final AlertDialog dialog = create( ac, null, message, layout, null, true );

        if( okClick != null ) {
            dialog.setOnShowListener( okClick );
        }

        return dialog;
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
     * Shows an alert dialog for the response from the server
     * @param c The context of the application
     * @param message A message to show
     */
    public static AlertDialog create( Context c, String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.text_ok ), null );

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        if( PrefUtils.isLiveScan( c ) ) {
            final int TIME_TO_DISMISS = PrefUtils.getDismissTime( c ) * 1000;
            final Handler t = new Handler();
            t.postDelayed( new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            }, TIME_TO_DISMISS );
        }

        return alertDialog;
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     * @param okClick Action for the positive button
     * @param cancelClick Action for the negative button
     */
    public static void create( Context c, int message, DialogInterface.OnClickListener okClick,
                               DialogInterface.OnClickListener cancelClick ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( c );
        builder.setMessage( message );
        builder.setCancelable( false );

        builder.setPositiveButton( c.getString( R.string.text_ok ), okClick );

        if(cancelClick != null) {
            builder.setNegativeButton( c.getString( R.string.text_cancel ), cancelClick );
        }

        builder.show();
    }

    /**
     * Shows an alert dialog with an EditText
     * @param c The context of the application
     * @param message The message of the dialog
     */
    public static void create( Context c, int message, DialogInterface.OnClickListener okClick ) {
        create( c, message, okClick, null );
    }
}
