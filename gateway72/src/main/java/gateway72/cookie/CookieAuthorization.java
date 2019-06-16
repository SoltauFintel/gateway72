package gateway72.cookie;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import gateway72.Gateway72Logger;
import gateway72.auth.AbstractAuthorization;
import gateway72.auth.AuthorizationService;
import gateway72.auth.StatusException;

public class CookieAuthorization extends AbstractAuthorization {
    public static final String _LOGIN = "$LOGIN";
    public static final String _LOGOUT = "$LOGOUT";
    private final Cookie userIdCookie = new Cookie("USER_ID");
    private final HttpServletRequest request;
    private final AuthorizationService authSv;
    
    public CookieAuthorization(HttpServletRequest request, AuthorizationService authSv) {
        this.request = request;
        this.authSv = authSv;
    }

    @Override
    public void fetchUserData() throws StatusException {
        // old-school Cookie based
        userId = userIdCookie.get(request);
        if (userId == null || userId.trim().isEmpty()) {
            throw new StatusException(401, "not logged in (no token, no cookie)");
            // Calling webpage should now show the login page.
        }

        // Using the authenticated user to get his roles.
        givenRoles = authSv.getRoles().get(userId);
    }
    
    public void onResponseContent(HttpServletResponse response, byte[] buffer, String mode) {
        if (_LOGIN.equals(mode)) {
            Gateway72Logger.instance.log(_LOGIN + " mode. Session ID: " + request.getSession().getId());
            String json = new String(buffer);
            Login login = new Gson().fromJson(json, Login.class);
            userIdCookie.set(login.getId(), response);
            authSv.getRoles().put(login.getId(), login.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        } else if (_LOGOUT.equals(mode)) {
            String userId = userIdCookie.get(request);
            if (userId != null) {
                authSv.getRoles().remove(userId);
                Gateway72Logger.instance.log("user entry '" + userId + "' removed");
                userIdCookie.remove(request, response);
            } else {
                Gateway72Logger.instance.log("User '" + userId + "' possibly already logged out.");
            }
        }
    }
}
