package co.yodo.fare;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import co.yodo.fare.injection.component.ApplicationComponent;
import co.yodo.fare.injection.component.DaggerApplicationComponent;
import co.yodo.fare.injection.component.DaggerGraphComponent;
import co.yodo.fare.injection.component.GraphComponent;
import co.yodo.fare.injection.module.ApplicationModule;
import co.yodo.restapi.helper.AppConfig;
import co.yodo.restapi.network.ApiClient;
import timber.log.Timber;

@ReportsCrashes(
                formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = HttpSender.Method.POST,
                reportType = HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.text_crash_toast
)
public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static GraphComponent component;

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

        // Init Dagger
        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule( new ApplicationModule( this ) )
                .build();

        component = DaggerGraphComponent.builder()
                .applicationComponent( appComponent )
                .build();

        // Init secure preferences
        Hawk.init(this).build();

        // Init timber
        if (BuildConfig.DEBUG) {
            // Debug
            Timber.plant(new Timber.DebugTree() {
                // Adds the line number
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        } else {
            // Release
            Timber.plant(new CrashReportingTree());
        }
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        /** The max size of a line */
        private static final int MAX_LOG_LENGTH = 4000;
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            if (message.length() < MAX_LOG_LENGTH) {
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, message);
                } else {
                    Log.println(priority, tag, message);
                }
                return;
            }

            for (int i = 0, length = message.length(); i < length; i++) {
                int newLine = message.indexOf('\n', i);
                newLine = newLine != -1 ? newLine : length;
                do {
                    int end = Math.min(newLine, i + MAX_LOG_LENGTH);
                    String part = message.substring(i, end);
                    if (priority == Log.ASSERT) {
                        Log.wtf(tag, part);
                    } else {
                        Log.println(priority, tag, part);
                    }
                    i = end;
                } while (i < newLine);
            }

        }
    }

    public static GraphComponent getComponent() {
        return component;
    }
}
