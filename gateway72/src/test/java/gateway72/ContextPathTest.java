package gateway72;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import gateway72.configuration.Configuration;
import gateway72.configuration.ConfigurationReader;

public class ContextPathTest {

    @Test
    public void testContextPath() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Testserver1.contextPath = "/*";
        threadPool.execute(new Testserver1());
        System.setProperty("GATEWAY_CONTEXT_PATH", "/api");
        String config = "# Every URL starts with '/api'."
                + "\nGET /public -> t1 -> /public"
                + "\nservices:\nt1 -> http://localhost:6066";
        Configuration model = new ConfigurationReader().parse(config);
        threadPool.execute(() -> Gateway72App.start(Gateway72Config.PORT, model));

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse res;
        HttpGet get;

        // Test with '/api' reaches the proxy servlet
        get = new HttpGet("http://localhost:4100/api/public");
        res = client.execute(get);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        System.out.println("[testcase] response of '/' is 200: " + getResponse(res));

        // Test without '/api' does not reach the proxy servlet and reaches nothing.
        get = new HttpGet("http://localhost:4100/public");
        res = client.execute(get);
        Assert.assertEquals(404, res.getStatusLine().getStatusCode());

        threadPool.shutdown();
    }
    
    private String getResponse(CloseableHttpResponse res) throws UnsupportedOperationException, IOException {
        try (BufferedInputStream s = new BufferedInputStream(res.getEntity().getContent())) {
            final int l = (int) res.getEntity().getContentLength();
            byte[] b = new byte[l];
            s.read(b, 0, l);
            return new String(b);
        }
    }
}
