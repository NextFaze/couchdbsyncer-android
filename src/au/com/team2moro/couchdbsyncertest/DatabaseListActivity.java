package au.com.team2moro.couchdbsyncertest;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class DatabaseListActivity extends BaseListActivity {
	public static final String TAG = "DatabaseListActivity";
	
	//ListView lv;
	List<Database> databases;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Database database = databases.get(position);
		Intent intent = new Intent(this, DatabaseActivity.class);
        intent.putExtra("database_name", database.getName());
        startActivity(intent);
	}

	// Called first time user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  MenuInflater inflater = getMenuInflater(); 
	  inflater.inflate(R.menu.database_list_menu, menu); 
	  return true;
	}
	
	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item1:
			startActivity(new Intent(this, NewDatabaseActivity.class));
			break;
		}

		return true; 
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "created");
        //setContentView(R.layout.database_list_activity);
        //lv = (ListView) findViewById(R.id.list);
        setTitle("Databases");
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		
		updateListView();
	}

	private void updateListView() {

        DatabaseStore dbstore = application.getDatabaseStore();
        databases = dbstore.getDatabases(); 
    	
        if(databases != null && databases.size() > 0) {
        	// create the grid item mapping
           	ArrayAdapter<Database> aa = new ArrayAdapter<Database>(this, android.R.layout.simple_list_item_1, databases);
        	setListAdapter(aa);
        }
    }

}
