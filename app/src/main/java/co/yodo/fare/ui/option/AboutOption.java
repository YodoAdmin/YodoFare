package co.yodo.fare.ui.option;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.fare.BuildConfig;
import co.yodo.fare.R;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.ui.notification.AlertDialogHelper;
import co.yodo.fare.ui.option.contract.IOption;
import co.yodo.restapi.network.ApiClient;

/**
 * Created by hei on 22/06/16.
 * Implements the About Option of the MainActivity
 */
public class AboutOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public AboutOption( Activity activity ) {
        super( activity );

        // Gets and sets the dialog layout
        LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this.activity ), false );
        setupLayout( layout );
    }

    /**
     * Prepares a layout for the About dialog
     * @param layout The layout to be prepared
     */
    private void setupLayout( View layout ) {
        // GUI controllers of the dialog
        TextView emailView = (TextView) layout.findViewById( R.id.emailView );
        TextView messageView = (TextView) layout.findViewById( R.id.messageView );

        // Get data
        final String hardwareToken = PrefUtils.getHardwareToken();
        final String message =
                activity.getString( R.string.imei ) + " " + PrefUtils.getHardwareToken() + "\n" +
                activity.getString( R.string.label_currency ) + " " + PrefUtils.getMerchantCurrency( activity ) + "\n" +
                activity.getString( R.string.text_version )  + " " + BuildConfig.VERSION_NAME + "/" +
                ApiClient.getSwitch()  + "\n\n" +
                activity.getString( R.string.text_about_message );
        final String email = activity.getString( R.string.text_about_email );

        // Set text to the controllers
        SpannableString ssEmail = new SpannableString( email );
        ssEmail.setSpan( new UnderlineSpan(), 0, ssEmail.length(), 0 );
        emailView.setText( ssEmail );
        messageView.setText( message  );

        // Create the onClick listener
        emailView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_SEND );
                String[] recipients = { email };
                intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                intent.putExtra( Intent.EXTRA_SUBJECT, hardwareToken );
                intent.setType( "text/html" );
                activity.startActivity( Intent.createChooser( intent, "Send Email" ) );
            }
        });

        // Generate the AlertDialog
        alertDialog = AlertDialogHelper.create(
                activity,
                R.string.action_about,
                layout
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
    }
}
