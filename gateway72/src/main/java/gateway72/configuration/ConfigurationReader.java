package gateway72.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import gateway72.Gateway72Logger;

/**
 * Reads all files of directory without sub folders and parses them as Gateway72 configuration files.
 */
public class ConfigurationReader {

    public Configuration readDir(String folder) {
        Configuration model = new Configuration();
        for (File file : getFiles(folder)) { // no sub folders
            
            Configuration m = parse(loadFile(file));
            
            for (Service service : m.getServices()) {
                if (isServiceUnique(model, file, service)) {
                    model.getServices().add(service);
                }
            }
            model.getRoutes().addAll(m.getRoutes());
        }
        checkWhetherRouteUrlsAreUnique(model);
        return model;
    }

    private List<File> getFiles(String folder) {
        File dir = new File(folder);
        if (!dir.isDirectory() && !folder.startsWith("/")) {
            dir = new File("/" + folder);
        }
        if (dir.isDirectory()) {
            Gateway72Logger.instance.boot("Configuration directory: " + dir.getAbsolutePath());
        } else {
            throw new RuntimeException("Configuration directory '" + folder + "' not found!");
        }
        List<File> ret = new ArrayList<>();
        String[] files = dir.list();
        for (String filename : files) {
            File file = new File(dir, filename);
            if (!file.isDirectory()) {
                ret.add(file);
            }
        }
        return ret;
    }

    public Configuration parse(String text) {
        Configuration model = new Configuration();
        int mode = 1; // 1=routes, 2=services
        for (String line: text.replace("\r", "").split("\n")) {
            try {
                String lt = line.trim();
                if (lt.isEmpty() || lt.startsWith("#")) { // ignore
                } else if ("routes:".equals(lt)) {
                    mode = 1;
                } else if ("services:".equals(lt)) {
                    mode = 2;
                } else if (mode == 1) {
                    parseRouteLine(lt, model);
                } else if (mode == 2) {
                    parseServiceLine(lt, model);
                } else {
                    throw new ConfigurationFileWarning("wrong mode? cannot parse line: " + lt);
                }
            } catch (ConfigurationFileWarning e) {
                Gateway72Logger.instance.warn(e.getMessage());
            }
        }
        return model;
    }

    private void parseRouteLine(String lt, Configuration model) throws ConfigurationFileWarning {
        int o = lt.indexOf("{");
        String roles="";
        if (o >= 0 && lt.endsWith("}")) {
            roles = lt.substring(o + 1).replace("}", "");
            lt = lt.substring(0, o).trim();
        }
        o = lt.indexOf("->");
        if (o < 0) {
            throw new ConfigurationFileWarning("arrow missing. cannot parse line: " + lt);
        }
        String li = lt.substring(0, o).trim();
        String re = lt.substring(o + "->".length()).trim();
        o = li.indexOf(" ");
        if (o < 0) {
            o = li.indexOf("\t");
        }
        if (o < 0) {
            throw new ConfigurationFileWarning("space missing. cannot parse line: " + lt);
        }
        String verb = li.substring(0, o);
        String url = li.substring(o + 1).trim();
        o = re.indexOf("->");
        String service = null;
        String path;
        if (o >= 0) {
            service = re.substring(0, o).trim();
            path = re.substring(o + "->".length()).trim();
        } else {
            path = re;
        }
        
        model.getRoutes().add(createRouteDefinition(verb, url, service, path, roles));
    }

    private RouteDefinition createRouteDefinition(String verb, String url, String service, String path, String roles) {
        RouteDefinition route = new RouteDefinition();
        route.setUrl(url);
        route.setVerb(Verb.fromString(verb.toUpperCase()));
        Endpoint endpoint = new Endpoint();
        endpoint.setServiceName(service);
        endpoint.setUrl(path);
        route.setEndpoint(endpoint);
        route.setRoles(new ArrayList<>());
        for (String role : roles.split(",")) {
            String rt = role.trim();
            if (!rt.isEmpty()) {
                route.getRoles().add(rt);
            }
        }
        return route;
    }

    private void parseServiceLine(String lt, Configuration model) throws ConfigurationFileWarning {
        int o = lt.indexOf("->");
        if (o < 0) {
            throw new ConfigurationFileWarning("arrow missing. cannot parse line: " + lt);
        }
        String name = lt.substring(0, o).trim();
        String url = lt.substring(o + "->".length()).trim();
        Service service = new Service();
        service.setName(name);
        service.setUrl(url);
        model.getServices().add(service);
    }

    private String loadFile(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration file: " + file.getAbsolutePath(), e);
        }
    }

    private boolean isServiceUnique(Configuration model, File file, Service service) {
        for (Service s2 : model.getServices()) {
            if (service.getName().equals(s2.getName())) {
                if (!service.getUrl().equals(s2.getUrl())) {
                    throw new RuntimeException("Duplicate service (with different URL) in configuration files!"
                            + "\r\nservice name: " + service.getName() + "\r\n" + file.toString());
                }
                return false;
            }
        }
        return true;
    }

    private void checkWhetherRouteUrlsAreUnique(Configuration model) {
        for (int i = 0; i < model.getRoutes().size(); i++) {
            RouteDefinition ii = model.getRoutes().get(i);
            for (int j = 0; j < i; j++) {
                RouteDefinition jj = model.getRoutes().get(j);
                if (ii.getUrl().equals(jj.getUrl()) && ii.getVerb().equals(jj.getVerb())) {
                    throw new RuntimeException("Duplicate route: " + ii.getVerb().name() + " " + ii.getUrl());
                }
            }
        }
    }
}
