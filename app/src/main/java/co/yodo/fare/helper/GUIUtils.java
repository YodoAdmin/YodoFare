package co.yodo.fare.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import java.util.Arrays;

import co.yodo.fare.R;

/**
 * Created by hei on 08/08/16.
 * Utils used for the interface
 */
public class GUIUtils {
    /**
     * Get the drawable based on the name
     * @param c The Context of the Android system.
     * @param name The name of the drawable
     * @return The drawable
     */
    public static Drawable getDrawableByName( Context c, String name ) throws Resources.NotFoundException {
        Resources resources = c.getResources();
        final int resourceId = resources.getIdentifier(name, "drawable", c.getPackageName());
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
    public static void setCurrencyIcon( Context c, TextView v, String currency ) {
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
    public static void setTenderCurrencyIcon( Context c, TextView v ) {
        setCurrencyIcon( c, v, PrefUtils.getTenderCurrency( c ) );
    }

    /**
     * Modify the size of the drawable for a TextView
     * @param c The Context of the Android system.
     * @param v The view to modify the drawable
     */
    public static void setMerchantCurrencyIcon( Context c, TextView v ) {
        setCurrencyIcon( c, v, PrefUtils.getMerchantCurrency( c ) );
    }
}
