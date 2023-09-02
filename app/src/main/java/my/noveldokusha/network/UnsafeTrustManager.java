package my.noveldokusha.network;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class UnsafeTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // No action needed
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // No action needed
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
