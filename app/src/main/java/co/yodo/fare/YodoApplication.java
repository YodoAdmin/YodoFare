package co.yodo.fare;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import co.yodo.fare.injection.component.ApplicationComponent;
import co.yodo.fare.injection.component.DaggerApplicationComponent;
import co.yodo.fare.injection.component.DaggerGraphComponent;
import co.yodo.fare.injection.component.GraphComponent;
import co.yodo.fare.injection.module.ApplicationModule;
import co.yodo.restapi.helper.AppConfig;
import co.yodo.restapi.network.ApiClient;

@ReportsCrashes(
                formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text
)
public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static GraphComponent mComponent;

    @Override
    protected void attachBaseContext( Context base ) {
        super.attachBaseContext( base );
        ACRA.init( this );

        // Sets the log flag and IP for the restapi
        ApiClient.IP = ApiClient.DEMO_IP;
        AppConfig.DEBUG = co.yodo.fare.helper.AppConfig.DEBUG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        mComponent = DaggerGraphComponent.builder()
                .applicationComponent( appComponent )
                .build();
    }

    public static GraphComponent getComponent() {
        return mComponent;
    }
}
