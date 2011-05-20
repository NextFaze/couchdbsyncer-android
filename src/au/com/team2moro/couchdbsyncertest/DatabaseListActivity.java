package au.com.team2moro.couchdbsyncertest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
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
import au.com.team2moro.couchdbsyncer.Syncer;

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

        // sync database
        // adds database record if it doesn't exist
        try {
        	URL url = new URL("http://192.168.1.134:5984/imvs");
        	DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
        	Database database = dbstore.getDatabase("test");
        	if(database == null) {
        		database = dbstore.addDatabase("test", url);    		
        	}
        	database.setUrl(url);
        	Syncer syncer = new Syncer(dbstore, database);
        	syncer.update();
        } catch(Exception e) {
        	Log.d(TAG, "exception: " + getStackTraceAsString(e));
        }
    }
    
    @Override
	protected void onStart() {
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
	
	private String getStackTraceAsString(Exception exception) 
	{ 
	StringWriter sw = new StringWriter(); 
	PrintWriter pw = new PrintWriter(sw); 
	pw.print(" [ "); 
	pw.print(exception.getClass().getName()); 
	pw.print(" ] "); 
	pw.print(exception.getMessage()); 
	exception.printStackTrace(pw); 
	return sw.toString(); 
	}
}
