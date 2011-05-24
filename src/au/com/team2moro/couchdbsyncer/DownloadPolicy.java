package au.com.team2moro.couchdbsyncer;

public interface DownloadPolicy {

	/**
	 * Return the download policy for the given attachment. The default policy is to download all attachments.
	 * @param attachment the attachment to be downloaded
	 * @return true to download the attachment, false otherwise.  If false, metadata for the attachment is still saved and available locally, but the content is not downloaded from the server.
	 */
	public boolean getAttachmentDownload(Attachment attachment);

	/**
	 * Return the download policy for the given document. The default policy is to download all documents except design documents.
	 * @param document the document to be downloaded
	 * @return true to download the document, false otherwise.  If false, no data is stored locally for this document.
	 */
	public boolean getDocumentDownload(Document document);
	
	/**
	 * Return the download thread priority for the given attachment
	 * @param attachment the attachment to be downloaded
	 * @return an integer between Thread.MIN_PRIORITY and Thread.MAX_PRIORITY
	 */
	public int getAttachmentPriority(Attachment attachment);

	/**
	 * Return the download thread priority for the given document
	 * @param document the document to be downloaded
	 * @return an integer between Thread.MIN_PRIORITY and Thread.MAX_PRIORITY
	 */
	public int getDocumentPriority(Document document);

	// TODO: some interface so that shipped databases can be installed.
	//public String defaultStoreDatabasePath();
}
