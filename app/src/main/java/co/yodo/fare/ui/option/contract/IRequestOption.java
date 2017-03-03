package co.yodo.fare.ui.option.contract;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import javax.inject.Inject;

import co.yodo.fare.R;
import co.yodo.fare.YodoApplication;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.restapi.network.ApiClient;

/**
 * Created by hei on 04/08/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options including requests
 */
public abstract class IRequestOption extends IOption {
    /** Manager for the server requests */
    @Inject
    protected ApiClient requestManager;

    /** Progress dialog for the requests */
    @Inject
    protected ProgressDialogHelper progressManager;

    /** User identifier */
    protected final String hardwareToken;

    /** GUI elements */
    protected EditText etInput;
    protected TextInputLayout tilPip;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    protected IRequestOption( Activity activity ) {
        super( activity );

        // Injection
        YodoApplication.getComponent().inject( this );

        // Gets request's data
        this.hardwareToken = PrefUtils.getHardwareToken();
    }

    /**
     * Creates the layout for the input pip
     * @return View The layout
     */
    protected View buildLayout() {
        // Dialog
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_with_pip, new LinearLayout( activity ), false );

        // GUI setup
        etInput = (EditText) layout.findViewById( R.id.text_pip );
        tilPip = (TextInputLayout) etInput.getParent().getParent();

        return layout;
    }

    /**
     * Builds a listener for the positive button
     * @param onPositive The new procedure for the positive button
     * @return The listener
     */
    protected DialogInterface.OnShowListener buildOnClick( final View.OnClickListener onPositive ) {
        return new DialogInterface.OnShowListener() {
            @Override
            public void onShow( DialogInterface dialog ) {
                // Get the AlertDialog and the positive Button
                alertDialog = AlertDialog.class.cast( dialog );
                final Button button = alertDialog.getButton( AlertDialog.BUTTON_POSITIVE );

                // Sets the action for the positive Button
                button.setOnClickListener( onPositive );
            }
        };
    }

    /**
     * Clears the layout
     */
    protected void clearGUI() {
        etInput.setText( "" );
        tilPip.setError( null );
        etInput.requestFocus();
    }
}