package au.com.team2moro.couchdbsyncertest;

import android.app.Application;
import android.content.SharedPreferences;
import au.com.team2moro.couchdbsyncer.Credentials;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class TestApplication extends Application {

	DatabaseStore dbstore;
	
	@Override
	public void onCreate() {
		super.onCreate();

		dbstore = new DatabaseStore(this);
	}
	
	DatabaseStore getDatabaseStore() {
		return dbstore;
	}

	private String getCredentialsPreferenceKey(Database database) {
		return "database_credentials_" + database.getName();
	}
	
	public Credentials getDatabaseCredentials(Database database) {
		SharedPreferences pref = getSharedPreferences(getCredentialsPreferenceKey(database), MODE_PRIVATE);
		return new Credentials(pref.getString("username", null), pref.getString("password", null));
	}
	
	public void setDatabaseCredentials(Database database, Credentials credentials) {
		SharedPreferences pref = getSharedPreferences(getCredentialsPreferenceKey(database), MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("username", credentials.getUsername());
		editor.putString("password", credentials.getPassword());
		editor.commit();
	}
}
