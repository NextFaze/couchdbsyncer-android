/**
 * couchdbsyncer - syncs couchdb databases from the server, saving documents and attachments locally in an sqlite database.
 * <p>
 * DatabaseStore handles the storage of documents and attachments from one or more CouchDB databases.  Applications will typically create a single shared instance of DatabaseStore.
 * a Syncer object is used to synchronize a local database with a remote CouchDB database.
 * SyncerService can be subclassed to provide a IntentService to run a syncer update.
 */

package au.com.team2moro.couchdbsyncer;
