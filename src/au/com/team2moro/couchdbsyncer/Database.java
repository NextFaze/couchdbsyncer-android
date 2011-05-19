package au.com.team2moro.couchdbsyncer;

import java.net.URL;

public class Database {
	private URL url;
	private String name, dbName;
	private String username, password;
	private int sequenceId, docDelCount, docUpdateSeq;
	
	public Database(String name, URL url) {
		this.name = name;
		this.url = url;
		this.sequenceId = 0;
		this.docDelCount = 0;
		this.docUpdateSeq = 0;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
