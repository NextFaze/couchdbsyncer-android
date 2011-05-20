package au.com.team2moro.couchdbsyncertest;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.Document;

public class DocumentListActivity extends ListActivity {

	TestApplication application;
	List<Document> documents;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Document document = documents.get(position);
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.putExtra("docId", document.getDocId());
        startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (TestApplication) getApplication();
    	
    	//String docId = getIntent().getStringExtra("docId");
        //Document document = dbstore.getDocument(docId);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
    	DatabaseStore dbstore = application.getDatabaseStore();
    	Database database = application.getDatabase();
    	documents = dbstore.getDocuments(database);
    	ArrayAdapter<Document> aa = new ArrayAdapter<Document>(this, android.R.layout.simple_list_item_1, documents);
    	setListAdapter(aa);
	}

}
