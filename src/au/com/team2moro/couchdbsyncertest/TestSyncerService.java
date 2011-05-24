package au.com.team2moro.couchdbsyncertest;

import android.util.Log;
import au.com.team2moro.couchdbsyncer.Credentials;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.SyncerService;

public class TestSyncerService extends SyncerService {
	public static final String TAG = "TestSyncerService";
	
	public TestSyncerService() {
		super();
	}
	
	protected DatabaseStore getDatabaseStore() {
		Log.d(TAG, "application = " + ((TestApplication) getApplication()));
		DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
		return dbstore;
	}
	
	protected Credentials getCredentials() {
		Credentials credentials = ((TestApplication) getApplication()).getDatabaseCredentials(getDatabase());
		return credentials;
	}
}
