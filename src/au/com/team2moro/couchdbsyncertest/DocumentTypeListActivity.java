package au.com.team2moro.couchdbsyncertest;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.com.team2moro.couchdbsyncer.Database;

public class DocumentTypeListActivity extends BaseListActivity {
	public static final String TAG = "DocumentTypeListActivity";
	Database database;
	List<String> types;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String type = types.get(position);
		Intent intent = new Intent(this, DocumentListActivity.class);
        intent.putExtra("database_name", database.getName());
        intent.putExtra("type", type);
        startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String dbname = getIntent().getStringExtra("database_name");
		Log.d(TAG, "database name: " + dbname);
		database = dbstore.getDatabase(dbname);
        setTitle("Database: " + database.getName());
	}

    @Override
	protected void onResume() {
		super.onResume();
		
		updateListView();
	}
    
	private void updateListView() {
		types = dbstore.getDocumentTypes(database);
    	
       	// create the grid item mapping
       	ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types);
       	setListAdapter(aa);
    }
}
