package gateway72;

import gateway72.auth.AuthorizationService;
import gateway72.configuration.Configuration;
import gateway72.configuration.ConfigurationReader;
import gateway72.configuration.ConfigurationService;
import gateway72.configuration.RoutingService;
import gateway72.jetty.GatewayErrorHandler;
import gateway72.jetty.Server;

/**
 * Simple API Gateway
 */
public class Gateway72App {
    public static final String VERSION = "1.0.4";
    private static Server server;
    
    public static void main(String[] args) {
        Gateway72Logger.instance.boot("");
        Gateway72Logger.instance.boot("API Gateway");

        // Read all files from configuration directory
        Configuration configuration = new ConfigurationReader().readDir(Gateway72Config.getDir());
        int port = Gateway72Config.getPort();
        
        // Start API Gateway server
        start(port, configuration);
        
        ConfigurationService.dump(configuration);
        Gateway72Logger.instance.boot("");
        Gateway72Logger.instance.boot("Gateway72 version " + VERSION + " ready on port " + port);
        Gateway72Logger.instance.boot("");
        
        server.runForever();
    }

    public static void start(int port, Configuration configuration) {
        String contextPath = Gateway72Config.getContextPath();
        Gateway72Logger.instance.boot("Server context path: '" + contextPath + "'");
        
        server = new Server(port, contextPath);
        server.addServlet(new Gateway72Servlet(new RoutingService(configuration), new AuthorizationService()));
        server.setErrorHandler(new GatewayErrorHandler());
        server.start();
    }
}
