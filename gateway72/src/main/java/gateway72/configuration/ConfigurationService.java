package gateway72.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import gateway72.Gateway72Logger;

public class ConfigurationService {

    public static void dump(Configuration model) {
        List<RouteWithVerbs> routes = new ArrayList<>();
        for (RouteDefinition route : model.getRoutes()) {
            String verb = route.getVerb().name();
            boolean found = false;
            for (RouteWithVerbs r : routes) {
                if (r.getUrl().equals(route.getUrl())) {
                    r.addVerb(verb);
                    found = true;
                    break;
                }
            }
            if (!found) {
                routes.add(new RouteWithVerbs(route.getUrl(), verb));
            }
        }
        routes.sort((a, b) -> a.getUrl().compareTo(b.getUrl()));
        Gateway72Logger.instance.dump("All routes:");
        for (RouteWithVerbs r : routes) {
            Gateway72Logger.instance.dump(" - " + r);
        }
        
        if (!model.getServices().isEmpty()) {
            List<Service> services = new ArrayList<>(model.getServices());
            services.sort((a, b) -> a.getName().compareTo(b.getName()));
            int max = services.stream().map(s -> s.getName().length()).max(Integer::compareTo).orElse(0);
            Gateway72Logger.instance.dump("All services:");
            for (Service service : services) {
                Gateway72Logger.instance.dump(" - " + pad(service.getName(), max) + " -> " + service.getUrl());
            }
        }
    }
    
    private static String pad(String text, int len) {
        while (text.length() < len) {
            text += " ";
        }
        return text;
    }
    
    private static class RouteWithVerbs {
        private String url;
        private final Set<String> verbs = new TreeSet<>();

        public RouteWithVerbs(String url, String verb) {
            this.url = url;
            verbs.add(verb);
        }

        public String getUrl() {
            return url;
        }
        
        public void addVerb(String verb) {
            verbs.add(verb);
        }

        public String toString() {
            return url + "  " + verbs.stream().collect(Collectors.joining(", "));
        }
    }
}
