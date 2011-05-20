package au.com.team2moro.couchdbsyncer;

public interface Store {
	
	/**
	 * @return the current sequence number of the given database
	 */
	int getSequenceId(Database database);

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
}
