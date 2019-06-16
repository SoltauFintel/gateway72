package gateway72.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gateway72.Gateway72Logger;

public class ConfigurationService {

    public static void dump(Configuration model) {
        List<String> urls = new ArrayList<>();
        // TODO Verben zusammenfassen
        for (RouteDefinition route : model.getRoutes()) {
            urls.add(route.getUrl() + "  " + route.getVerb().name());
        }
        Collections.sort(urls);
        Gateway72Logger.instance.dump("All routes:");
        for (String url : urls) {
            Gateway72Logger.instance.dump(" - " + url);
        }
        
        List<String> services = new ArrayList<>();
        for (Service service : model.getServices()) {
            services.add(service.getName() + " -> " + service.getUrl());
        }
        Collections.sort(services);
        Gateway72Logger.instance.dump("All services:");
        // TODO output as table
        for (String name : services) {
            Gateway72Logger.instance.dump(" - " + name);
        }
    }
}
