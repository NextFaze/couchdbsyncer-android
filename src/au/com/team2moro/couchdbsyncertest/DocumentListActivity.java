package au.com.team2moro.couchdbsyncertest;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.Document;

public class DocumentListActivity extends BaseListActivity {

	TestApplication application;
	Database database;
	List<Document> documents;
	String type;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Document document = documents.get(position);
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.putExtra("database_name", database.getName());
        intent.putExtra("docId", document.getDocId());
        startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		    	
    	String db_name = getIntent().getStringExtra("database_name");
    	type = getIntent().getStringExtra("type");
    	database = dbstore.getDatabase(db_name);
        setTitle("Database: " + database.getName() + " type:" + type);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
    	documents = dbstore.getDocuments(database, type);
    	ArrayAdapter<Document> aa = new ArrayAdapter<Document>(this, android.R.layout.simple_list_item_1, documents);
    	setListAdapter(aa);
	}

}
