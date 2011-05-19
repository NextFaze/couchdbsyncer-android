package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import android.util.Log;

public class Syncer {
	public static final String TAG = "Syncer";
	
    private int countReq;
    private Database database;
    
	public Syncer(Database database) {
		this.database = database;
	}
	
	public boolean updateStore(Store store) throws IOException {
		return updateStore(store, null);
	}
	
	public boolean updateStore(Store store, StoreDownloadPolicy policy) throws IOException {
		
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

        	String documentId = (String) change.get("id");
        	//String revision = (String) change.get("revision");
        	Boolean deleted = (Boolean) change.get("deleted");
        	
        	Document document = new Document(documentId);
        	document.setDeleted(deleted == null ? false : deleted.booleanValue());
        	//document.setRevision(revision);
        	//doc.setSequenceId(change.get("seq"));
        	
        	updateStoreDocument(store, document, policy);
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
	
	public boolean updateStoreDocument(Store store, Document document) throws IOException {
		return updateStoreDocument(store, document, null);
	}
	
	public boolean updateStoreDocument(Store store, Document document, StoreDownloadPolicy policy) throws IOException {
    	boolean download = !document.isDesignDocument();
    	int priority = Thread.NORM_PRIORITY;
    	Log.d(TAG, "update store document: " + document.getDocumentId());
    	
    	if(policy != null) {
    		download = policy.getDocumentDownload(document);
    		if(download) priority = policy.getDocumentPriority(document);
    	}
    	
    	if(!download) return false;
    	
    	/*
    	   if(aborted) {
    	        LOG(@"syncer is aborted, returning");
    	        return;
    	    }
    	    */

    	if(document.isDeleted()) {
    		// document deleted 
    		store.updateDocument(document);
    	}
    	else {
    		// need to fetch document
    		// TODO: use BulkFetcher
    		
        	Fetcher fetcher = getFetcher();
    		URL documentURL = new URL(database.getUrl() + "/" + document.getDocumentId());
    		Map<String, Object> object = fetcher.fetchJSON(documentURL);
    		document.setObject(object);
    		store.updateDocument(document);
    	}
 
    	return false; // TODO
	}
	
	public boolean updateStoreAttachment(Store store, Document document, Attachment attachment) throws IOException {
		return updateStoreAttachment(store, document, attachment, null);
	}

	public boolean updateStoreAttachment(Store store, Document document, Attachment attachment, StoreDownloadPolicy policy) throws IOException {
    	boolean download = true;
    	int priority = 4;  //  lower priority than normal
    	if(policy != null) {
    		download = policy.getAttachmentDownload(attachment);
    		if(download) priority = policy.getAttachmentPriority(attachment);
    	}
    	
    	if(!download) return false;
    	
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
		store.updateAttachment(document, attachment);
		
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
		Fetcher fetcher = new Fetcher(database.getUsername(), database.getPassword());
		return fetcher;
	}
}
