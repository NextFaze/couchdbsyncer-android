package au.com.team2moro.couchdbsyncer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class BulkFetcher extends Fetcher {
	private List<Document> documents;

	public BulkFetcher() {
		this.documents = new ArrayList<Document>();
	}

	public void addDocument(Document document) {
		documents.add(document);
	}
	
	public List<Document> fetchDocuments(URL serverURL) throws IOException {
	    URL fetchURL = new URL(serverURL + "/_all_docs?include_docs=true");
	    Map<String, Object> data = (Map<String, Object>) fetchJSON(fetchURL, getHttpBody());
	    List<Map> rows = (List<Map>) data.get("rows");
	    List<Document> results = new ArrayList<Document>();
	    
	    for(Map<String, Object> row : rows) {
	    	Map<String, Object> object = (Map<String, Object>) row.get("doc");
	    	String documentId = (String) row.get("id");
	    	Document document = new Document(documentId);
	    	document.setObject(object);
	    	results.add(document);
	    }
	    
	    return results;	    
	}
	
	private String getHttpBody() {
		List<String> keys = new ArrayList<String>();
		for(Document document : documents) {
			keys.add(document.getDocumentId());
		}
		Gson gson = new Gson();
		return gson.toJson(keys);
	}
}
