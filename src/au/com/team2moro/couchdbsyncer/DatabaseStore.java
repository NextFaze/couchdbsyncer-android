package au.com.team2moro.couchdbsyncer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private static final String[] DB_DATABASE_COLUMNS = { "_id", "name", "sequence_id", "doc_del_count", "db_name", "url" };
	private static final String[] DB_DOCUMENT_COLUMNS = { "_id", "doc_id", "database_id", "revision", "content", "object", "parent_id", "type", "tags" };
	private static final String[] DB_ATTACHMENT_COLUMNS = { "_id", "document_id", "doc_id", "filename", "length", "content_type", "stale", "content" };
	private static final String[] DB_TYPE_COLUMN = { "type" };
	private static final String[] DB_NAME_COLUMN = { "name" };

	public DatabaseStore(Context context) {
		context = context.getApplicationContext();
		this.dbHelper = new DbHelper(context);
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void purge() {
		// TODO
	}
	public void purge(Database database) {
		// TODO
	}

	public int getSequenceId(Database database) {
		return database.getSequenceId();
	}
	
	public void updateDocument(Database database, Document document, int sequenceId) {
		Log.d(TAG, String.format("updating document %s (seq:%d)", document.getDocId(), sequenceId));
		
		if(document.isDeleted()) {
			// delete document and attachments, updates sequence id
			deleteDocument(database, document);
			return;
		}
		
		// fetch existing document from database
		Document dbDocument = getDocument(database, document.getDocId());
		Set<Attachment> oldAttachments = new HashSet<Attachment>();
		Set<Attachment> updatedAttachments = new HashSet<Attachment>();
		
		if(dbDocument == null) {
			// new document
			dbDocument = document;
		} else {
			// existing document
			dbDocument.setRevision(document.getRevision());
			dbDocument.setContent(document.getContent());
			dbDocument.setType(document.getType());
			dbDocument.setParentId(document.getParentId());
			dbDocument.setTags(document.getTags());
			
			oldAttachments.addAll(dbDocument.getAttachments());
		}
		
	    //NSMutableSet *old = [NSMutableSet setWithSet:document.attachments];
	    //NSMutableSet *new = [NSMutableSet set];
	    
		for(Attachment attachment : document.getAttachments()) {
			Attachment dbAttachment = dbDocument.getAttachment(attachment.getFilename());			
			oldAttachments.remove(attachment);  // this attachment still exists
			
			if(dbAttachment == null) {
				dbAttachment = new Attachment(attachment.getFilename());
			}
			
			if(attachment.getRevision() != dbAttachment.getRevision()) {
				// attachment not yet downloaded or revision has changed
				// update attachment attributes
				dbAttachment.setStale(true);
				dbAttachment.setContentType(attachment.getContentType());
				dbAttachment.setDocumentId(attachment.getDocumentId());
				dbAttachment.setRevision(attachment.getRevision());
				
				updatedAttachments.add(dbAttachment);  // need to download this attachment
			}
		}
		
		// write changes to database
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		try {
			ContentValues values = contentValuesDocument(dbDocument);
			dbrw.beginTransaction();
			
			if(document == dbDocument) {
				// new: insert
				Log.d(TAG, "inserting document");
				dbrw.insert("documents", null, values);
			} else {
				// existing: update
				Log.d(TAG, "updating document");
				String[] whereArgs = { Integer.toString(dbDocument.getDocumentId()) };
				dbrw.update("documents", values, "_id = ?", whereArgs);
			}
			
			// remove local attachments that are no longer attached to the document
			for(Attachment attachment : oldAttachments) {
				Log.d(TAG, "removing attachment: " + attachment.getFilename());
				String[] attachmentWhere = { Integer.toString(attachment.getAttachmentId()) };
				dbrw.delete("attachments", "_id = ?", attachmentWhere);
			}
			document.getAttachments().remove(oldAttachments);  // update in-memory document state

			// save metadata for new attachments
			for(Attachment attachment : updatedAttachments) {
				ContentValues attachmentValues = contentValuesAttachment(attachment);

				if(attachment.getAttachmentId() >= 0) {
					// existing: update
					String[] whereArgs = { Integer.toString(attachment.getAttachmentId()) };
					dbrw.update("attachments", attachmentValues, "_id = ?", whereArgs);
				} else {
					// new: insert
					dbrw.insert("attachments", null, attachmentValues);
				}
			}
			
			// update sequence Id
			writeDatabaseSequenceId(dbrw, database);
			dbrw.setTransactionSuccessful();
		} finally {
			dbrw.endTransaction();
			dbrw.close();
		}
	}
	
	public void updateAttachment(Database database, Document document, Attachment attachment) {
		Log.d(TAG, "updating attachment: " + attachment.getFilename());
		
		Attachment dbAttachment = getAttachment(database, document, attachment.getFilename());
		if(dbAttachment == null) {
			// attachment record should be in the database
			Log.d(TAG, "internal error: no attachment record found for " + attachment.getFilename());
			return;
		}

		dbAttachment.setStale(false);
		dbAttachment.setContent(attachment.getContent());
		dbAttachment.setLength(attachment.getLength());
		dbAttachment.setRevision(attachment.getRevision());

		// write changes to database
	}
	
	public Map<String, Object> getStatistics(Database database) {
		// TODO
		return null;
	}

	public Document getDocument(Database database, String documentId) {
		// TODO
		return null;
	}

	public List<Document> getDocuments(Database database) {

		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		List<Document> documents = new ArrayList<Document>();
		Cursor cursor = null;
		try {
			//query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
			//Query the given table, returning a Cursor over the result set.
			
			cursor = db.query("documents", DB_DOCUMENT_COLUMNS, null, null, null, null, null);
			// "doc_id", "database_id", "revision", "content", "parent_id", "type", "tags"

			while(cursor.moveToNext()) {
				String docId = cursor.getString(0);
				Document document = new Document(docId);
				
				document.setDatabaseId(cursor.getInt(1));
				document.setRevision(cursor.getString(2));
				document.setContent(cursor.getBlob(3));
				document.setParentId(cursor.getString(4));
				document.setType(cursor.getString(5));
				document.setTags(cursor.getString(6));

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
	
	/**
	 * Delete a document and associated attachments from the local database.  updates sequence id
	 * @param document The document to delete
	 */
	private void deleteDocument(Database database, Document document) {
		String[] whereArgs = { Integer.toString(document.getDocumentId()) };
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		try {
			dbrw.beginTransaction();
			dbrw.delete("documents", "_id = ?", whereArgs);
			dbrw.delete("attachments", "document_id = ?", whereArgs);
			writeDatabaseSequenceId(dbrw, database);
			dbrw.setTransactionSuccessful();
		} finally {
			dbrw.endTransaction();
			dbrw.close();
		}
	}
	
	/**
	 * Read an attachment from the local database.
	 * @param database The database to read from
	 * @param document The document containing the attachment
	 * @param filename The filename of the attachment to read
	 * @return The attachment object
	 */
	public Attachment getAttachment(Database database, Document document, String filename) {
		Attachment attachment = null;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = null;

		try {
			String[] selectionArgs = { filename };
			cursor = db.query("attachments", DB_ATTACHMENT_COLUMNS, "filename = ?", selectionArgs, null, null, null);
			// "document_id", "doc_id", "filename", "length", "content_type", "stale", "content"

			if(cursor.moveToNext()) {
				attachment = new Attachment(cursor.getString(2));
				attachment.setDocumentId(cursor.getInt(0));
				attachment.setDocId(cursor.getString(1));
				attachment.setLength(cursor.getInt(3));
				attachment.setContentType(cursor.getString(4));
				attachment.setStale(cursor.getInt(5) == 0 ? false : true);
				attachment.setContent(cursor.getBlob(6));
			}
		}
		finally {
			if(cursor != null) cursor.close();
			db.close();
		}
		return attachment;
	}
	
	/**
	 * return a list of all databases managed by this store
	 * @return
	 */
	public List<Database> getDatabases() {
		// fetch database records
		// "_id", "name", "sequence_id", "doc_del_count", "db_name", "url"

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("databases", DB_DATABASE_COLUMNS, null, null, null, null, null);
		List<Database> databases = new ArrayList<Database>();

		while(cursor.moveToNext()) {
			String name = cursor.getString(1);
			String urlStr = cursor.getString(5);
			URL url = null;
			try {
				url = new URL(urlStr);
			} catch(MalformedURLException e) {
				Log.d(TAG, e.toString());
			}

			Database database = new Database(name, url);
			database.setDatabaseId(cursor.getInt(0));
			database.setSequenceId(cursor.getInt(2));
			database.setDocDelCount(cursor.getInt(3));

			databases.add(database);
		}
		cursor.close();
		db.close();	

		return databases;
	}

	/**
	 * return the database with the given name
	 * @param name The name of the database to find
	 * @return A database object, or null if the database was not found
	 */
	public Database getDatabase(String name) {
		for(Database database : getDatabases()) {
			if(database.getName().equals(name))
				return database;
		}
		return null;  // not found
	}
	

	public Database addDatabase(String name, URL url) {
		Database database = new Database(name, url);
		
		ContentValues values = contentValuesDatabase(database);
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		dbrw.insert("databases", null, values);
		dbrw.close();
		
		return database;
	}
	
	private void writeDatabaseSequenceId(SQLiteDatabase dbrw, Database database) {
		String[] whereArgs = { Integer.toString(database.getDatabaseId()) };
		ContentValues values = new ContentValues();
		values.put("sequence_id", database.getSequenceId());
		dbrw.update("databases", values, "_id = ?", whereArgs);
	}
	
	private ContentValues contentValuesDatabase(Database database) {
		ContentValues values = new ContentValues();
		values.put("name", database.getName());
		values.put("url", database.getUrl().toString());
		values.put("sequence_id", database.getSequenceId());
		values.put("doc_del_count", database.getDocDelCount());
		values.put("db_name", database.getDbName());
		
		return values;
	}
	
	// 	{ "doc_id", "database_id", "revision", "content", "object", "parent_id", "type", "tags" };
	private ContentValues contentValuesDocument(Document document) {
		ContentValues values = new ContentValues();
		values.put("doc_id", document.getDocId());
		values.put("database_id", document.getDatabaseId());
		values.put("revision", document.getRevision());
		values.put("content", document.getContentBytes());
		values.put("parent_id", document.getParentId());
		values.put("type", document.getType());
		values.put("tags", document.getTags());
		
		return values;
	}
	
	// { "document_id", "doc_id", "filename", "length", "content_type", "stale", "content" };
	private ContentValues contentValuesAttachment(Attachment attachment) {
		ContentValues values = new ContentValues();
		values.put("document_id", attachment.getDocumentId());
		values.put("doc_id", attachment.getDocId());
		values.put("filename", attachment.getFilename());
		values.put("length", attachment.getLength());
		values.put("content_type", attachment.getContentType());
		values.put("stale", attachment.isStale());
		values.put("content", attachment.getContent());

		return values;
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

