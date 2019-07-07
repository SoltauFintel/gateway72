package gateway72;

import java.io.IOException;
import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class RestCaller {
    private static CloseableHttpClient httpClient = null;

    public String post(String url, String body, String mediaType) throws IOException {
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(body);
        entity.setContentType(mediaType);
        post.setEntity(entity);
        return request(url, post);
    }

    private String request(String url, HttpRequestBase request) throws IOException {
        init(request);
        if (httpClient == null) {
            httpClient = createAcceptSelfSignedCertificateClient();
        }
        URI uri = URI.create(url);
        HttpResponse response = httpClient.execute(
                new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()),
                request,
                HttpClientContext.create());

        // Error handling
        final int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            throw new RuntimeException("Status " + status + ". " + EntityUtils.toString(response.getEntity()));
        }
        // Return response as String or JSON.
        return EntityUtils.toString(response.getEntity());
    }

    // https://memorynotfound.com/ignore-certificate-errors-apache-httpclient/
    private CloseableHttpClient createAcceptSelfSignedCertificateClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
            SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
            return HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void init(HttpRequestBase request) {
    }
}
