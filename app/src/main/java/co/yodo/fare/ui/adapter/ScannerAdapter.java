package co.yodo.fare.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import co.yodo.fare.ui.scanner.QRScannerFactory;

/**
 * Created by hei on 26/06/16.
 * handles the items for the scanner
 */
public class ScannerAdapter extends ArrayAdapter<QRScannerFactory.SupportedScanner> {
    /** Context object */
    private Context ac;

    public ScannerAdapter( Context context, int resource, QRScannerFactory.SupportedScanner[] values ) {
        super( context, resource, values );
        this.ac = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent ) {
        QRScannerFactory.SupportedScanner scanner = getItem( position );
        TextView textView = (TextView) super.getView( position, convertView, parent );
        textView.setText( ac.getString( scanner.getValue() ) );

        return textView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent ) {
        QRScannerFactory.SupportedScanner scanner = getItem( position );
        TextView textView = (TextView) super.getView( position, convertView, parent );
        textView.setText( ac.getString( scanner.getValue() ) );
        textView.setTextColor( Color.WHITE );

        return textView;
    }
}
