package gateway72;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import gateway72.configuration.Configuration;
import gateway72.configuration.ConfigurationReader;
import gateway72.configuration.Route;
import gateway72.configuration.RouteException;
import gateway72.configuration.RoutingService;

/**
 * ConfigurationReader und RoutingService testen
 */
public class RoutingServiceTest {

    @Test
    public void get() {
        String config = "routes:"
                + "\nGET /abc -> zap->/def"
                + "\nservices:"
                + "\nzap -> http://localhost:6066";
        Configuration model = new ConfigurationReader().parse(config);
        RoutingService sv = new RoutingService(model);
        
        Assert.assertEquals(1, model.getRoutes().size());
        Assert.assertEquals(1, model.getServices().size());
        Assert.assertEquals("http://localhost:6066/def", buildURL("GET", "/abc", sv));
    }

    @Test
    public void post() {
        String config = "POST /abc -> http://localhost:6066/def";
        Configuration model = new ConfigurationReader().parse(config);
        RoutingService sv = new RoutingService(model);
        
        Assert.assertEquals(1, model.getRoutes().size());
        Assert.assertEquals(0, model.getServices().size());
        Assert.assertEquals("http://localhost:6066/def", buildURL("POST", "/abc", sv));
    }

    @Test
    public void put() {
        String config = "put /abc -> http://localhost:6066/def";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Assert.assertEquals("http://localhost:6066/def", buildURL("PUT", "/abc", sv));
    }

    @Test
    public void delete() {
        String config = "DELETE /abc -> http://localhost:6066/def/g";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Assert.assertEquals("http://localhost:6066/def/g", buildURL("delete", "/abc", sv));
    }

    @Test
    public void get_role() {
        String config = " # Rolle USER erforderlich \n"
                + "GET\t/abc\t->\ts1\t->\t/def\t{USER, OFFERS_ADMIN}"
                + "\n\n\tservices:\t\n\t\ts1\t -> \thttp://localhost:6080 \t\r\n\n";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Optional<Route> route = sv.findRoute("GET", "/abc");
        Assert.assertEquals("http://localhost:6080/def", buildURL("GET", "/abc", sv));
        Assert.assertEquals(2, route.get().getRoles().size());
        Assert.assertTrue(route.get().getRoles().contains("USER"));
        Assert.assertTrue(route.get().getRoles().contains("OFFERS_ADMIN"));
    }

    @Test
    public void post_wildcardAtEnd() {
        String config = "POST /log/* -> http://logservice/log/gateway/*";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Assert.assertEquals("http://logservice/log/gateway/pq/4711", buildURL("POST", "/log/pq/4711", sv));
    }

    @Test
    public void put_params() {
        String config = "PUT /person/:id/edit/:p ->   person -> /rest/edit-person/:id/:p"
                + "\nservices:\nperson -> http://server:8017/rest";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Assert.assertEquals("http://server:8017/rest/rest/edit-person/4711/kk", buildURL("PUT", "/person/4711/edit/kk", sv));
    }

    @Test
    public void put_wildcardAsParam() {
        String config = "PUT /person/*/edit ->   http://server/personen/*/bearbeiten";
        RoutingService sv = new RoutingService(new ConfigurationReader().parse(config));
        
        Assert.assertEquals("http://server/personen/4711/bearbeiten", buildURL("PUT", "/person/4711/edit", sv));
    }

    @Test // ok
    public void wildcardAtEndNoError() {
        String config = "PUT /abc/* ->   http://server/def";
        Configuration model = new ConfigurationReader().parse(config);
        new RoutingService(model);
    }

    @Test(expected = RouteException.class)
    public void wildcardAtEndError2() {
        String config = "PUT /abc ->http://server/def/*";
        Configuration model = new ConfigurationReader().parse(config);
        new RoutingService(model);
    }

    @Test // ok
    public void placeholderNotDefinedNoError() {
        String config = "PUT /person/edit/:x ->http://server/personen/bearbeiten";
        Configuration model = new ConfigurationReader().parse(config);
        new RoutingService(model);
    }

    @Test(expected = RouteException.class)
    public void placeholderNotDefined() {
        String config = "PUT /person/edit ->   http://server/personen/:x/bearbeiten";
        Configuration model = new ConfigurationReader().parse(config);
        new RoutingService(model);
    }
    
    private String buildURL(String method, String target, RoutingService sv) {
        Optional<Route> route = sv.findRoute(method, target);
        return route.isPresent() ? route.get().getURL() : "";
    }
}
