package au.com.team2moro.couchdbsyncer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class Document {
	public static final String TAG = "Document";

	private int documentId, databaseId;
	private String revision, parentId, type, tags, docId;
	private boolean deleted;
	private Map<String, Object> content;
	private Set<Attachment> attachments;

	public Document(String documentId) {
		this.docId = documentId;
		this.attachments = new HashSet<Attachment>();
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
		for(Attachment attachment : attachments) {
			if(attachment.getFilename().equals(filename))
				return attachment;
		}
		return null;  // attachment not found
	}
	
	public String toString() {
		return docId;
	}
	
	// accessors 
	
	public int getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}

	public int getDocumentId() {
		return documentId;
	}

	public void setDocumentId(int documentId) {
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

	public void setContent(Map<String, Object> content, boolean populateAttachments) {
		String id = (String) content.get("_id");
		if(!id.equals(getDocId())) {
			// document id mismatch
			throw new IllegalArgumentException("document ID does not match");
		}
		
		this.content = content;

		setRevision(getString("_rev"));
		setType(getString("type"));
		setTags(getString("tags"));
		setParentId(getString("parent_id"));

		if(populateAttachments) {
			// populate attachment data from the _attachments hash
			// "_attachments":{"image.jpg":{"content_type":"image/jpeg","revpos":2,"length":13138,"stub":true}}
			Map<String, Map<String, Object>> attachments = (Map<String, Map<String, Object>>) get("_attachments");
			for(String filename : attachments.keySet()) {
				Map<String, Object> info = attachments.get(filename);
				Attachment attachment = new Attachment(filename);
				attachment.setContentType((String) info.get("content_type"));
				attachment.setRevision((Integer) info.get("revpos"));
				attachment.setLength((Integer) info.get("length"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setContent(byte[] content) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(content);
			ObjectInputStream ois = new ObjectInputStream(bis);
			Map<String, Object> data = (Map<String, Object>) ois.readObject();
			ois.close();
			
			setContent(data);
		} catch(Exception e) {
			Log.d(TAG, e.toString());
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

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDocId() {
		return docId;
	}

	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}
	
}
