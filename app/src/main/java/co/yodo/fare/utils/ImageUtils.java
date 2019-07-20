package co.yodo.fare.utils;

import android.widget.ImageView;

import co.yodo.fare.R;
import co.yodo.fare.helper.AppConfig;

/**
 * Created by hei on 26/02/17.
 * Handle some images features
 */
public class ImageUtils {
    /**
     * Switch the image of the zone buttons
     * @param current The ImageView to change the state
     * @param selected The state
     */
    public static int handleFeeZone( ImageView current, boolean selected ) {
        switch( current.getId() ) {
            case R.id.image_zone_two:
                if( selected )
                    current.setImageResource( R.drawable.ic_zone_two_selected );
                else
                    current.setImageResource( R.drawable.ic_zone_two );

                return AppConfig.FEE_ZONE_2;

            case R.id.image_zone_three:
                if( selected )
                    current.setImageResource( R.drawable.ic_zone_three_selected );
                else
                    current.setImageResource( R.drawable.ic_zone_three );

                return AppConfig.FEE_ZONE_3;

            default:
                if( selected )
                    current.setImageResource( R.drawable.ic_zone_one_selected );
                else
                    current.setImageResource( R.drawable.ic_zone_one );

                return AppConfig.FEE_ZONE_1;
        }
    }

}
