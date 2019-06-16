package gateway72.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import gateway72.Gateway72Servlet;
import gateway72.cookie.CookieAuthorization;

public class AuthorizationService {
    private final Map<String, List<String>> roles = new HashMap<>(); // TODO limit size (remove not often used entries)

    public String checkRoles(HttpServletRequest request, List<String> roles) throws StatusException {
        if (roles == null || roles.isEmpty()) {
            return "not protected";
        }
        if (roles.contains(CookieAuthorization._LOGIN)) {
            request.setAttribute(Gateway72Servlet.MODE, CookieAuthorization._LOGIN);
            return "login mode";
        }

        // Route is protected by at least 1 role.
        AbstractAuthorization auth = getAuthorization(request);
        auth.fetchUserData();
        String userId = auth.getUserId();
        List<String> givenRoles = auth.getGivenRoles();
        // Needed roles satisfied.

        String info = "user " + userId + " has enough rights.";
        for (String role : roles) {
            if (CookieAuthorization._LOGOUT.equals(role)) {
                request.setAttribute(Gateway72Servlet.MODE, CookieAuthorization._LOGOUT);
                info = CookieAuthorization._LOGOUT + " mode";
            } else if (!role.isEmpty() && !givenRoles.contains(role)) {
                throw new StatusException(403, "not authorized, missing role: " + role
                        + ", needed roles: " + roles + ", given roles: " + givenRoles);
            }
        }
        return info;
    }

    private AbstractAuthorization getAuthorization(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        AbstractAuthorization auth;
        if (authorization == null || authorization.trim().isEmpty()) { // Cookie based
            auth = new CookieAuthorization(request, this);
        } else { // JWT token based
            auth = new JwtAuthorization(authorization);
        }
        return auth;
    }

    public Map<String, List<String>> getRoles() {
        return roles;
    }
}
