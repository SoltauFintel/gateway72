package gateway72.configuration;

import java.util.List;

public class Route {
    private final RouteDefinition route;
    private final String target;
    
    public Route(RouteDefinition route, String target) {
        this.route = route;
        this.target = target;
    }
    
    public List<String> getRoles() {
        return route.getRoles();
    }

    public String getURL() {
        Endpoint endpoint = route.getEndpoint();
        
        // Transfer content of route-URL-placeholders from target to endpoint URL.
        String url = endpoint.getUrl();
        if (route.getUrl().contains(":") || route.getUrl().contains("*")) {
            List<String> ul = RoutingService.convertRouteToList(route.getUrl());
            List<String> tl = RoutingService.convertRouteToList(target);
            if (ul.size() == tl.size()) {
                // placeholders section
                for (int i = 0; i < ul.size(); i++) {
                    String p = ul.get(i);
                    if (p.startsWith(":") || "*".equals(p)) {
                        // replace first
                        int o = url.indexOf(p);
                        if (o >= 0) {
                            url = url.substring(0, o) + tl.get(i) + url.substring(o + p.length());
                        }
                    }
                }
            } else if (route.getUrl().endsWith("*")) { // wildcard at end section
                int i = ul.size() - 1;
                if (i >= 0 && "*".equals(ul.get(i))) {
                    StringBuilder buf = new StringBuilder();
                    for (int j = i; j < tl.size(); j++) {
                        if (j > i) {
                            buf.append("/");
                        }
                        buf.append(tl.get(j));
                    }
                    return endpoint.getServiceUrl() + endpoint.getUrl().replace("*", buf);
                }
            }
        }
        return endpoint.getServiceUrl() + url;
    }
}
