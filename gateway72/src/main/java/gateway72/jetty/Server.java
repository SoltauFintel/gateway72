package gateway72.jetty;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Server {
    private org.eclipse.jetty.server.Server server;
    private ServletContextHandler context;
    
    public Server(int port, String contextPath) {
        server = new org.eclipse.jetty.server.Server(port);
        int options = ServletContextHandler.SESSIONS;
        context = new ServletContextHandler(server, contextPath, options);
    }

    public void addServlet(HttpServlet servlet) {
        context.addServlet(new ServletHolder(servlet), "/*");
    }
    
    public void setErrorHandler(ErrorHandler errorHandler) {
        context.setErrorHandler(errorHandler);
    }
    
    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void runForever() {
        try {
            server.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
