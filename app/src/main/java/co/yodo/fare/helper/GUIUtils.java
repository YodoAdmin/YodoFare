package co.yodo.fare.helper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

import co.yodo.fare.R;

/**
 * Created by hei on 08/08/16.
 * Utils used for the interface
 */
public class GUIUtils {
    /**
     * Sets the action bar and title to the activity
     * @param act      The activity to be updated
     * @param title    The integer that represents the resource title
     * @return Toolbar The toolbar found for the activity
     */
    public static Toolbar setActionBar(AppCompatActivity act, int title ) {
        // Only used at creation
        Toolbar toolbar = act.findViewById( R.id.actionBar );

        // Setup the toolbar
        act.setTitle( title );
        act.setSupportActionBar( toolbar );
        ActionBar actionBar = act.getSupportActionBar();
        if( actionBar != null )
            actionBar.setDisplayHomeAsUpEnabled( true );

        return toolbar;
    }

    /**
     * Get the drawable based on the name
     * @param c The Context of the Android system.
     * @param name The name of the drawable
     * @return The drawable
     */
    private static Drawable getDrawableByName( Context c, String name ) throws Resources.NotFoundException {
        Resources resources = c.getResources();
        final int resourceId = resources.getIdentifier( name, "mipmap", c.getPackageName() );

        Drawable image = ContextCompat.getDrawable( c, resourceId );
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        return image;
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     * @param currency The currency to tbe set in the view
     */
    private static void setCurrencyIcon( Context c, TextView v, String currency ) {
        // Get currencies and icons
        final String[] icons = c.getResources().getStringArray( R.array.currency_icon_array );
        final String[] currencies = c.getResources().getStringArray( R.array.currency_array );

        // Set the drawable to the TextView
        final int position = Arrays.asList( currencies ).indexOf( currency );
        Drawable icon  = getDrawableByName( c, icons[ position ] );
        icon.setBounds( 3, 0, v.getLineHeight(), (int)( v.getLineHeight() * 0.9 ) );
        v.setCompoundDrawables( icon, null, null, null );
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     */
    public static void setMerchantCurrencyIcon( Context c, TextView v ) {
        setCurrencyIcon( c, v, PrefUtils.getMerchantCurrency() );
    }

    /**
     * Rotates an image by 360 in 1 second
     * @param image The image to rotate
     */
    public static void rotateImage(View image) {
        RotateAnimation rotateAnimation1 = new RotateAnimation( 0, 90,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f );
        rotateAnimation1.setInterpolator( new LinearInterpolator() );
        rotateAnimation1.setDuration( 500 );
        rotateAnimation1.setRepeatCount( 0 );

        image.startAnimation( rotateAnimation1 );
    }

    /**
     * Hides the soft keyboard
     * @param a The activity where the keyboard is open
     */
    public static void hideSoftKeyboard( Activity a ) {
        View v = a.getCurrentFocus();
        if( v != null ) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( v.getWindowToken(), 0 );
        }
    }
}
