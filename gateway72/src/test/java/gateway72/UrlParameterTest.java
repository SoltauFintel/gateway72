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

public class UrlParameterTest {
    
    @Test
    public void testUrlParameters() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Testserver1.contextPath = "/*";
        threadPool.execute(new Testserver1());
        String config = "GET /person/:id -> t1 -> /person/:id"
                + "\nservices:"
                + "\n t1 -> http://localhost:6066";
        Configuration model = new ConfigurationReader().parse(config);
        threadPool.execute(() -> Gateway72App.start(Gateway72Config.PORT, model));

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse res;
        HttpGet get;
        
        // Test 1
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/person/10");
        res = client.execute(get);
        String r = getResponse(res);
        System.out.println(r);
        Assert.assertEquals("Marcus", r);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        
        // Test 2
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/person/2");
        res = client.execute(get);
        r = getResponse(res);
        System.out.println(r);
        Assert.assertEquals("Christian", r);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        
        // Test 3: unknown person
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/person/504");
        res = client.execute(get);
        r = getResponse(res);
        System.out.println(res.getStatusLine().getStatusCode() + " -> " + r);
        Assert.assertEquals(404, res.getStatusLine().getStatusCode());
        
        // Test 4: invalid person number
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/person/quatsch");
        res = client.execute(get);
        r = getResponse(res);
        System.out.println(res.getStatusLine().getStatusCode() + " -> " + r);
        Assert.assertEquals(500, res.getStatusLine().getStatusCode());

        // Test 5: wrong URL
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/person/quatsch/mit-sosse");
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
