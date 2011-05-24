package au.com.team2moro.couchdbsyncertest;

import au.com.team2moro.couchdbsyncer.Credentials;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.SyncerService;

public class TestSyncerService extends SyncerService {

	TestApplication application;
	
	public TestSyncerService() {
		super();
		this.application = (TestApplication) getApplication();
	}
	
	protected DatabaseStore getDatabaseStore() {
		DatabaseStore dbstore = application.getDatabaseStore();
		return dbstore;
	}
	
	protected Credentials getCredentials() {
		Credentials credentials = application.getDatabaseCredentials(getDatabase());
		return credentials;
	}
}
