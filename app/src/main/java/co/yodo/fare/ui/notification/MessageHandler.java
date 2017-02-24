package co.yodo.fare.ui.notification;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import co.yodo.fare.R;
import co.yodo.restapi.network.model.ServerResponse;

/**
 * Created by luis on 15/12/14.
 * Handler for messages
 */
public class MessageHandler extends Handler {
    /** Id for Messages */
    public static final int INIT_ERROR   = 1;
    private static final int SERVER_ERROR = 2;

    /** Id for the content */
    private static final String CODE    = "code";
    private static final String MESSAGE = "message";

    private final WeakReference<Activity> wMain;

    public MessageHandler( Activity main ) {
        super();
        this.wMain = new WeakReference<>( main );
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage( msg );
        final Activity main = wMain.get();

        // message arrived after activity death
        if( main == null )
            return;

        DialogInterface.OnClickListener clickListener = null;

        if( msg.what == INIT_ERROR ) {
            clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    main.finish();
                }
            };
        }

        String code     = msg.getData().getString( CODE, ServerResponse.ERROR_FAILED );
        String response = msg.getData().getString( MESSAGE );

        switch( code ) {
            case ServerResponse.ERROR_TIMEOUT:
                response = main.getString( R.string.message_error_timeout );
                break;

            case ServerResponse.ERROR_NETWORK:
                response = main.getString( R.string.message_error_network );
                break;

            case ServerResponse.ERROR_SERVER:
                response = main.getString( R.string.message_error_server );
                break;

            case ServerResponse.ERROR_DUP_AUTH:
                response = main.getString( R.string.message_error_transaction );
                break;

            case ServerResponse.ERROR_UNKOWN:
                response = main.getString( R.string.message_error_unknown );
                break;
        }

        AlertDialogHelper.showAlertDialog( main, code, response, clickListener );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( int messageType, MessageHandler handlerMessages, String title, String message ) {
        Message msg = new Message();
        msg.what = messageType;

        Bundle bundle = new Bundle();
        bundle.putString( MessageHandler.CODE, title );
        bundle.putString( MessageHandler.MESSAGE, message );
        msg.setData( bundle );

        handlerMessages.sendMessage( msg );
    }

    /**
     * Sends a message to the handler
     * @param handlerMessages The Handler for the app
     * @param title The title for the alert
     * @param message The message for the alert
     */
    public static void sendMessage( MessageHandler handlerMessages, String title, String message ) {
        sendMessage( SERVER_ERROR, handlerMessages, title, message );
    }
}
