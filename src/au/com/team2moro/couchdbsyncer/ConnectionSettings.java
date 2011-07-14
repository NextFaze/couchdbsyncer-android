package au.com.team2moro.couchdbsyncer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class ConnectionSettings {
	private String username, password;
	private SSLSocketFactory sslSocketFactory;
	private HostnameVerifier hostnameVerifier;
	
	public ConnectionSettings(String username, String password) {
		this.username = username;
		this.password = password;
	}
	public ConnectionSettings(String username, String password, SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
		this(username, password);
		this.sslSocketFactory = sslSocketFactory;
		this.hostnameVerifier = hostnameVerifier;
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public SSLSocketFactory getSSLSocketFactory() {
		return sslSocketFactory;
	}
	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}
}
