package gateway72.configuration;

import java.util.List;

public class RouteDefinition {
    // HTTP method for incoming and outgoing URL
    private Verb verb = Verb.GET;
    // incoming URL
    private String url;
    // ... protected by user roles
    private List<String> roles;
    // outgoing URL
    private Endpoint endpoint;

    public Verb getVerb() {
        return verb;
    }

    public void setVerb(Verb verb) {
        this.verb = verb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }
}
