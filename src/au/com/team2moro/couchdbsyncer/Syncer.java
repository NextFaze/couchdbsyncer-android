package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
	
	public void update() throws IOException {
		update(null);
	}
	
	public void update(StoreDownloadPolicy policy) throws IOException {
		
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
			return;
		}
		
		Collection<Map> results = (Collection<Map>) changes.get("results");
		Log.d(TAG, "results: " + results);

        // convert change list to document objects
        for(Map<String, Object> change : results) {
    		//Log.d(TAG, "processing change: " + change);
        	Integer seqId = (Integer) change.get("seq");
        	String documentId = (String) change.get("id");
        	Boolean deleted = (Boolean) change.get("deleted");
        	
        	Document document = new Document(documentId);
        	document.setDeleted(deleted == null ? false : deleted.booleanValue());
        	
        	updateDocument(document, seqId, policy);
        }

		return;
	}
	
	public void updateDocument(Document document, int sequenceId) throws IOException {
		updateDocument(document, sequenceId, null);
	}
	
	public void updateDocument(Document document, int sequenceId, StoreDownloadPolicy policy) throws IOException {
    	boolean download = !document.isDesignDocument();
    	int priority = Thread.NORM_PRIORITY;
    	Log.d(TAG, "update document: " + document.getDocId());
    	
    	if(aborted) {
    		Log.d(TAG, "syncer is aborted, returning");
    		return;
    	}

    	if(policy != null) {
    		download = policy.getDocumentDownload(document);
    		if(download) priority = policy.getDocumentPriority(document);
    	}
    	
    	if(!download) return;  // policy says not to download document

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

        // download unfetched attachments.
        Set<Attachment> attachments = document.getAttachments();
        if(attachments != null) {
        	for(Attachment attachment : attachments) {
        		if(!attachment.isStale()) continue;
        		
        		// stale attachment, download it
        		updateAttachment(document, attachment, policy);
        	}
        }
        
    	return;
	}
	
	public void updateAttachment(Document document, Attachment attachment) throws IOException {
		updateAttachment(document, attachment, null);
	}

	public void updateAttachment(Document document, Attachment attachment, StoreDownloadPolicy policy) throws IOException {
    	boolean download = true;
    	int priority = 4;  //  lower priority than normal

    	if(aborted) {
    		Log.d(TAG, "syncer is aborted, returning");
    		return;
    	}
        if(document.isDeleted()) {
            // document is deleted (we shouldn't get here)
            return;  // do nothing.
        }
    	if(policy != null) {
    		download = policy.getAttachmentDownload(attachment);
    		if(download) priority = policy.getAttachmentPriority(attachment);
    	}
    	
    	if(!download) return;  // policy says not to download attachment    	
        
        // fetch attachment
    	URL attachmentURL = new URL(database.getUrl() + "/" + document.getDocumentId() + "/" + attachment.getFilename());
    	Fetcher fetcher = getFetcher();
    	byte[] content = fetcher.fetchBytes(attachmentURL);
		attachment.setContent(content);
		attachment.setLength(content.length);
		store.updateAttachment(database, document, attachment);
		
		return; 
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
