package au.com.team2moro.couchdbsyncer;

public interface StoreDownloadPolicy {

	public boolean getAttachmentDownload(Attachment attachment);
	public boolean getDocumentDownload(Document document);
	public int getAttachmentPriority(Attachment attachment);
	public int getDocumentPriority(Document document);

	public String defaultStoreDatabasePath();
}
