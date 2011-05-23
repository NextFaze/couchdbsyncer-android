package au.com.team2moro.couchdbsyncer;

import java.util.List;

public interface Store {
	
	/**
	 * @return the current sequence number of the given database
	 */
	long getSequenceId(Database database);

	/**
	 * Get a document from the store by the given document id
	 * @param database The database containing the document
	 * @param docId The document id string
	 * @return the document if found, else null
	 */
	Document getDocument(Database database, String docId);

	/**
	 * Create/Update/Delete a document from the store
	 * @param document The new document
	 */
	void updateDocument(Database database, Document document, int sequenceId);

	/**
	 * Create/Update/Delete an attachment from the store
	 * @param document The document containing the attachment
	 * @param attachment The attachment to add/update/delete
	 */
	void updateAttachment(Database database, Document document, Attachment attachment);
	
	/**
	 * Return a list of unfetched (stale) attachments in the store
	 * @param database The database containing the attachments
	 * @return A list of attachment objects
	 */
	List<Attachment> getStaleAttachments(Database database);
}
