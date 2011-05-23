package au.com.team2moro.couchdbsyncertest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.Document;

public class DocumentActivity extends Activity {

	TestApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (TestApplication) getApplication();
    	
		String db_name = getIntent().getStringExtra("database_name");
		DatabaseStore dbstore = application.getDatabaseStore();
    	Database database = dbstore.getDatabase(db_name);
    	String docId = getIntent().getStringExtra("docId");
        Document document = dbstore.getDocument(database, docId);
        setTitle("Document " + document.getDocId());

        TextView tv = new TextView(this);
        tv.setText(document.getContent().toString());
        setContentView(tv);
	}
}
