package au.com.team2moro.couchdbsyncertest;

import android.app.Application;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class TestApplication extends Application {

	DatabaseStore dbstore;
	Database database;
	
	@Override
	public void onCreate() {
		super.onCreate();

		dbstore = new DatabaseStore(this);
	}
	
	DatabaseStore getDatabaseStore() {
		return dbstore;
	}
	
	Database getDatabase() {
		return database;
	}
	void setDatabase(Database database) {
		this.database = database;
	}

}
