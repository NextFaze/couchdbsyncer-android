package au.com.team2moro.couchdbsyncer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseStore implements Store {
	private static final String TAG = "DatabaseStore";
	private DbHelper dbHelper;
	
	private static final String[] DB_DATABASE_COLUMNS = { "_id", "name", "sequence_id", "doc_del_count", "db_name", "url" };
	private static final String[] DB_DOCUMENT_COLUMNS = { "_id", "doc_id", "database_id", "revision", "content", "parent_id", "type", "tags" };
	private static final String[] DB_ATTACHMENT_CONTENT_COLUMNS = { "_id", "document_id", "doc_id", "revision", "filename", "length", "content_type", "stale", "content" };
	private static final String[] DB_ATTACHMENT_COLUMNS = { "_id", "document_id", "doc_id", "revision", "filename", "length", "content_type", "stale" };
	private static final String[] DB_TYPE_COLUMN = { "type" };

	public DatabaseStore(Context context) {
		context = context.getApplicationContext();
		this.dbHelper = new DbHelper(context);
	}
	
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Delete all documents and attachments from all databases
	 */
	public void purge() {
		for(Database database : getDatabases()) {
			purge(database);
		}
	}
	
	public synchronized void purge(Database database) {
		purge(database, false);
	}
	
	/**
	 * Delete all documents and attachments from the given database
	 * @param database The database to purge
	 */
	public synchronized void purge(Database database, boolean deleteDatabase) {
		String[] whereArgs = { Long.toString(database.getDatabaseId()) };
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		Log.d(TAG, "deleting database: " + database.getName());
		
		try {
			dbrw.beginTransaction();
			dbrw.delete("attachments", "document_id IN (SELECT _id FROM documents WHERE database_id = ?)", whereArgs);
			dbrw.delete("documents", "database_id = ?", whereArgs);
			if(deleteDatabase) {
				dbrw.delete("databases", "database_id = ?", whereArgs);
			} else {
				database.setSequenceId(0);
				writeDatabaseSequenceId(dbrw, database);
			}
			dbrw.setTransactionSuccessful();
		} finally {
			dbrw.endTransaction();
			dbrw.close();
		}
	}

	public long getSequenceId(Database database) {
		return database.getSequenceId();
	}
	
	public synchronized void updateDocument(Database database, Document document, int sequenceId) {
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
		boolean newDocument = false;
		
		if(dbDocument == null) {
			// new document
			dbDocument = document;
			newDocument = true;
		} else {
			// existing document
			dbDocument.setRevision(document.getRevision());
			dbDocument.setContent(document.getContent());
			dbDocument.setType(document.getType());
			dbDocument.setParentId(document.getParentId());
			dbDocument.setTags(document.getTags());
			
			oldAttachments.addAll(dbDocument.getAttachments());
		}

		for(Attachment attachment : document.getAttachments()) {
			Attachment dbAttachment = dbDocument.getAttachment(attachment.getFilename());			
			oldAttachments.remove(attachment);  // this attachment still exists
			
			if(newDocument || dbAttachment == null) {
				// create new attachment object
				dbAttachment = new Attachment(attachment.getFilename());
				dbAttachment.setDocId(document.getDocId());
				// documentId set later (after document commit)
			}
			
			Log.d(TAG, "attachment: new revision = " + attachment.getRevision() + ", old revision = " + dbAttachment.getRevision());
			if(attachment.getRevision() != dbAttachment.getRevision()) {
				// attachment not yet downloaded or revision has changed
				// update attachment attributes
				attachment.setStale(true);    // provides feedback to Syncer that this needs to be downloaded
				dbAttachment.setStale(true);
				dbAttachment.setContentType(attachment.getContentType());
				dbAttachment.setRevision(attachment.getRevision());
				
				updatedAttachments.add(dbAttachment);  // need to download this attachment
				Log.d(TAG, "attachment changed: " + dbAttachment);				
			}
		}
		
		// write changes to database
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		try {
			ContentValues values = contentValuesDocument(dbDocument);
			dbrw.beginTransaction();
			
			if(document == dbDocument) {
				// new: insert
				values.put("database_id", database.getDatabaseId());
				Log.d(TAG, "new document, inserting");
				long documentId = dbrw.insert("documents", null, values);
				document.setDocumentId(documentId);
			} else {
				// existing: update
				Log.d(TAG, "existing document, updating");
				String[] whereArgs = { Long.toString(dbDocument.getDocumentId()) };
				dbrw.update("documents", values, "_id = ?", whereArgs);
			}
			
			// remove local attachments that are no longer attached to the document
			for(Attachment attachment : oldAttachments) {
				Log.d(TAG, "removing attachment: " + attachment.getFilename());
				String[] attachmentWhere = { Long.toString(attachment.getAttachmentId()) };
				dbrw.delete("attachments", "_id = ?", attachmentWhere);
			}
			document.getAttachments().remove(oldAttachments);  // update in-memory document state

			// save metadata for new attachments
			for(Attachment attachment : updatedAttachments) {
				// set attachment document id (required if this is a new document, as documentId only known after document insert)
				attachment.setDocumentId(dbDocument.getDocumentId());
				ContentValues attachmentValues = contentValuesAttachment(attachment);

				if(attachment.getAttachmentId() >= 0) {
					// existing: update
					String[] whereArgs = { Long.toString(attachment.getAttachmentId()) };
					dbrw.update("attachments", attachmentValues, "_id = ?", whereArgs);
				} else {
					// new: insert
					attachmentValues.put("document_id", document.getDocumentId());
					dbrw.insert("attachments", null, attachmentValues);
				}
			}
			
			// update sequence Id
			database.setSequenceId(sequenceId);
			writeDatabaseSequenceId(dbrw, database);
			dbrw.setTransactionSuccessful();
		} finally {
			dbrw.endTransaction();
			dbrw.close();
		}
	}
	
	public synchronized void updateAttachment(Database database, Document document, Attachment attachment) {
		Log.d(TAG, "updating attachment: " + attachment.getFilename());
		
		Attachment dbAttachment = getAttachment(database, document, attachment.getFilename());
		if(dbAttachment == null) {
			// attachment record should be in the database at this point
			Log.d(TAG, "internal error: no attachment record found for " + attachment.getFilename());
			return;
		}

		dbAttachment.setStale(false);
		dbAttachment.setContent(attachment.getContent());
		dbAttachment.setLength(attachment.getLength());
		dbAttachment.setRevision(attachment.getRevision());
		dbAttachment.setContentType(attachment.getContentType());

		// write changes to database
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		try {
			String[] whereArgs = { Long.toString(dbAttachment.getAttachmentId()) };
			ContentValues attachmentValues = contentValuesAttachment(dbAttachment);
			dbrw.update("attachments", attachmentValues, "_id = ?", whereArgs);
		} finally {
			dbrw.close();
		}
	}
	
	public long getDocumentCount(Database database) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		SQLiteStatement stmt = db.compileStatement("SELECT count(*) FROM documents WHERE database_id = " + database.getDatabaseId());
		long count = stmt.simpleQueryForLong();
		db.close();
		return count;
	}
	
	public long getAttachmentCount(Database database) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		SQLiteStatement stmt = db.compileStatement("SELECT count(*) FROM attachments WHERE document_id IN (SELECT _id FROM documents WHERE database_id = " + database.getDatabaseId() + ")");
		long count = stmt.simpleQueryForLong();
		db.close();
		return count;
	}
	
	public Document getDocument(Database database, String docId) {
		String[] selectionArgs = { Long.toString(database.getDatabaseId()), docId };
		List<Document> documents = getDocuments("database_id = ? AND doc_id = ?", selectionArgs, 1);

		return (documents.size() == 1) ? documents.get(0) : null;
	}

	public List<Document> getDocuments(Database database) {
		String[] selectionArgs = { Long.toString(database.getDatabaseId()) };
		return getDocuments("database_id = ?", selectionArgs, -1);
	}

	private List<Document> getDocuments(String selection, String[] selectionArgs, int limit) {

		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		List<Document> documents = new ArrayList<Document>();
		Cursor cursor = null;
		
		try {
			//query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy)
			//Query the given table, returning a Cursor over the result set.
			
			cursor = db.query("documents", DB_DOCUMENT_COLUMNS, selection, selectionArgs, null, null, null);
			// "_id", "doc_id", "database_id", "revision", "content", "parent_id", "type", "tags"

			while(cursor.moveToNext()) {
				String docId = cursor.getString(1);
				Document document = new Document(docId);
				
				document.setDocumentId(cursor.getInt(0));
				document.setDatabaseId(cursor.getInt(2));
				document.setRevision(cursor.getString(3));
				document.setContent(cursor.getBlob(4));
				document.setParentId(cursor.getString(5));
				document.setType(cursor.getString(6));
				document.setTags(cursor.getString(7));

				documents.add(document);
				
				if(limit > 0 && documents.size() == limit)
					break;
			}
		} finally {
			if(cursor != null) cursor.close();
			db.close();
		}
		
		return documents;
	}
	
	public List<Document> getDocuments(Database database, String type) {
		String[] selectionArgs = { Long.toString(database.getDatabaseId()), type };
		return getDocuments("database_id = ? AND type = ?", selectionArgs, -1);
	}
	
	public List<Document> getDocuments(Database database, String type, String tag) {
		String[] selectionArgs = { Long.toString(database.getDatabaseId()), type, tag };
		return getDocuments("database_id = ? AND type = ? AND tag LIKE ?", selectionArgs, -1);
	}

	/**
	 * Find and return a list of all distinct document types in the store
	 */
	public List<String> getDocumentTypes(Database database) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String[] selectionArgs = { Long.toString(database.getDatabaseId()) };
		Cursor cursor = db.query(true, "documents", DB_TYPE_COLUMN, "_id = ?", selectionArgs, null, null, null, null);
		List<String> types = new ArrayList<String>();
		
		while(cursor.moveToNext()) {
			types.add(cursor.getString(0));
		}
		return types;
	}
	
	/**
	 * Delete a document and associated attachments from the local database.  updates sequence id
	 * @param document The document to delete
	 */
	private synchronized void deleteDocument(Database database, Document document) {
		String[] whereArgs = { Long.toString(document.getDocumentId()) };
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		Log.d(TAG, "deleting document: " + document.getDocumentId());
		
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

	private List<Attachment> getAttachments(String selection, String[] selectionArgs, int limit, boolean fetchContent) {
		List<Attachment> attachments = new ArrayList<Attachment>();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = null;

		try {
			String[] columns = fetchContent ? DB_ATTACHMENT_CONTENT_COLUMNS : DB_ATTACHMENT_COLUMNS;
			cursor = db.query("attachments", columns, selection, selectionArgs, null, null, null);
			// "_id", "document_id", "doc_id", "revision", "filename", "length", "content_type", "stale", "content"
			
			while(cursor.moveToNext()) {
				Attachment attachment = new Attachment(cursor.getString(4));
				attachment.setAttachmentId(cursor.getInt(0));
				attachment.setDocumentId(cursor.getInt(1));
				attachment.setDocId(cursor.getString(2));
				attachment.setRevision(cursor.getInt(3));
				attachment.setLength(cursor.getInt(5));
				attachment.setContentType(cursor.getString(6));
				attachment.setStale(cursor.getInt(7) == 0 ? false : true);
				if(fetchContent) attachment.setContent(cursor.getBlob(8));
				
				attachments.add(attachment);
				
				if(limit > 0 && attachments.size() == limit)
					break;
			}
		}
		finally {
			if(cursor != null) cursor.close();
			db.close();
		}
		return attachments;
	}
	
	/**
	 * Read an attachment from the local database.
	 * @param database The database to read from
	 * @param document The document containing the attachment
	 * @param filename The filename of the attachment to read
	 * @return The attachment object
	 */
	public Attachment getAttachment(Database database, Document document, String filename) {
		String[] selectionArgs = { Long.toString(document.getDocumentId()), filename };
		List<Attachment> attachments = getAttachments("document_id = ? AND filename = ?", selectionArgs, 1, true);
		return attachments.size() == 1 ? attachments.get(0) : null;
	}
	
	/**
	 * Read attachments from the local database.
	 * @param database The database to read from
	 * @param document The document to read attachments from
	 * @return A list of attachments
	 */
	public List<Attachment> getAttachments(Database database, Document document) {
		String[] selectionArgs = { Long.toString(document.getDocumentId()) };
		return getAttachments("document_id = ?", selectionArgs, -1, true);
	}
	
	public List<Attachment> getStaleAttachments(Database database) {
		String[] selectionArgs = { Long.toString(database.getDatabaseId()) };
		Log.d(TAG, "database id: " + database.getDatabaseId());
		return getAttachments("stale = 1 AND document_id IN (SELECT _id FROM documents WHERE database_id = ?)", selectionArgs, -1, false);
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
	

	public synchronized Database addDatabase(String name, URL url) {
		Database database = new Database(name, url);
		
		ContentValues values = contentValuesDatabase(database);
		SQLiteDatabase dbrw = this.dbHelper.getWritableDatabase();
		long databaseId = dbrw.insert("databases", null, values);
		dbrw.close();
		
		database.setDatabaseId(databaseId);
		
		return database;  // re-fetch database so we have the database id
	}
	
	private void writeDatabaseSequenceId(SQLiteDatabase dbrw, Database database) {
		String[] whereArgs = { Long.toString(database.getDatabaseId()) };
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
	
	// { "document_id", "doc_id", "revision", "filename", "length", "content_type", "stale", "content" };
	private ContentValues contentValuesAttachment(Attachment attachment) {
		ContentValues values = new ContentValues();
		values.put("document_id", attachment.getDocumentId());
		values.put("doc_id", attachment.getDocId());
		values.put("revision", attachment.getRevision());
		values.put("filename", attachment.getFilename());
		values.put("length", attachment.getLength());
		values.put("content_type", attachment.getContentType());
		values.put("stale", attachment.isStale());
		values.put("content", attachment.getContent());

		return values;
	}
	
	private class DbHelper extends SQLiteOpenHelper {
        static final String TAG = "DBHelper";
        static final int DB_VERSION = 1;
        static final String DB_NAME = "couchdbsyncer.db";

        public DbHelper(Context context) {
        	super(context, DB_NAME, null, DB_VERSION);
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

