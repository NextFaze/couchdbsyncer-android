package au.com.team2moro.couchdbsyncer;

public class Credentials {
	private String username, password;
	
	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
}
