package au.com.team2moro.couchdbsyncertest;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.Document;

public class DocumentActivity extends Activity {

	TestApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (TestApplication) getApplication();
    	
    	DatabaseStore dbstore = application.getDatabaseStore();
    	Database database = application.getDatabase();
    	String docId = getIntent().getStringExtra("docId");
        Document document = dbstore.getDocument(database, docId);
        
	}
}
