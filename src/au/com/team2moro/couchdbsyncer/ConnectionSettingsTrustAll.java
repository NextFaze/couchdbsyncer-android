package au.com.team2moro.couchdbsyncer;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConnectionSettingsTrustAll {
	
	// always verify the host - dont check for certificate
	final static HostnameVerifier trustAllHostnameVerifier = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	/**
	 * Hostname verifier that trusts all hostnames
	 * @return
	 */
	public static HostnameVerifier getHostnameVerifier() {
		return trustAllHostnameVerifier;
	}

	/**
	 * Trust every server - dont check for any certificate
	 */
	public static SSLSocketFactory getSocketFactory() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[] {};
					}

					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
					}
				}
		};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			return sc.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
