package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import android.util.Log;

public class Syncer {
	public static final String TAG = "Syncer";
	
    private int countReq;
    private Store store;
    private Database database;
    private String username, password;
    private boolean aborted;
    
	public Syncer(Store store, Database database) {
		this.store = store;
		this.database = database;
		this.aborted = false;
	}
	
	public Syncer(Store store, Database database, String username, String password) {
		this(store, database);
		this.username = username;
		this.password = password;
	}
	
	public boolean update() throws IOException {
		return update(null);
	}
	
	public boolean update(StoreDownloadPolicy policy) throws IOException {
		
		// fetch list of changes
        // example changes data:
        /*
         {"results":[
         {"seq":2,"id":"test2","changes":[{"rev":"1-e18422e6a82d0f2157d74b5dcf457997"}]}
         ],
         "last_seq":2}
         */
		Log.d(TAG, "updating store");
		
		int sid = store.getSequenceId(database);
		URL url = new URL(database.getUrl() + "/_changes?since=" + sid);
    	Fetcher fetcher = getFetcher();
		Map<String, Object> changes = fetcher.fetchJSON(url);
		if(changes == null) {
			// no changes
			Log.d(TAG, "no change list found");
			return false;
		}
		
		Collection<Map> results = (Collection<Map>) changes.get("results");
		Log.d(TAG, "results: " + results);

        // convert change list to document objects
        for(Map<String, Object> change : results) {
    		//Log.d(TAG, "processing change: " + change);
        	Integer seqId = (Integer) change.get("seq");
        	String documentId = (String) change.get("id");
        	//String revision = (String) change.get("revision");
        	Boolean deleted = (Boolean) change.get("deleted");
        	
        	Document document = new Document(documentId);
        	document.setDeleted(deleted == null ? false : deleted.booleanValue());
        	//document.setRevision(revision);
        	//doc.setSequenceId(change.get("seq"));
        	
        	updateDocument(document, seqId, policy);
        }

        // TODO: move this into DatabaseStore implementation ?
        /*
        // download unfetched attachments.
        List<Attachment> unfetchedAttachments = store.getUnfetchedAttachments();
        for(Attachment attachment : unfetchedAttachments) {
        	updateStoreAttachment(store, attachment, policy);
        }
        */

		return false;
	}
	
	public boolean updateDocument(Document document, int sequenceId) throws IOException {
		return updateDocument(document, sequenceId, null);
	}
	
	public boolean updateDocument(Document document, int sequenceId, StoreDownloadPolicy policy) throws IOException {
    	boolean download = !document.isDesignDocument();
    	int priority = Thread.NORM_PRIORITY;
    	Log.d(TAG, "update document: " + document.getDocId());
    	
    	if(policy != null) {
    		download = policy.getDocumentDownload(document);
    		if(download) priority = policy.getDocumentPriority(document);
    	}
    	
    	if(!download) return false;

    	if(aborted) {
    		Log.d(TAG, "syncer is aborted, returning");
    		return false;
    	}

    	if(document.isDeleted()) {
    		// document deleted 
    		store.updateDocument(database, document, sequenceId);
    	}
    	else {
    		// need to fetch document
    		// TODO: use BulkFetcher
    		
        	Fetcher fetcher = getFetcher();
    		URL documentURL = new URL(database.getUrl() + "/" + document.getDocId());
    		Map<String, Object> object = fetcher.fetchJSON(documentURL);
    		document.setContent(object, true);
    		store.updateDocument(database, document, sequenceId);
    	}
 
    	return false; // TODO
	}
	
	public boolean updateAttachment(Document document, Attachment attachment) throws IOException {
		return updateAttachment(document, attachment, null);
	}

	public boolean updateAttachment(Document document, Attachment attachment, StoreDownloadPolicy policy) throws IOException {
    	boolean download = true;
    	int priority = 4;  //  lower priority than normal
    	if(policy != null) {
    		download = policy.getAttachmentDownload(attachment);
    		if(download) priority = policy.getAttachmentPriority(attachment);
    	}
    	
    	if(!download) return false;
    	
    	if(aborted) {
    		Log.d(TAG, "syncer is aborted, returning");
    		return false;
    	}
    	
        if(document.isDeleted()) {
            // document is deleted
            return false;  // do nothing.  TODO: call update attachment?
        }
        
    	// TODO: don't fetch attachment if it is already in the queue to be fetched
        
        // fetch attachment
    	URL attachmentURL = new URL(database.getUrl() + "/" + document.getDocumentId() + "/" + attachment.getFilename());
    	Fetcher fetcher = getFetcher();
    	byte[] content = fetcher.fetchBytes(attachmentURL);
		attachment.setContent(content);
		store.updateAttachment(database, document, attachment);
		
		return false; 
	}
	
	public Map<String, Object> getDatabaseInformation() {
		return null;  // TODO
	}
	
	public float getProgress() {
		return 0;  // TODO
	}
	public float getProgressDocuments() {
		return 0;  // TODO
	}
	public float getProgressAttachments() {
		return 0; // TODO
	}
	
	private Fetcher getFetcher() {
		Fetcher fetcher = new Fetcher(username, password);
		return fetcher;
	}
}
