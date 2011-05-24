package au.com.team2moro.couchdbsyncertest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.SyncerService;

public class DatabaseActivity extends Activity implements OnClickListener {
	public static final String TAG = "DatabaseActivity";
	private Database database;
	private TestApplication application;
	private DatabaseStore dbstore;
	private ProgressBar[] progressBars = new ProgressBar[3];
	private TextView[] textView = new TextView[4];
	private IntentFilter filter;
	private SyncerReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database_activity);
		
    	String db_name = getIntent().getStringExtra("database_name");

		application = (TestApplication)getApplication();
		dbstore = application.getDatabaseStore();
    	database = dbstore.getDatabase(db_name);
        setTitle("Database: " + database.getName());
         
        int[] buttonIds = { R.id.button1, R.id.button2, R.id.button3, R.id.button4 };
        int[] progressBarIds = { R.id.progressBar1, R.id.progressBar2, R.id.progressBar3 };
        int[] textViewIds = { R.id.textViewURL, R.id.textViewStats1, R.id.textViewStats2, R.id.textViewStats3 };
        
        for(int i = 0; i < 4; i++) {
        	Button button = (Button) findViewById(buttonIds[i]);
        	button.setOnClickListener(this);
        }
        for(int i = 0; i < 3; i++) progressBars[i] = (ProgressBar)findViewById(progressBarIds[i]);
        for(int i = 0; i < 4; i++) textView[i] = (TextView)findViewById(textViewIds[i]);

        textView[0].setText(database.getUrl().toString());
        
        filter = new IntentFilter(SyncerService.SYNCER_PROGRESS_INTENT);
        receiver = new SyncerReceiver();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        updateView();
        registerReceiver(receiver, filter);  // register for syncer progress broadcasts
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(receiver);  // unregister for syncer progress broadcasts
	}
	
	private void updateView() {
		textView[1].setText(Long.toString(dbstore.getDocumentCount(database)));
		textView[2].setText(Long.toString(dbstore.getAttachmentCount(database)));
        textView[3].setText(Long.toString(database.getSequenceId()));
	}
	
	public void onClick(View v) {
		Intent intent;

		switch(v.getId()) {
		case R.id.button1:
			// view docs
			intent = new Intent(this, DocumentTypeListActivity.class);
	        intent.putExtra("database_name", database.getName());
	        startActivity(intent);
	        break;
		case R.id.button2:
			// sync database
			// start SyncerService
			intent = new Intent(this, TestSyncerService.class);
			intent.putExtra("database_name", database.getName());
			startService(intent);
			break;
		case R.id.button3:
			// purge docs
			dbstore.purge(database);
			updateView();
			break;
		case R.id.button4:
			// delete database
			dbstore.purge(database, true);
			finish();
			break;
		}
	}

	// called when syncer progress is made
	class SyncerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			double progress = 100 * intent.getDoubleExtra(SyncerService.SYNCER_PROGRESS_OVERALL, 0.0);
			double progressDocuments = 100 * intent.getDoubleExtra(SyncerService.SYNCER_PROGRESS_DOCUMENTS, 0.0);
			double progressAttachments = 100 * intent.getDoubleExtra(SyncerService.SYNCER_PROGRESS_ATTACHMENTS, 0.0);
			boolean finished = intent.getBooleanExtra(SyncerService.SYNCER_FINISHED, false);
			long sequenceId = intent.getLongExtra(SyncerService.SYNCER_SEQUENCE_ID, 0);
			progressBars[0].setProgress((int)progressDocuments);
			progressBars[1].setProgress((int)progressAttachments);			
			progressBars[2].setProgress((int)progress);
	        textView[3].setText(Long.toString(sequenceId));

	        database.setSequenceId(sequenceId);
			if(finished) updateView();  // update number of documents etc
			Log.d("SyncerReceiver", String.format("syncer updated. progress: %.1f%%, %.1f%%, %.1f%%", progressDocuments, progressAttachments, progress));
		}
	}
}
