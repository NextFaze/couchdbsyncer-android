package au.com.team2moro.couchdbsyncer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class Document {
	private static final String TAG = "Document";

	private long documentId, databaseId;
	private String revision, parentId, type, docId;
	private boolean deleted;
	private List<String> tags;
	private Map<String, Object> content;
	private Map<String, Attachment> attachments;

	public Document(String docId) {
		this.docId = docId;
		this.attachments = new HashMap<String, Attachment>();
		this.tags = new ArrayList<String>();
	}
	
	/**
	 * @return true if this is a design document, false otherwise
	 */
	public boolean isDesignDocument() {
		return docId.startsWith("_design/") ? true : false;
	}
	
	/**
	 * Fetch a field value from the document
	 * @param key The name of the field to fetch
	 * @return The value of the field
	 */
	public Object get(String key) {
		return content == null ? null : content.get(key);
	}
	public String getString(String key) {
		return (String) get(key);
	}
	
	public Attachment getAttachment(String filename) {
		return attachments.get(filename);
	}
	
	public String toString() {
		return docId;
	}
	
	// accessors 
	
	public long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(long databaseId) {
		this.databaseId = databaseId;
	}

	public long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(long documentId) {
		this.documentId = documentId;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public Map<String, Object> getContent() {
		return content;
	}
	
	public byte[] getContentBytes() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(content);
			oos.close();
			return bos.toByteArray();
		} catch(Exception e) {
			Log.d(TAG, e.toString());
		}
		return null;
	}

	public void setContent(Map<String, Object> content) {
		setContent(content, false);
	}

	@SuppressWarnings("unchecked")
	public void setContent(Map<String, Object> content, boolean populateAttachments) {
		String id = (String) content.get("_id");
		if(!id.equals(getDocId())) {
			// document id mismatch
			throw new IllegalArgumentException("document ID does not match");
		}
		
		this.content = content;

		setRevision(getString("_rev"));
		setType(getString("type"));
		setTags(get("tags"));
		setParentId(getString("parent_id"));

		if(populateAttachments) {
			// populate attachment data from the _attachments hash
			// "_attachments":{"image.jpg":{"content_type":"image/jpeg","revpos":2,"length":13138,"stub":true}}
			Map<String, Map<String, Object>> atts = (Map<String, Map<String, Object>>) get("_attachments");
			if(atts != null) {
				for(String filename : atts.keySet()) {
					Map<String, Object> info = atts.get(filename);
					Attachment attachment = new Attachment(filename);
					attachment.setContentType((String) info.get("content_type"));
					attachment.setRevision((Integer) info.get("revpos"));
					attachment.setLength((Integer) info.get("length"));
					attachment.setDocId(getDocId());
					attachment.setDocumentId(getDocumentId());
					
					attachments.put(filename, attachment);
					//Log.d(TAG, "detected attachment: " + attachment);
				}
			}
		}
	}

	// set content from serialised hash data
	@SuppressWarnings("unchecked")
	protected void setContent(byte[] content) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(content);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Map<String, Object> data = (Map<String, Object>) ois.readObject();
			ois.close();
			
			setContent(data, true);  // populate attachments
		} catch(Exception e) {
			// error deserialising object
			Log.d(TAG, e.toString());
			this.content = null;
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getTags() {
		return tags;
	}
	
	// return tags separated by spaces
	public String getTagsString() {
		StringBuffer sbuf = new StringBuffer();
		for(String tag : tags) {
			sbuf.append(tag);
			sbuf.append(" ");
		}
		return sbuf.toString();
	}

	@SuppressWarnings("unchecked")
	public void setTags(Object tags) {
		if(tags instanceof List) {
			setTags((List<String>)tags);
		} else if(tags instanceof String) {
			setTags((String)tags);
		}
	}

	public void setTags(String tags) {
		String[] tagArray = tags.split("[\\s\\t]+");
		this.tags = Arrays.asList(tagArray);
	}
	
	public void setTags(List<String> tags) {
		this.tags = new ArrayList<String>(tags);
	}

	public String getDocId() {
		return docId;
	}

	public Collection<Attachment> getAttachments() {
		return attachments.values();
	}

	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments.clear();
		for(Attachment attachment : attachments) {
			this.attachments.put(attachment.getFilename(), attachment);
		}
	}
	
}
