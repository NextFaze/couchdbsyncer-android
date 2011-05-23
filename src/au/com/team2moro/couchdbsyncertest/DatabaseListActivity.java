package au.com.team2moro.couchdbsyncertest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
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
import au.com.team2moro.couchdbsyncer.Syncer;

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
        
        // sync database
        // adds database record if it doesn't exist
        try {
        	URL url = new URL("http://192.168.1.134:5984/syncertest");
        	String dbname = "syncertest";
        	DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
        	Database database = dbstore.getDatabase(dbname);
        	if(database == null) {
        		database = dbstore.addDatabase(dbname, url);    		
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

        DatabaseStore dbstore = application.getDatabaseStore();
        databases = dbstore.getDatabases(); 
    	
        if(databases != null && databases.size() > 0) {
        	// create the grid item mapping
           	ArrayAdapter<Database> aa = new ArrayAdapter<Database>(this, android.R.layout.simple_list_item_1, databases);
        	setListAdapter(aa);
        }
    }

	private String getStackTraceAsString(Exception exception) 
	{ 
		StringWriter sw = new StringWriter(); 
		PrintWriter pw = new PrintWriter(sw); 
		pw.print(" [ "); 
		pw.print(exception.getClass().getName()); 
		pw.print(" ] "); 
		pw.println(exception.getMessage()); 
		exception.printStackTrace(pw); 
		return sw.toString(); 
	}
}
