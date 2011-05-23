package au.com.team2moro.couchdbsyncertest;

import android.app.ListActivity;
import android.os.Bundle;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class BaseListActivity extends ListActivity {

	TestApplication application;
	DatabaseStore dbstore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (TestApplication) getApplication();
        dbstore = application.getDatabaseStore();
	}

}
