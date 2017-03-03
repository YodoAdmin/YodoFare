package co.yodo.fare.ui.dialog.contract;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import co.yodo.fare.R;

/**
 * Created by hei on 16/06/16.
 * implements the Dialog abstract class
 */
public abstract class IDialog {
    /** Dialog to be build */
    private final AlertDialog privateDialog;

    /**
     * Constructor that shows the privateDialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    protected IDialog( DialogBuilder builder ) {
        this.privateDialog = builder.dialog;
        this.privateDialog.show();
    }

    /**
     * Show the inner privateDialog
     */
    public void show() {
        this.privateDialog.show();
    }

    /**
     * Abstract class for the Dialog Builders
     */
    protected static abstract class DialogBuilder {
        /** Context object */
        protected final Context context;

        /** Dialog to be build */
        protected final AlertDialog dialog;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         * @param layout The layout for the privateDialog
         */
        protected DialogBuilder( Context context, int layout, int title ) {
            this.context = context;
            AlertDialog.Builder mBuilder = new AlertDialog.Builder( this.context );
            mBuilder.setIcon( R.mipmap.icon );
            mBuilder.setTitle( title );
            mBuilder.setView( layout );
            mBuilder.setCancelable( false );
            mBuilder.setPositiveButton( R.string.text_ok, null );
            this.dialog = mBuilder.show();
        }

        /**
         * Builds the IDialog
         * @return an IDialog
         */
        public abstract IDialog build();
    }

}