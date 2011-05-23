package au.com.team2moro.couchdbsyncertest;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;

public class DatabaseActivity extends Activity implements OnClickListener {
	
	private Database database;
	private TestApplication application;
	private DatabaseStore dbstore;
	private ProgressBar[] progressBars = new ProgressBar[3];
	private TextView[] textView = new TextView[3];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database_activity);
		
    	String db_name = getIntent().getStringExtra("database_name");

		application = (TestApplication)getApplication();
		dbstore = application.getDatabaseStore();
    	database = dbstore.getDatabase(db_name);
        setTitle("Database: " + database.getName());
        
        int[] buttonIds = { R.id.button1, R.id.button2, R.id.button3 };
        int[] progressBarIds = { R.id.progressBar1, R.id.progressBar2, R.id.progressBar3 };
        int[] textViewIds = { R.id.textViewStats1, R.id.textViewStats2, R.id.textViewStats3 };
        
        for(int i = 0; i < 3; i++) {
        	Button button = (Button) findViewById(buttonIds[i]);
        	button.setOnClickListener(this);
        }
        for(int i = 0; i < 3; i++) progressBars[i] = (ProgressBar)findViewById(progressBarIds[i]);
        for(int i = 0; i < 3; i++) textView[i] = (TextView)findViewById(textViewIds[i]);

        updateView();
	}

	private void updateView() {
		textView[0].setText(Long.toString(dbstore.getDocumentCount(database)));
		textView[1].setText(Long.toString(dbstore.getAttachmentCount(database)));
        textView[2].setText(Long.toString(database.getSequenceId()));
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.button1:
			// view docs
			Intent intent = new Intent(this, DocumentTypeListActivity.class);
	        intent.putExtra("database_name", database.getName());
	        startActivity(intent);
	        break;
		case R.id.button2:
			// delete docs
			dbstore.purge(database);
			updateView();
			break;
		case R.id.button3:
			// sync database
			// TODO - start IntentService to perform sync ?
			break;
		}
	}

}
