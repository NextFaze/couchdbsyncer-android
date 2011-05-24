package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import android.util.Log;

public class Syncer extends Observable {
	private static final String TAG = "Syncer";
	
    private int countReq, countReqDoc, countReqAtt, countFin, countFinAtt, countFinDoc, countHttp;
    private DatabaseStore store;
    private Database database;
    private Credentials credentials;
    private boolean aborted;
    private BulkFetcher bulkFetcher;
    private int documentsPerRequest;
    private boolean changed, finished;

    /**
     * Create a new Syncer object that will sync the given Database in the given DatabaseStore
     * @param store The DatabaseStore to sync
     * @param database The Database to sync
     */
	public Syncer(DatabaseStore store, Database database) {
		this.store = store;
		this.database = database;
		this.aborted = false;
	}
	
    /**
     * Create a new Syncer object that will sync the given Database in the given DatabaseStore, using the given credentials for http authentication.
     * @param store The DatabaseStore to sync
     * @param database The Database to sync
     */
	public Syncer(DatabaseStore store, Database database, Credentials credentials) {
		this(store, database);
		this.credentials = credentials;
	}
	
	/**
	 * Runs an update against the remote CouchDB database.
	 * @throws IOException
	 */
	public void update() throws IOException {
		update(null);
	}
	
	/**
	 * Runs an update against the remote CouchDB database, using the given download policy.
	 * @param policy
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void update(DownloadPolicy policy) throws IOException {
		finished = false;
		
		// fetch list of changes
        // example changes data:
        /*
         {"results":[
         {"seq":2,"id":"test2","changes":[{"rev":"1-e18422e6a82d0f2157d74b5dcf457997"}]}
         ],
         "last_seq":2}
         */
		Log.d(TAG, "updating store");
		
		long sid = store.getSequenceId(database);
		URL url = new URL(database.getUrl() + "/_changes?since=" + sid);
    	Fetcher fetcher = getFetcher();
		Map<String, Object> changes = fetcher.fetchJSON(url);
		if(changes == null) {
			// no changes
			Log.d(TAG, "no change list found");
			return;
		}
		
		Collection<Map<String, Object>> results = (Collection<Map<String, Object>>) changes.get("results");
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

        // perform any outstanding bulk document fetches
        performBulkFetch(policy);
        
        // download unfetched (stale) attachments
        // these might have failed to download from a previous sync
        List<Attachment> unfetched = store.getStaleAttachments(database);
        Log.d(TAG, unfetched.size() + " unfetched attachments");
        for(Attachment attachment : unfetched) {
        	Document document = store.getDocument(database, attachment.getDocId());
        	updateAttachment(document, attachment, policy);
        }
        
        finished = true;
        changed = true;
        notifyObservers();
	}
	
	private void updateDocument(Document document, int sequenceId, DownloadPolicy policy) throws IOException {
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

    	// need to fetch document
    	// add to bulk fetch list
    	// (we also add deleted documents to bulk fetcher to retain ordering. it returns documents ordered by sequence id)
    	BulkFetcher bfetcher = getBulkFetcher();
    	bfetcher.addDocument(document, sequenceId);
    	countReq++;
    	countReqDoc++;
    	if(documentsPerRequest > 0 && bfetcher.getFetchCount() == documentsPerRequest) {
    		performBulkFetch(policy);
    		bulkFetcher = null;  // reset bulk fetcher
    	}
	}

	private void updateAttachment(Document document, Attachment attachment, DownloadPolicy policy) throws IOException {
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
    	URL attachmentURL = new URL(database.getUrl() + "/" + document.getDocId() + "/" + attachment.getFilename());
    	Fetcher fetcher = getFetcher();
    	byte[] content = fetcher.fetchBytes(attachmentURL);
		countHttp++;
		countReq++;
		countReqAtt++;
		attachment.setContent(content);
		attachment.setLength(content.length);
		store.updateAttachment(database, document, attachment);
		countFin++;
		countFinAtt++;
		changed = true;
    	notifyObservers();

		return;
	}
	
	public Map<String, Object> getDatabaseInformation() {
		return null;  // TODO
	}

	/**
	 * Returns the overall download progress as a number between 0 and 1
	 * @return the overall download progress
	 */
	public double getProgress() {
		return countReq == 0 ? 0 : (double)countFin / countReq;
	}
	
	/**
	 * Returns the download progress for documents as a number between 0 and 1
	 * @return the download progress for documents
	 */
	public double getProgressDocuments() {
		return countReqDoc == 0 ? 0 : (double)countFinDoc / countReqDoc;
	}
	
	/**
	 * Returns the download progress for attachments as a number between 0 and 1
	 * @return the download progress for attachments
	 */
	public double getProgressAttachments() {
		return countReqAtt == 0 ? 0 : (double)countFinAtt / countReqAtt;
	}
	
	private void performBulkFetch(DownloadPolicy policy) throws IOException {
        BulkFetcher bfetcher = getBulkFetcher();
        
        if(bfetcher.getDocumentCount() == 0) return;

        // perform bulk document fetch
        List<SequencedDocument> documents = bfetcher.fetchDocuments(database.getUrl());
        countHttp++;

        for(SequencedDocument sdoc : documents) {
        	Document document = sdoc.getDocument();
        	// update document in the data store
        	store.updateDocument(database, document, sdoc.getSequenceId());
        	countFin++;
        	countFinDoc++;
    		changed = true;
        	notifyObservers();
        	
        	if(document.isDeleted()) continue;
        	
            // download unfetched (stale) attachments
            Collection<Attachment> attachments = document.getAttachments();
           	for(Attachment attachment : attachments) {
           		Log.d(TAG, "attachment: " + attachment + ", stale: " + attachment.isStale());
           		if(!attachment.isStale()) continue;

           		// stale attachment, download it
           		updateAttachment(document, attachment, policy);
            }
        }
	}
	
	private Fetcher getFetcher() {
		Fetcher fetcher = new Fetcher(credentials);
		return fetcher;
	}

	private BulkFetcher getBulkFetcher() {
		if(bulkFetcher == null) {
			bulkFetcher = new BulkFetcher(credentials);
		}
		return bulkFetcher;
	}

	public boolean hasChanged() {
		return changed;
	}
	
	protected void clearChanged() {
		changed = false;
	}
	
	public boolean isFinished() {
		return finished;
	}
}
