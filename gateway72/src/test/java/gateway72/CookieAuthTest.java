package gateway72;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import gateway72.configuration.Configuration;
import gateway72.configuration.ConfigurationReader;

/**
 * Cookie based authentication and authorization test
 * 
 * @see JwtAuthTest
 */
public class CookieAuthTest {

    @Test
    public void test() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Testserver1.contextPath = "/*";
        threadPool.execute(new Testserver1());
        String config = "GET / -> t1 -> /public"
                + "\nGET /protected -> t1 -> /protected {USER}"
                + "\nPOST /do-login -> t1 -> /do-login {$LOGIN}"
                + "\nGET /do-logout -> t1 -> /do-logout {USER,$LOGOUT}"
                + "\nservices:\nt1 -> http://localhost:6066";
        Configuration model = new ConfigurationReader().parse(config);
        threadPool.execute(() -> Gateway72App.start(Gateway72Config.PORT, model));

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse res;
        HttpGet get;
        
        // Test 1
        // public Test
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100");
        res = client.execute(get);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        System.out.println("[testcase] response of '/' is 200: " + getResponse(res));
        
        // Test 2
        // protected Test
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/protected");
        res = client.execute(get);
        Assert.assertEquals(401, res.getStatusLine().getStatusCode());
        
        // Test 3
        // Der Client kümmert sich nun um den Login.
        System.out.println("---------------------------------------");
        HttpPost post = new HttpPost("http://localhost:4100/do-login");
        HttpEntity entity = new StringEntity("login=Mickey&password=secret");
        post.setEntity(entity);
        res = client.execute(post);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        String resText = getResponse(res);
        System.out.println("[testcase] Test 3 response: " + resText);

        // Test 2 wiederholen
        // protected Test
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/protected");
        res = client.execute(get);
        Assert.assertEquals("Test 2 Wiederholung gescheitert\r\n", 200, res.getStatusLine().getStatusCode());
        System.out.println("[testcase] Test 2 Wh response: " + getResponse(res));
        
        // Logout
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/do-logout");
        res = client.execute(get);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());
        
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
