package gateway72.doc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gateway72.Gateway72App;
import gateway72.Gateway72Logger;
import gateway72.configuration.Configuration;
import gateway72.configuration.RouteDefinition;
import gateway72.configuration.Service;

public class DocServlet extends HttpServlet {
    private final String contextPath;
    private final Configuration configuration;
    
    public DocServlet(String contextPath, Configuration configuration) {
        this.contextPath = contextPath;
        this.configuration = configuration;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().equals(contextPath + "/doc")) {
            handleDoc(resp);
        } else {
            super.doGet(req, resp);
        }
    }

    private void handleDoc(HttpServletResponse resp) throws IOException {
        Gateway72Logger.instance.log("doc");
        resp.setContentType("text/html");
        PrintWriter w = resp.getWriter();
        w.println("<html><head><title>API-Gateway " + Gateway72App.VERSION + " Doku</title>"
                + "<style>* { font-family: 'Arial'; }\r\np.roles { color: red; }</style></head><body>");
        w.println("<h1>API-Gateway Dokumentation</h1>");
        w.println("<h2>Basis-URL</h2>" + contextPath);
        w.println("<h2>Routes</h2>");
        configuration.getRoutes().stream().sorted((a,b) -> a.getUrl().compareTo(b.getUrl())).forEach(printRoute(w));
        w.println("<p style=\"margin-top: 2cm;\">Diese Dokumentation kann mit Env Var GATEWAY_DOC=0 deaktiviert werden.</p>");
        w.println("</body></html>");
        w.close();
    }

    private Consumer<? super RouteDefinition> printRoute(PrintWriter w) {
        return route -> {
            w.println("<h3>" + route.getUrl() + " (" + route.getVerb().name() + ")</h3><p>"
                    + route.getEndpoint().getServiceUrl() + route.getEndpoint().getUrl() + "</p>");
            if (route.getRoles() != null && !route.getRoles().isEmpty()) {
                w.println("<p class=\"roles\">" + route.getRoles().stream().collect(Collectors.joining(", ")) + "</p>");
            }
            String docURL = getServiceDocURL(route.getEndpoint().getServiceName());
            if (docURL != null && !docURL.trim().isEmpty()) {
                w.println("<p class=\"doc\"><a href=\"" + docURL + "\" target=\"_blank\">"
                        + route.getEndpoint().getServiceName() + " Dokumentation</a></p>");
            }
        };
    }
    
    private String getServiceDocURL(String serviceName) {
        for (Service sv : configuration.getServices()) {
            if (sv.getName().equals(serviceName)) {
                return sv.getDoc();
            }
        }
        return null; // should never happen
    }
}
