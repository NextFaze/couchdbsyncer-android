couchdbsyncer-android
=====================

syncs couchdb databases from the server, saving documents and attachments locally in an sqlite database.

Adding to your project
----------------------

Reference the couchdbsyncer-android project as a library (Properties -> Android -> Library -> Add in eclipse) and reference libs/*.jar as external jars.
Alternatively, create a jar from au.com.team2moro.couchdbsyncer (include au.com.team2moro.couchdbsyncer.data), and add to your Android project.

You must have the following line in your AndroidManifest.xml:

    <uses-permission android:name="android.permission.INTERNET"></uses-permission>
    
Usage
-----

DatabaseStore handles the storage of documents and attachments from one or more CouchDB databases.  Applications will typically create a single shared instance of DatabaseStore.

a Syncer object is used to download changes from a remote CouchDB database to the local sqlite database. The Syncer uses the bulk document fetch API (http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API) to fetch all changed documents in a single HTTP request.

SyncerService can be subclassed to provide a IntentService to run a syncer update. At minimum your subclass must provide an implementation for the abstract getDatabaseStore() method.

See also the au.com.team2moro.couchdbsyncerexample application for an example application.

Synopsis
--------

create a DatabaseStore and a Database to sync to (this will probably happen in your Application class)

    public static final String DB_NAME = "my_database_name";
    public static final String SHIPPED_DB = "my_db.sqlite";   // in assets folder
    public static final String DB_URL = "http://example.com:5984/db_name";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // create a new database store.
        // if SHIPPED_DB is newer than the internal database or the
        // internal database does not exist, SHIPPED_DB is installed
        // as the current database.
        dbstore = new DatabaseStore(this, SHIPPED_DB);

        // get the database DB_NAME.  updates the database url to the given URL.
        // creates a new local database record if it has not been created yet.
        database = dbstore.getDatabase(DB_NAME, new URL(DB_URL));
    }
    public DatabaseStore getDatabaseStore() {
        return dbstore;
    }
    public Database getDatabase() {
        return database;
    }
 
Subclass SyncerService to create an IntentService that will run a sync operation.  Your subclass can provide HTTP credentials and a download policy if required.  Your implementation might look like:

    public class MySyncerService extends SyncerService {
        public DatabaseStore getDatabaseStore() {
            return ((MyApplication)getApplication()).getDatabaseStore();
        }
    }

Then sync the database like this:

    intent = new Intent(this, MySyncerService.class);
    intent.putExtra("database_name", "my_database_name");
    startService(intent);

Download policies
-----------------

The DownloadPolicy interface allows control over which documents and attachments are downloaded to the local sqlite database from the server.  By default, all documents and attachments are downloaded except design documents.
It also allows control over download thread priority, which currently has no effect because the downloads all run in a single thread.
TODO: implement multi-thread downloads.

Accessing documents / attachments
---------------------------------

See DatabaseStore getDocument() and getAttachment() methods.
Some example code that fetches all documents of type 'Dog', and prints their name:

    List<Document> dogs = dbstore.getDocuments(database, "Dog");
    for(Document dog : dogs) {
        System.out.println(dog.getString("name"));
    }

Documents retrieved from the database contain metadata about attachments accessible via the Document#getAttachments() method.  To read the attachment content (stored as BLOB binary data in sqlite), use one of the getAttachment() or getAttachments() methods of DatabaseStore.  e.g. to fetch a dog image:

    Attachment attachment = dbstore.getAttachment(dog, "image.jpg");
    byte[] data = attachment.getContent();
    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

License
-------
see LICENSE.txt
Copyright 2011 2moro mobile
