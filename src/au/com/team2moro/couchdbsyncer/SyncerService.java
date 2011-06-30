package au.com.team2moro.couchdbsyncer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Observable;
import java.util.Observer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public abstract class SyncerService extends IntentService {
	private static final String TAG = "SyncerService";
	public static final String SYNCER_PROGRESS_INTENT = "au.com.team2moro.couchdbsyncer.SYNCER_PROGRESS";
	public static final String SYNCER_PROGRESS_DOCUMENTS = "documents";
	public static final String SYNCER_PROGRESS_ATTACHMENTS = "attachments";
	public static final String SYNCER_PROGRESS_OVERALL = "overall";
	public static final String SYNCER_FINISHED = "finished";
	public static final String SYNCER_SEQUENCE_ID = "sequenceId";
	
	private Syncer syncer;
	private Database database;
	
	public SyncerService() {
		super(TAG);

		Log.d(TAG, "SyncerService constructed");
	}
	
	/**
	 * Get the credentials to use to synchronize with the remote database.  Returns null by default
	 * @return the credentials
	 */
	protected Credentials getCredentials() {
		return null;
	}
	
	/**
	 * Get the database store to synchronize.
	 * @return the DatabaseStore object to sync
	 */
	protected abstract DatabaseStore getDatabaseStore();
	
	/**
	 * Get the database to synchronize.  This is initialised based on the "database_name" string extra sent to the IntentService.
	 * @return the Database to sync
	 */
	protected Database getDatabase() {
		return database;
	}
	
	/**
	 * Get the download policy implementation for this sync. Returns null by default
	 * @return An object implementing StoreDownloadPolicy
	 */
	protected DownloadPolicy getDownloadPolicy() {
		return null;
	}
	
	/**
	 * Runs Syncer against the database. Broadcast Intents are sent regularly with details of sync progress.
	 */
	@Override
	protected void onHandleIntent(Intent arg0) {
		String dbname = arg0.getStringExtra("database_name");
		DatabaseStore dbstore = getDatabaseStore();
		if(dbstore == null) {
			Log.d(TAG, "database store not defined");
			return;
		}
		database = dbstore.getDatabase(dbname);
		if(database == null) {
			// database not found, return
			Log.d(TAG, "database " + dbname + " not found");
			return;
		}

		try {
			syncer = new Syncer(dbstore, database, getCredentials());
			syncer.addObserver(new SyncerObserver());
			syncer.update(getDownloadPolicy());
			syncer.deleteObservers();
		} catch(Exception e) {
			Log.d(TAG, "syncer error: " + getStackTraceAsString(e));
		}
	}

	private class SyncerObserver implements Observer {
		
		public void update(Observable arg0, Object arg1) {
			// broadcast syncer progress
			Intent intent = new Intent(SYNCER_PROGRESS_INTENT);
			intent.putExtra(SYNCER_PROGRESS_DOCUMENTS, syncer.getProgressDocuments());
			intent.putExtra(SYNCER_PROGRESS_ATTACHMENTS, syncer.getProgressAttachments());
			intent.putExtra(SYNCER_PROGRESS_OVERALL, syncer.getProgress());
			intent.putExtra(SYNCER_FINISHED, syncer.isFinished());
			intent.putExtra(SYNCER_SEQUENCE_ID, database.getSequenceId());
			sendBroadcast(intent);
		}
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
