package au.com.team2moro.couchdbsyncertest;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import au.com.team2moro.couchdbsyncer.Database;
import au.com.team2moro.couchdbsyncer.DatabaseStore;
import au.com.team2moro.couchdbsyncer.Syncer;

public class SyncerService extends IntentService {
	private static final String TAG = "SyncerService";
	public static final String SYNCER_PROGRESS_INTENT = "au.com.team2moro.couchdbsyncer.SYNCER_PROGRESS";
	public static final String SYNCER_PROGRESS_DOCUMENTS = "documents";
	public static final String SYNCER_PROGRESS_ATTACHMENTS = "attachments";
	public static final String SYNCER_PROGRESS_OVERALL = "overall";

	private Syncer syncer;
	
	public SyncerService() {
		super(TAG);

		Log.d(TAG, "SyncerService constructed");
	}
	
	@Override
	protected void onHandleIntent(Intent arg0) {
		String dbname = arg0.getStringExtra("database_name");
		DatabaseStore dbstore = ((TestApplication) getApplication()).getDatabaseStore();
		Database database = dbstore.getDatabase(dbname);
		if(database == null) {
			// database not found, return
			Log.d(TAG, "database " + dbname + " not found");
			return;
		}

		try {
			syncer = new Syncer(dbstore, database);
			syncer.update();
		} catch(Exception e) {
			Log.d(TAG, "syncer error: " + e.getMessage());
		}
	}

	private void broadcastProgress() {
        Intent intent = new Intent(SYNCER_PROGRESS_INTENT);
        intent.putExtra(SYNCER_PROGRESS_DOCUMENTS, syncer.getProgressDocuments());
        intent.putExtra(SYNCER_PROGRESS_ATTACHMENTS, syncer.getProgressAttachments());
        intent.putExtra(SYNCER_PROGRESS_OVERALL, syncer.getProgress());
        sendBroadcast(intent);
	}

	private String getStackTraceAsString(Exception exception) { 
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
