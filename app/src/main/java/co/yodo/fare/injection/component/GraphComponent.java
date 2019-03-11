package co.yodo.fare.injection.component;

import co.yodo.fare.injection.module.ApiClientModule;
import co.yodo.fare.injection.scope.ApplicationScope;
import co.yodo.fare.ui.FareActivity;
import co.yodo.fare.ui.RegistrationActivity;
import co.yodo.fare.ui.SplashActivity;
import co.yodo.fare.ui.option.contract.IRequestOption;
import dagger.Component;

@ApplicationScope
@Component(
        modules = { ApiClientModule.class},
        dependencies = ApplicationComponent.class
)
public interface GraphComponent {
    // Injects to the Activities
    void inject( SplashActivity activity );
    void inject( RegistrationActivity activity );
    void inject( FareActivity activity );

    // Injects to the Components
    void inject( IRequestOption option );
}
