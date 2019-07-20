package co.yodo.fare.injection.module;

import co.yodo.fare.injection.scope.ApplicationScope;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import dagger.Module;
import dagger.Provides;

@Module
public class ApiClientModule {
    @Provides
    @ApplicationScope
    public ProgressDialogHelper providesProgressDialogHelper() {
        return new ProgressDialogHelper();
    }
}
