package co.yodo.fare.net;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.yodo.fare.R;
import co.yodo.fare.data.ServerResponse;
import co.yodo.fare.helper.PrefUtils;

/**
 * Created by luis on 19/01/16.
 * Handler for the JSON responses
 */
public class JSONHandler {
    /** JSON Tags */
    private static final String YODO_TAG     = "YodoCurrency";
    private static final String CURRENCY_TAG = "currency";
    private static final String RATE_TAG     = "rate";

    /** Currencies */
    private final String FARE_CURRENCY;
    private final String MERCHANT_CURRENCY;

    public JSONHandler( Context ctx ) {
        String[] currencies = ctx.getResources().getStringArray( R.array.currency_array );
        FARE_CURRENCY = currencies[ PrefUtils.getTenderCurrency( ctx ) ];
        MERCHANT_CURRENCY = PrefUtils.getMerchantCurrency( ctx );
    }

    /**
     * Parse the required currencies from the array of
     * currencies and their rates
     * @param array An array of currencies and rates
     * @return The server response with the parsed values
     */
    public ServerResponse parseCurrencies( JSONArray array ) {
        ServerResponse response = new ServerResponse();

        for( int i = 0; i < array.length(); i++ ) {
            try {
                JSONObject temp = array.getJSONObject( i );
                JSONObject c    = temp.getJSONObject( YODO_TAG );
                String currency = c.getString( CURRENCY_TAG );
                String rate     = c.getString( RATE_TAG );

                // Gets the Merchant Currency
                if( currency.equals( MERCHANT_CURRENCY ) )
                    response.addParam( ServerResponse.MERCH_RATE, rate );
                // Gets the fare currency
                if( currency.equals( FARE_CURRENCY ) )
                    response.addParam( ServerResponse.FARE_RATE, rate );
            } catch( JSONException e ) {
                e.printStackTrace();
            }
        }

        return response;
    }
}

