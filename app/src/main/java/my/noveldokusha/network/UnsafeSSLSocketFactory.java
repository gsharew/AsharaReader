package my.noveldokusha.network;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class UnsafeSSLSocketFactory {
    public static SSLSocketFactory getSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCertificates = new TrustManager[]{new UnsafeTrustManager()};
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLSocketFactory", e);
        }
    }
}
