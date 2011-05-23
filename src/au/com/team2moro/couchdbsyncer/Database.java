package au.com.team2moro.couchdbsyncer;

import java.net.URL;

public class Database {
	private URL url;
	private String name, dbName;
	private long sequenceId, docDelCount, docUpdateSeq;
	private long databaseId;
	
	public Database(String name, URL url) {
		this.name = name;
		this.url = url;
		this.sequenceId = 0;
		this.docDelCount = 0;
		this.docUpdateSeq = 0;
		this.databaseId = -1;
	}

	public String toString() {
		return name;
	}
	
	public long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(long databaseId) {
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

	public long getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public long getDocDelCount() {
		return docDelCount;
	}

	public void setDocDelCount(long docDelCount) {
		this.docDelCount = docDelCount;
	}

	public long getDocUpdateSeq() {
		return docUpdateSeq;
	}

	public void setDocUpdateSeq(long docUpdateSeq) {
		this.docUpdateSeq = docUpdateSeq;
	}

	
}
