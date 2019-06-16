package gateway72.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoutingService {
    private final Configuration model;
    
    public RoutingService(Configuration model) {
        this.model = model;
        for (RouteDefinition route : this.model.getRoutes()) {
            Endpoint endpoint = route.getEndpoint();
            if (endpoint.getServiceUrl() == null || endpoint.getServiceUrl().isEmpty()) {
                if (endpoint.getServiceName() == null || endpoint.getServiceName().isEmpty()) {
                    endpoint.setServiceUrl("");
                } else {
                    endpoint.setServiceUrl(getServiceUrl(endpoint.getServiceName(), model.getServices()));
                }
            }
            if (endpoint.getUrl().endsWith("*") && !route.getUrl().endsWith("*")) {
                throw new RouteException("endpoint.url ends with '*' but route.url ends not with '*'"
                        + "\r\nendpoint.url: " + endpoint.getUrl()
                        + "\r\nroute.url: " + route.getUrl());
            }
            List<String> el = convertRouteToList(endpoint.getUrl()); // SOLL
            List<String> rl = convertRouteToList(route.getUrl());
            for (String i : el) {
                if (i.startsWith(":")) {
                    if (!rl.contains(i)) {
                        throw new RouteException("endpoint.url has parameter '" + i + "' but route.url does not have that parameter"
                                + "\r\nendpoint.url: " + endpoint.getUrl()
                                + "\r\nroute.url: " + route.getUrl());
                    }
                }
            }
        }
    }
    
    private String getServiceUrl(String serviceName, List<Service> services) {
        for (Service service : services) {
            if (service.getName().equals(serviceName)) {
                return service.getUrl();
            }
        }
        throw new RouteException("Service not found: " + serviceName);
    }
    
    public Optional<Route> findRoute(String method, String target) {
        return model.getRoutes().stream()
                .filter(route -> matchPath(target, route.getUrl()) && method.equalsIgnoreCase(route.getVerb().name()))
                .map(route -> new Route(route, target))
                .findFirst();
    }
    
    // from Spark RouteEntry
    private boolean matchPath(String target, String routePath) { // NOSONAR
        if (!routePath.endsWith("*") && ((target.endsWith("/") && !routePath.endsWith("/")) // NOSONAR
                || (routePath.endsWith("/") && !target.endsWith("/")))) {
            // One and not both ends with slash
            return false;
        }
        if (routePath.equals(target)) {
            // Paths are the same
            return true;
        }

        // check params
        List<String> thisPathList = convertRouteToList(routePath);
        List<String> pathList = convertRouteToList(target);

        int thisPathSize = thisPathList.size();
        int pathSize = pathList.size();

        if (thisPathSize == pathSize) {
            for (int i = 0; i < thisPathSize; i++) {
                String thisPathPart = thisPathList.get(i);
                String pathPart = pathList.get(i);

                if ((i == thisPathSize - 1) && (thisPathPart.equals("*") && routePath.endsWith("*"))) {
                    // wildcard match
                    return true;
                }

                if ((!thisPathPart.startsWith(":"))
                        && !thisPathPart.equals(pathPart)
                        && !thisPathPart.equals("*")) {
                    return false;
                }
            }
            // All parts matched
            return true;
        } else {
            // Number of "path parts" not the same
            // check wild card:
            if (routePath.endsWith("*")) {
                if (pathSize == (thisPathSize - 1) && (target.endsWith("/"))) {
                    // Hack for making wildcards work with trailing slash
                    pathList.add("");
                    pathList.add("");
                    pathSize += 2;
                }

                if (thisPathSize < pathSize) {
                    for (int i = 0; i < thisPathSize; i++) {
                        String thisPathPart = thisPathList.get(i);
                        String pathPart = pathList.get(i);
                        if (thisPathPart.equals("*") && (i == thisPathSize - 1) && routePath.endsWith("*")) {
                            // wildcard match
                            return true;
                        }
                        if (!thisPathPart.startsWith(":")
                                && !thisPathPart.equals(pathPart)
                                && !thisPathPart.equals("*")) {
                            return false;
                        }
                    }
                    // All parts matched
                    return true;
                }
                // End check wild card
            }
            return false;
        }
    }

    // from Spark SparkUtils
    public static List<String> convertRouteToList(String route) {
        String[] pathArray = route.split("/");
        List<String> path = new ArrayList<>();
        for (String p : pathArray) {
            if (p.length() > 0) {
                path.add(p);
            }
        }
        return path;
    }
}
