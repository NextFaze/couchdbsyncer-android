package au.com.team2moro.couchdbsyncer;

import java.util.Map;
import java.util.Set;

public class Document {

	private String revision, parentId, type, tags, documentId;
	private boolean deleted;
	private String content;
	private Map<String, Object> object;
	private Set<Attachment> attachments;

	public Document(String documentId) {
		this.documentId = documentId;
	}

	public boolean isDesignDocument() {
		return documentId.startsWith("_design/") ? true : false;
	}
	
	// accessors 
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, Object> getObject() {
		return object;
	}

	public void setObject(Map<String, Object> object) {
		this.object = object;
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

	public String getDocumentId() {
		return documentId;
	}

	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}
	
}
