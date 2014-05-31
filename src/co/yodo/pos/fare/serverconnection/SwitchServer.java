package co.yodo.pos.fare.serverconnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import co.yodo.pos.fare.R;
import co.yodo.pos.fare.helper.ConnectionDetector;
import co.yodo.pos.fare.helper.YodoBase;
import co.yodo.pos.fare.helper.YodoGlobals;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by luis on 21/07/13.
 */
public class SwitchServer extends AsyncTask<String, Void, ServerResponse>  {
	/*!< DEBUG */
	private final static boolean DEBUG = false;
	
    /*!< Query type */
    private int type = 0;

    /*!< Progress Dialog constants */
    private ProgressDialog progDialog;
    private boolean flagPorgress = false;
    private String message;

    /*!< Activity context */
    private Context _context;

    /*!< Connection Detector */
    private ConnectionDetector cd;
    private boolean internetFlag = true;

    /*!< ID for identify requests */
    public static final String EXCH_MERCH_REQUEST    = "01";	// RT=1, ST=1
    public static final String REG_MERCH_REQUEST     = "02";	// RT=9, ST=1
	public static final String AUTH_HW_MERCH_REQUEST = "03";	// RT=0, ST=4
	public static final String QRY_BAL_TP_REQUEST    = "04";	// RT=4, ST=2
	public static final String QRY_BAL_REQUEST       = "05";	// RT=4, ST=3

    /*!< Switch server ip address */
	//private static final String IP 			 = "http://192.168.1.35";
	//private static final String IP 	         = "http://50.56.180.133"; 
    private static final String IP 			 = "http://198.101.209.120"; 
    private static final String YODO_ADDRESS = "/yodo/yodoswitchrequest/getRequest/";
    //private static final String YODO_ADDRESS = "/yodoLuis/yodoswitchrequest/getRequest/";

    /*!< Response Params */
    HashMap<String, String> responseParams;

    /**
     * Switch Server Constructor
     */
    public SwitchServer(Context _context) {
        this._context = _context;
        cd = new ConnectionDetector(_context);
        message =  _context.getString(R.string.loading);
    }

    @Override
    protected ServerResponse doInBackground(String... requestParams) {
        // Check internet connection
        if(!cd.isConnectingToInternet() || !cd.isOnline(IP)) {
        	internetFlag = false;
        }

        // Connecting to the server
        if(requestParams[0].equals(EXCH_MERCH_REQUEST)) {
			this.connect(ServerRequest.createExchangeRequest(requestParams[1], Integer.parseInt(ServerRequest.EXCH_MERCH_SUBREQ)));
		}
        else if(requestParams[0].equals(REG_MERCH_REQUEST)) {
			this.connect(ServerRequest.createRegistrationRequest(requestParams[1], Integer.parseInt(ServerRequest.REG_MERCH_SUBREQ)));
		}
        else if(requestParams[0].equals(AUTH_HW_MERCH_REQUEST)) {
            this.connect(ServerRequest.createAuthenticationRequest(requestParams[1], Integer.parseInt(ServerRequest.AUTH_HW_MERCH_SUBREQ)));
        }
        else if(requestParams[0].equals(QRY_BAL_REQUEST)) {
			this.connect(ServerRequest.createBalanceThirdPartyRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_BAL_SUBREQ)));
		}
        else if(requestParams[0].equals(QRY_BAL_TP_REQUEST)) {
			this.connect(ServerRequest.createBalanceThirdPartyRequest(requestParams[1], Integer.parseInt(ServerRequest.QUERY_BAL_TP_SUBREQ)));
		}

        // Getting response
        return getServerResponse();
    }

    public void connect(String pRequest){
        try{
            // Handling XML
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            // Send URL to parse XML Tags
            URL sourceUrl = new URL(IP + YODO_ADDRESS + pRequest);

            // Create handler to handle XML Tags ( extends DefaultHandler )
            XMLHandler myXMLHandler = new XMLHandler();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(sourceUrl.openStream()));
        } catch (Exception e) {
            System.out.println("XML Pasing Exception = " + e);
        }

        // Get result from MyXMLHandler SitlesList Object
        responseParams = XMLHandler.responseValues;
    }

    public ServerResponse getServerResponse(){
        ServerResponse oSrvRes = null;

        if((responseParams != null) && (!responseParams.isEmpty()) ){
            oSrvRes = new ServerResponse();

            Iterator<Map.Entry<String, String>> it = responseParams.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, String> pairs = it.next();

                // Getting the response code
                if(pairs.getKey().equals(ServerResponse.CODE_ELEM))
                    oSrvRes.setCode(pairs.getValue());

                // Getting the authorization number
                if(pairs.getKey().equals(ServerResponse.AUTH_NUM_ELEM))
                    oSrvRes.setAuthNumber(pairs.getValue());

                // Getting the response message
                if(pairs.getKey().equals(ServerResponse.MESSAGE_ELEM))
                    oSrvRes.setMessage(pairs.getValue());

                // Getting the response parameters
                if(pairs.getKey().equals(ServerResponse.PARAMS_ELEM))
                    oSrvRes.setParams(pairs.getValue());

                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        return oSrvRes;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(flagPorgress)
            showProgressDialog(message);
    }

    @Override
    protected void onPostExecute(ServerResponse response) {
        super.onPostExecute(response);

        if(progDialog != null)
            progDialog.dismiss();
        
        if(response == null && !internetFlag) {
        	response = new ServerResponse();
        	response.setCode(YodoGlobals.ERROR_INTERNET);
        }

        if(DEBUG) {
        	if(response == null)
        		Log.e("Response", _context.getString(R.string.null_response));
        	else
        		Log.e("Response", response.toString());
        }
        
        ((YodoBase) _context).setData(response, type);
    }
    
    @Override
    protected void onCancelled(ServerResponse response) {
        super.onCancelled(response);

        if(progDialog != null)
            progDialog.dismiss();
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDialog(boolean state, String message) {
        this.flagPorgress = state;

        if(message != null)
            this.message = message;
    }

    /**
     * ProgressDialog progressDialog; I have declared earlier.
     */
    private void showProgressDialog(String message) {
        progDialog = new ProgressDialog(_context);

        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage(message);
        progDialog.setCancelable(false);
        progDialog.show();
    }
}
