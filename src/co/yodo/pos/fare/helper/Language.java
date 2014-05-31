package co.yodo.pos.fare.helper;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

public class Language {
	 /**
	 * Change the language of the application
	 */
	public static void changeLanguage(Context context) {
		SharedPreferences settings = context.getSharedPreferences(YodoGlobals.PREFERENCES, Context.MODE_PRIVATE);
		int languagePosition = settings.getInt(YodoGlobals.ID_LANGUAGE, YodoGlobals.DEFAULT_LANGUAGE);
		Locale appLoc;
		
		if(YodoGlobals.languages[languagePosition].equals("English")) {
			appLoc = new Locale("en");
	    } 
		else {
	    	appLoc = new Locale("es");
	    }
		
		Resources standardResources = context.getResources();
		Locale.setDefault(appLoc);
		Configuration appConfig = new Configuration(standardResources.getConfiguration());
		appConfig.locale = appLoc;
		standardResources.updateConfiguration(appConfig, standardResources.getDisplayMetrics());
	}
}
