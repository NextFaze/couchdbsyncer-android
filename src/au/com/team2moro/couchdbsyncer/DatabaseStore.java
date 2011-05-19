package au.com.team2moro.couchdbsyncer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseStore implements Store {
	public static final String TAG = "DatabaseStore";
	private String name;
	private DbHelper dbHelper;
	
	private static final String[] DB_DOCUMENT_COLUMNS = { "doc_id", "database_id", "revision", "content", "object", "parent_id", "type", "tags" };
	private static final String[] DB_DATABASE_COLUMNS = { "name", "sequence_id", "doc_del_count", "db_name", "url", "username", "password" };
	private static final String[] DB_TYPE_COLUMN = { "type" };
	private static final String[] DB_NAME_COLUMN = { "name" };

	public DatabaseStore(Context context) {
		context = context.getApplicationContext();
		this.dbHelper = new DbHelper(context);
	}
	
	private void doStuffDeprecated() {
		/*
		this.name = name;
		this.sequenceId = 0;
		
		this.dbHelper = new DbHelper(context);
		this.database = null;
		*/
		
		/*
		// fetch or create database record if it doesn't exist
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = db.query("databases", DB_DATABASE_COLUMNS, null, null, null, null, null);

		while(cursor.moveToNext()) {
			String dbName = cursor.getString(0);
			String dbUrl = cursor.getString(4);
			if(dbName.equals(name)) {
				// found
				database = new Database(name, url);  // change to given url
				database.setSequenceId(Integer.parseInt(cursor.getString(1)));
				database.setDocDelCount(Integer.parseInt(cursor.getString(2)));
				database.setUsername(username);
				database.setPassword(password);
				
				break;
			}
		}
		cursor.close();
		db.close();
		
		if(database == null) {
	        // add database record
			database = new Database(name, url);
			database.setUsername(username);
			database.setPassword(password);
			
			ContentValues values = new ContentValues();
			values.put("name", database.getName());
			values.put("url", database.getUrl().toString());
			values.put("sequence_id", database.getSequenceId());
			values.put("doc_del_count", database.getDocDelCount());
			values.put("db_name", database.getDbName());
			values.put("username", database.getUsername());
			values.put("password", database.getPassword());
			
			SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
			dbrw.insert("databases", null, values);
			dbrw.close();
	    }
	    */
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void purge() {
		// TODO
	}
	public int getSequenceId(Database database) {
		return database.getSequenceId();
	}
	
	public void updateDocument(Document document) {
		// TODO
	}
	public void updateAttachment(Document document, Attachment attachment) {
		// TODO
	}
	
	public void addDatabase(String name, URL url, String username, String password) {
		// TODO
	}
	
	public Map<String, Object> getStatistics() {
		// TODO
		return null;
	}

	public Document getDocument(String documentId) {
		// TODO
		return null;
	}
		
	public List<Document> getDocuments() {

		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		List<Document> documents = new ArrayList<Document>();
		Cursor cursor = null;
		try {
			//query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
			//Query the given table, returning a Cursor over the result set.
			
			cursor = db.query("documents", DB_DOCUMENT_COLUMNS, null, null, null, null, null);
			// "doc_id", "database_id", "revision", "content", "object", "parent_id", "type", "tags"
			while(cursor.moveToNext()) {
				String documentId = cursor.getString(0);
				Document document = new Document(documentId);
				
				document.setRevision(cursor.getString(2));
				document.setContent(cursor.getString(3));
				//document.setObject(cursor.getString(4));   // TODO: deserialize object
				document.setParentId(cursor.getString(5));
				document.setType(cursor.getString(6));
				document.setTags(cursor.getString(7));

				documents.add(document);
			}
		} finally {
			if(cursor != null) cursor.close();
			db.close();
		}
		
		return documents;
	}
	
	public List<Document> getDocuments(Database database, String type) {
		// TODO
		return null;
	}
	public List<Document> getDocuments(Database database, String type, String tag) {
		// TODO
		return null;
	}
	
	public List<Database> getDatabases() {
		return dbHelper.getDatabases();
	}

	public Database getDatabase(String name) {
		return null;  // TODO
	}
	
	/**
	 * Find and return a list of all distinct document types in the store
	 */
	public List<String> getDocumentTypes(Database database) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = db.query(true, "documents", DB_TYPE_COLUMN, null, null, null, null, null, null);
		List<String> types = new ArrayList<String>();
		
		while(cursor.moveToNext()) {
			types.add(cursor.getString(0));
		}
		return types;
	}
	
	private class DbHelper extends SQLiteOpenHelper {
        static final String TAG = "DBHelper";
        static final int DB_VERSION = 1;
        static final String DB_NAME = "couchdbsyncer.db";
        Context context;

        public DbHelper(Context context) {
        	super(context, DB_NAME, null, DB_VERSION);
        	this.context = context;
        }

        // Called only once, first time the DB is created
        @Override
        public void onCreate(SQLiteDatabase db) {
        	Log.d(TAG, "onCreate");
        	String sql = readFile("data/schema.sql");
        	String[] tables = sql.split("-- SPLIT");
        	Log.d(TAG, "table count: " + tables.length);
        	for(String table : tables) {
            	Log.d(TAG, "onCreate sql: " + table);
        		db.execSQL(table);
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Log.d(TAG, "onUpgrade");
        	String[] tables = {"databases", "documents", "attachments"};
        	for(String table : tables) {
        		db.execSQL("drop table if exists " + table);
        	}
        	onCreate(db); // run onCreate to get new database
        }

        public List<Database> getDatabases() {
        	// fetch or create database record if it doesn't exist
        	SQLiteDatabase db = getReadableDatabase();
        	Cursor cursor = db.query("databases", DB_DATABASE_COLUMNS, null, null, null, null, null);
        	List<Database> databases = new ArrayList<Database>();
        	
        	while(cursor.moveToNext()) {
        		String name = cursor.getString(0);
        		String urlStr = cursor.getString(4);
        		URL url = null;
        		try {
        			url = new URL(urlStr);
        		} catch(MalformedURLException e) {
        			Log.d(TAG, e.toString());
        		}

        		Database database = new Database(name, url);
       			database.setSequenceId(Integer.parseInt(cursor.getString(1)));
       			database.setDocDelCount(Integer.parseInt(cursor.getString(2)));
       			database.setUsername(cursor.getString(5));
       			database.setPassword(cursor.getString(6));

       			databases.add(database);
        	}
        	cursor.close();
        	db.close();	
        	
        	return databases;
        }
        
        private String readFile(String s) {
        	String result = "";
        	String thisLine;
        	try {
        		InputStream is = getClass().getResourceAsStream(s);
        		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        		while ((thisLine = br.readLine()) != null) {
        			result += thisLine;
        		}
        	} 
        	catch (Exception e) {
        		Log.d(TAG, e.toString());
        	}
        	return result;
        }
	}
}

