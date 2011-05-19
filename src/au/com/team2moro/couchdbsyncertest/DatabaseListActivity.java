package au.com.team2moro.couchdbsyncertest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class DatabaseListActivity extends Activity {
	public static final String TAG = "DatabaseListActivity";
	
	ListView lv;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "created");
        setContentView(R.layout.database_list_activity);
        
        lv = (ListView) findViewById(R.id.list);
       
        /*
        try {
        	URL url = new URL("http://example.com:5984/dbname");
        	DatabaseStore store = new DatabaseStore(this, "db", url);
        	Syncer syncer = new Syncer(url, "username", "password");
        	syncer.updateStore(store);
        } catch(Exception e) {
        	Log.d(TAG, "exception: " + e);
        }
        */
    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		updateListView();
	}

	private void updateListView() {

        DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
        List<Database> databases = dbstore.getDatabases(); 
        
        if(databases != null && databases.size() > 0) {
        	// create the grid item mapping
        	String[] from = new String[] {"col_1", "col_2", "col_3"};
        	int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3 };

        	// prepare the list of all records
        	List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        	for(Database database : databases) {
        		HashMap<String, String> map = new HashMap<String, String>();
        		map.put("col_1", database.getName());
        		map.put("col_2", database.getSequenceId() + "");
        		map.put("col_3", "");
        		fillMaps.add(map);
        	}

        	// fill in the grid_item layout
        	SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
        	lv.setAdapter(adapter);
        }
    }
}
