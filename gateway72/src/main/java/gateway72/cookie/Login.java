package gateway72.cookie;

import java.util.List;

// JSON
public class Login {
    private String id;
    private List<Role> roles;

    /**
     * @return user ID
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return can be null or empty
     */
    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
