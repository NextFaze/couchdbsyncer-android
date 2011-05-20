package au.com.team2moro.couchdbsyncer;

import java.net.URL;

public class Database {
	private URL url;
	private String name, dbName;
	private int sequenceId, docDelCount, docUpdateSeq, databaseId;
	
	public Database(String name, URL url) {
		this.name = name;
		this.url = url;
		this.sequenceId = 0;
		this.docDelCount = 0;
		this.docUpdateSeq = 0;
		this.databaseId = -1;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}
	
	public void setUrl(URL url) {
		this.url = url;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public int getDocDelCount() {
		return docDelCount;
	}

	public void setDocDelCount(int docDelCount) {
		this.docDelCount = docDelCount;
	}

	public int getDocUpdateSeq() {
		return docUpdateSeq;
	}

	public void setDocUpdateSeq(int docUpdateSeq) {
		this.docUpdateSeq = docUpdateSeq;
	}

	
}
