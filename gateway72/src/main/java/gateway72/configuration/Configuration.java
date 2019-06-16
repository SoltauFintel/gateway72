package gateway72.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * API Gateway configuration
 */
public class Configuration {
    private final List<RouteDefinition> routes = new ArrayList<>();
    private final List<Service> services = new ArrayList<>();

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public List<Service> getServices() {
        return services;
    }
}
