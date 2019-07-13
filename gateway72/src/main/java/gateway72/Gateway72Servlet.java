package gateway72;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gateway72.auth.AuthorizationService;
import gateway72.auth.StatusException;
import gateway72.configuration.Route;
import gateway72.configuration.RoutingService;
import gateway72.cookie.CookieAuthorization;
import gateway72.jetty.CORS;
import gateway72.jetty.ProxyServletBase;

/**
 * API Gateway Servlet
 */
public class Gateway72Servlet extends ProxyServletBase {
    public static final String MODE = "X-gateway-mode";
    private final RoutingService routingService;
    private final AuthorizationService authorizationService;

    public Gateway72Servlet(RoutingService routingService, AuthorizationService authorizationService, CORS cors) {
        super(cors);
        this.routingService = routingService;
        this.authorizationService = authorizationService;
    }

    @Override
    protected String rewriteTarget(HttpServletRequest request) {
        String target = request.getPathInfo();
        String verb = request.getMethod();
        String queryParams = getQueryParams(request);
        try {
            // Is the route defined?
            Optional<Route> route = routingService.findRoute(verb, target);
            if (!route.isPresent()) {
                throw new StatusException(404, "route not defined");
            }

            // check roles
            String authInfo = authorizationService.checkRoles(request, route.get().getRoles());
            
            // rewrite target
            String newURL = route.get().getURL() + queryParams;
            Gateway72Logger.instance.rewrite(true, verb + " " + target + queryParams + "\t (" + authInfo + ")", newURL);
            return newURL; // success
        } catch (StatusException e) {
            Gateway72Logger.instance.rewrite(false, verb + " " + target + queryParams, e.getMessage());
            setMode(request, "" + e.getStatus());
            return null;
        }
    }

    private String getQueryParams(HttpServletRequest request) {
        String queryParams = request.getQueryString();
        if (queryParams == null || queryParams.isEmpty()) {
            return "";
        }
        return "?" + queryParams;
    }
    
    @Override
    protected void onProxyRewriteFailed(HttpServletRequest clientRequest, HttpServletResponse proxyResponse) {
        String mode = getMode(clientRequest);
        if ("401".equals(mode) || "404".equals(mode)) { // UNAUTHORIZED, NOT FOUND
            sendProxyResponseError(clientRequest, proxyResponse, Integer.parseInt(mode));
        } else { // 403 FORBIDDEN
            super.onProxyRewriteFailed(clientRequest, proxyResponse);
        }
    }
    
    @Override
    protected void onResponseContent(HttpServletRequest request, HttpServletResponse response, byte[] buffer) {
        new CookieAuthorization(request, authorizationService).onResponseContent(response, buffer, getMode(request));
    }

    private String getMode(HttpServletRequest request) {
        return (String) request.getAttribute(MODE);
    }
    
    private void setMode(HttpServletRequest request, String value) {
        request.setAttribute(MODE, value);
    }
}
