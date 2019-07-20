package co.yodo.fare.ui.option.contract;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by hei on 14/06/16.
 * The abstract class used to implement the Command Design Pattern for the
 * different options
 */
public abstract class IOption {
    /** Main options elements */
    protected final Activity activity;
    protected AlertDialog alertDialog;

    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    protected IOption( Activity activity ) {
        this.activity = activity;
    }

    /**
     * Executes an option
     */
    public abstract void execute();
}
