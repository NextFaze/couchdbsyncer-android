package au.com.team2moro.couchdbsyncer;

public class Attachment {

	private byte[] content;
	String filename;
	int revpos;
	
	public Attachment() {
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getRevpos() {
		return revpos;
	}

	public void setRevpos(int revpos) {
		this.revpos = revpos;
	}
}
