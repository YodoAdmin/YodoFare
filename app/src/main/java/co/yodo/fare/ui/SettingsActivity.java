package co.yodo.fare.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import co.yodo.fare.R;
import co.yodo.fare.helper.AppConfig;
import co.yodo.fare.helper.PrefUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
        PrefUtils.setLanguage( SettingsActivity.this );
        setContentView( R.layout.activity_settings );

        setupGUI();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch( itemId ) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );

        setSupportActionBar( toolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        getFragmentManager().beginTransaction().replace( R.id.content, new PrefsFragmentInner() ).commit();
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    	private Context c;

        private CheckBoxPreference
            ETP_ADVERTISING;

    	private EditTextPreference
            ETP_SPREF_USERNAME,
            ETP_SPREF_CURRENT_ROUTE,

    		ETP_SPREF_FARE_OLD_ZONE_1,
            ETP_SPREF_FARE_OLD_ZONE_2,
            ETP_SPREF_FARE_OLD_ZONE_3,

            ETP_SPREF_FARE_ADULT_ZONE_1,
            ETP_SPREF_FARE_ADULT_ZONE_2,
            ETP_SPREF_FARE_ADULT_ZONE_3,

            ETP_SPREF_FARE_CHILD_ZONE_1,
            ETP_SPREF_FARE_CHILD_ZONE_2,
            ETP_SPREF_FARE_CHILD_ZONE_3,

            ETP_SPREF_FARE_STUDENT_ZONE_1,
            ETP_SPREF_FARE_STUDENT_ZONE_2,
            ETP_SPREF_FARE_STUDENT_ZONE_3;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

    		PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName( AppConfig.SHARED_PREF_FILE );
            prefMgr.setSharedPreferencesMode( MODE_PRIVATE );

            addPreferencesFromResource( R.xml.fragment_settings);

            setupGUI();
        }

        private void setupGUI() {
            // get the context
            c = getActivity();

            ETP_SPREF_USERNAME = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CURRENT_BEACON );
            ETP_SPREF_CURRENT_ROUTE = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CURRENT_ROUTE );

            ETP_ADVERTISING = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADVERTISING_SERVICE );

            ETP_SPREF_FARE_OLD_ZONE_1 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ELDERLY_ZONE_1 );
            ETP_SPREF_FARE_OLD_ZONE_2 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ELDERLY_ZONE_2 );
            ETP_SPREF_FARE_OLD_ZONE_3 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ELDERLY_ZONE_3 );

            ETP_SPREF_FARE_ADULT_ZONE_1 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADULT_ZONE_1 );
            ETP_SPREF_FARE_ADULT_ZONE_2 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADULT_ZONE_2 );
            ETP_SPREF_FARE_ADULT_ZONE_3 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_ADULT_ZONE_3 );

            ETP_SPREF_FARE_CHILD_ZONE_1 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CHILD_ZONE_1 );
            ETP_SPREF_FARE_CHILD_ZONE_2 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CHILD_ZONE_2 );
            ETP_SPREF_FARE_CHILD_ZONE_3 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_CHILD_ZONE_3 );

            ETP_SPREF_FARE_STUDENT_ZONE_1 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_STUDENT_ZONE_1 );
            ETP_SPREF_FARE_STUDENT_ZONE_2 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_STUDENT_ZONE_2 );
            ETP_SPREF_FARE_STUDENT_ZONE_3 = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_STUDENT_ZONE_3 );

            if( !PrefUtils.hasBluetooth() ) {
                ETP_ADVERTISING.setEnabled( false );
            }
        }

        private void updateStatus( String key ) {
             if( key.equals( AppConfig.SPREF_CURRENT_LANGUAGE ) ) {
                Intent i = new Intent( getActivity(), FareActivity.class );
                i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity( i );
                getActivity().finish();
            }
        }

        @Override
		public void onResume() {
    		super.onResume();
    		// register listener to update when value change
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
            setAllSummaries();
    	}

        @Override
        public void onPause() {
    		super.onPause();
    		// unregister listener
    		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    	}

        private void setAllSummaries() {
            ETP_SPREF_USERNAME.setSummary( PrefUtils.getBeaconName( c ) );
            ETP_SPREF_CURRENT_ROUTE.setSummary( PrefUtils.getBusRoute( c ) );

            ETP_SPREF_FARE_OLD_ZONE_1.setSummary( PrefUtils.getElderlyFare( c, AppConfig.FEE_ZONE_1 ) );
            ETP_SPREF_FARE_OLD_ZONE_2.setSummary( PrefUtils.getElderlyFare( c, AppConfig.FEE_ZONE_2 ) );
            ETP_SPREF_FARE_OLD_ZONE_3.setSummary( PrefUtils.getElderlyFare( c, AppConfig.FEE_ZONE_3 ) );

            ETP_SPREF_FARE_ADULT_ZONE_1.setSummary( PrefUtils.getAdultFare( c, AppConfig.FEE_ZONE_1 ) );
            ETP_SPREF_FARE_ADULT_ZONE_2.setSummary( PrefUtils.getAdultFare( c, AppConfig.FEE_ZONE_2 ) );
            ETP_SPREF_FARE_ADULT_ZONE_3.setSummary( PrefUtils.getAdultFare( c, AppConfig.FEE_ZONE_3 ) );

            ETP_SPREF_FARE_CHILD_ZONE_1.setSummary( PrefUtils.getChildFare( c, AppConfig.FEE_ZONE_1 ) );
            ETP_SPREF_FARE_CHILD_ZONE_2.setSummary( PrefUtils.getChildFare( c, AppConfig.FEE_ZONE_2 ) );
            ETP_SPREF_FARE_CHILD_ZONE_3.setSummary( PrefUtils.getChildFare( c, AppConfig.FEE_ZONE_3 ) );

            ETP_SPREF_FARE_STUDENT_ZONE_1.setSummary( PrefUtils.getStudentFare( c, AppConfig.FEE_ZONE_1 ) );
            ETP_SPREF_FARE_STUDENT_ZONE_2.setSummary( PrefUtils.getStudentFare( c, AppConfig.FEE_ZONE_2 ) );
            ETP_SPREF_FARE_STUDENT_ZONE_3.setSummary( PrefUtils.getStudentFare( c, AppConfig.FEE_ZONE_3 ) );
    	}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setAllSummaries();
            updateStatus( key );
		}
    }
}
