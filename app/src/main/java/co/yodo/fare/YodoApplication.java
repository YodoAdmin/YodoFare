package co.yodo.fare;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.orhanobut.hawk.Hawk;

import co.yodo.fare.injection.component.ApplicationComponent;
import co.yodo.fare.injection.component.DaggerApplicationComponent;
import co.yodo.fare.injection.component.DaggerGraphComponent;
import co.yodo.fare.injection.component.GraphComponent;
import co.yodo.fare.injection.module.ApplicationModule;
import co.yodo.restapi.YodoApi;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class YodoApplication extends Application {
    /** Component that build the dependencies */
    private static GraphComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Dagger
        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        component = DaggerGraphComponent.builder()
                .applicationComponent(appComponent)
                .build();

        Hawk.init(this).build();

        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                // Adds the line number
                @Override
                protected String createStackElementTag(@NonNull StackTraceElement element) {
                    return super.createStackElementTag(element) + ':' + element.getLineNumber();
                }
            });
        } else {
            Timber.plant(new CrashReportingTree());
        }

        YodoApi.init(this)
                .setLog(BuildConfig.DEBUG)
                .server(BuildConfig.URL, BuildConfig.TAG)
                .build();
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        /** The max size of a line */
        private static final int MAX_LOG_LENGTH = 4000;

        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return;
            }

            if (priority == Log.ERROR && t != null) {
                Crashlytics.logException(t);
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
