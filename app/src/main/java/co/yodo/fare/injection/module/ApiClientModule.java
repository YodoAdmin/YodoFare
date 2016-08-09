package co.yodo.fare.injection.module;

import android.content.Context;

import co.yodo.fare.injection.scope.ApplicationScope;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.restapi.network.YodoRequest;
import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {
    @Provides
    @ApplicationScope
    public ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }

    @Provides
    @ApplicationScope
    public YodoRequest providesApiClient( Context context ) {
        return YodoRequest.getInstance( context );
    }
}
