package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.google.gson.Gson;

public class BulkFetcher extends Fetcher {
	public static final String TAG = "BulkFetcher";
	private List<Document> documents;  // documents to fetch
	private List<Document> deleted;    // deleted documents (returned in results)
	private Map<String, Integer> sequenceMap;  // map of docId -> sequence id
	
	public BulkFetcher() {
		this.documents = new ArrayList<Document>();
		this.deleted = new ArrayList<Document>();
		this.sequenceMap = new HashMap<String, Integer>();
	}
	public BulkFetcher(String username, String password) {
		super(username, password);
		this.documents = new ArrayList<Document>();
		this.deleted = new ArrayList<Document>();
		this.sequenceMap = new HashMap<String, Integer>();
	}
	
	public void addDocument(Document document, int sequenceId) {
		if(document.isDeleted()) {
			deleted.add(document);
		} else {
			documents.add(document);
		}
		sequenceMap.put(document.getDocId(), sequenceId);
	}
	
	/**
	 * @return the number of documents to be fetched (including deleted documents)
	 */
	public int getDocumentCount() {
		return documents.size() + deleted.size();
	}

	/**
	 * @return the number of documents to be fetched (excluding deleted documents)
	 */
	public int getFetchCount() {
		return documents.size();
	}

	@SuppressWarnings("unchecked")
	public List<SequencedDocument> fetchDocuments(URL serverURL) throws IOException {
	    URL fetchURL = new URL(serverURL + "/_all_docs?include_docs=true");
	    Map<String, Object> data = (Map<String, Object>) fetchJSON(fetchURL, getHttpBody());
	    List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
	    List<SequencedDocument> results = new ArrayList<SequencedDocument>();
	    
	    for(Map<String, Object> row : rows) {
	    	Map<String, Object> object = (Map<String, Object>) row.get("doc");
	    	String docId = (String) row.get("id");
	    	Document document = new Document(docId);
	    	document.setContent(object, true);  // populate attachments

	    	addResult(results, document);
	    }
	    
	    for(Document deldoc : deleted) {
	    	addResult(results, deldoc);
	    }
	    
	    // sort documents by sequence id number (ascending)
	    Collections.sort(results);
	    return results;
	}
	
	// add the given document to the list of results
	private void addResult(List<SequencedDocument> results, Document document) {
    	Integer seqId = sequenceMap.get(document.getDocId());
    	if(seqId == null) {
    		// we should have a sequence id for every document fetched
    		Log.d(TAG, "internal error: document " + document.getDocId() + " fetched with no sequence id");
    		return;
    	}
    	SequencedDocument sdoc = new SequencedDocument(document, seqId);
    	results.add(sdoc);
	}
	
	private String getHttpBody() {
		List<String> keys = new ArrayList<String>();
		Map<String, Object> body = new HashMap<String, Object>();
		
		for(Document document : documents) {
			keys.add(document.getDocId());
		}
		body.put("keys", keys);
		
		Gson gson = new Gson();
		return gson.toJson(body);
	}
	
}
