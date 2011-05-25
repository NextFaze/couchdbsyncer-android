couchdbsyncer-android
=====================

syncs couchdb databases from the server, saving documents and attachments locally in an sqlite database.

Installation
------------

create a jar from au.com.team2moro.couchdbsyncer (include au.com.team2moro.couchdbsyncer.data), and add to your Android project.

Usage
-----

DatabaseStore handles the storage of documents and attachments from one or more CouchDB databases.  Applications will typically create a single shared instance of DatabaseStore.

a Syncer object is used to download changes from a remote CouchDB database to the local sqlite database. The Syncer uses the bulk document fetch API (http://wiki.apache.org/couchdb/HTTP_Bulk_Document_API) to fetch all changed documents in a single HTTP request.

SyncerService can be subclassed to provide a IntentService to run a syncer update. At minimum your subclass must provide an implementation for the abstract getDatabaseStore() method.

See also the au.com.team2moro.couchdbsyncertest application for an example application.

Synopsis
--------

create a DatabaseStore and a Database to sync to (this will probably happen in your Application class)

    @Override
    public void onCreate() {
        super.onCreate();
        
        dbstore = new DatabaseStore(this);
        database = dbstore.getDatabase("my_database_name");
        if(database == null) {
            // create database
            database = dbstore.addDatabase("my_database_name", "http://example.com:5984/db_name");
        }
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

(The MIT License)

Copyright (c) 2moro mobile

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
'Software'), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
