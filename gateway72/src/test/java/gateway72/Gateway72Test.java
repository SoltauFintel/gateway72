package gateway72;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import gateway72.configuration.Configuration;
import gateway72.configuration.Endpoint;
import gateway72.configuration.RouteDefinition;
import gateway72.configuration.Service;
import gateway72.configuration.Verb;

public class Gateway72Test {

    @Test
    public void test() throws Exception {
        // Start the target server in an extra thread.
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(new Testserver1());

        Configuration model = new Configuration();
        Service service = new Service();
        service.setName("mouse");
        service.setUrl("http://localhost:6066");
        model.getServices().add(service);

        model.getRoutes().add(createRoute(Verb.GET, "/mouse-service/foo", "mouse", "/rest/mouse/foo"));
        model.getRoutes().add(createRoute(Verb.POST, "/mouse-service/foo", "mouse", "/rest/mouse/foo"));
        
        // Starts the proxy server
        threadPool.execute(() -> Gateway72App.start(Gateway72Config.PORT, model));
        
        // Make calls to the proxy server
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://localhost:4100/mouse-service/foo?m=post-test");
        post.setEntity(new StringEntity("bla blub blabla\r\nThe End."));
        post.setHeader("X-gateway72", "some-info");
        CloseableHttpResponse res = client.execute(post);
        
        // Check for the right response
        System.out.println("res.status: " + res.getStatusLine().getStatusCode());
        try (BufferedInputStream s = new BufferedInputStream(res.getEntity().getContent())) {
            final int l = (int) res.getEntity().getContentLength();
            byte[] b = new byte[l];
            s.read(b, 0, l);
            System.out.println("res.body  : " + new String(b));
        }
        
        threadPool.shutdown();
    }

    private RouteDefinition createRoute(Verb verb, String url, String serviceName, String endpointUrl) {
        RouteDefinition route = new RouteDefinition();
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        route.setRoles(roles);
        route.setUrl(url);
        route.setVerb(verb);
        
        Endpoint endpoint = new Endpoint();
        endpoint.setServiceName(serviceName);
        endpoint.setUrl(endpointUrl);
        route.setEndpoint(endpoint);
        return route;
    }
}
