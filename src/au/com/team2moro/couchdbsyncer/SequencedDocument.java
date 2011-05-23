package au.com.team2moro.couchdbsyncer;

public class SequencedDocument implements Comparable<SequencedDocument> {
	private Document document;
	private int sequenceId;
	
	public SequencedDocument(Document document, int sequenceId) {
		this.document = document;
		this.sequenceId = sequenceId;
	}
	
	public Document getDocument() {
		return document;
	}
	public int getSequenceId() {
		return sequenceId;
	}

	public int compareTo(SequencedDocument another) {
		int otherSequenceId = another.getSequenceId();
		return (sequenceId < otherSequenceId ? -1 :
				sequenceId == otherSequenceId ? 0 : 1);
	}
	
}
