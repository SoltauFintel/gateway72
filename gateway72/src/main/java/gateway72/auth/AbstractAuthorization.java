package gateway72.auth;

import java.util.List;

public abstract class AbstractAuthorization {
    protected String userId;
    protected List<String> givenRoles;

    public abstract void fetchUserData() throws StatusException;
    
    public String getUserId() {
        return userId;
    }

    public List<String> getGivenRoles() throws StatusException {
        if (givenRoles == null) {
            throw new StatusException(401, "not logged in (no roles)");
        }
        return givenRoles;
    }
}
