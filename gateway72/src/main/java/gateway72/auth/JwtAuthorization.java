package gateway72.auth;

import java.util.ArrayList;

import gateway72.Gateway72Logger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultJwtParser;

public class JwtAuthorization extends AbstractAuthorization {
    private static final String TOKEN_PREFIX = "Bearer ";
    private final String authorization;
    
    public JwtAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public void fetchUserData() throws StatusException {
        if (!authorization.startsWith(TOKEN_PREFIX)) {
            throw new StatusException(401, "invalid Authorization header");
        }
        String token = authorization.replace(TOKEN_PREFIX, "");
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
            if (userId != null && !userId.trim().isEmpty()) {
                Object scopes = claims.get("scopes"); // roles
                if (scopes instanceof String) {
                    givenRoles = new ArrayList<>();
                    String[] w = ((String) scopes).split(",");
                    for (int i = 0; i < w.length; i++) {
                        String wt = w[i].trim();
                        if (!wt.isEmpty()) {
                            givenRoles.add(wt);
                        }
                    }
                    Gateway72Logger.instance.role(true, "given roles out of JWT token: " + givenRoles);
                } else {
                    Gateway72Logger.instance.role(false, "no scopes in JWT token");
                }
            }
        } catch (Exception e) {
            throw new StatusException(401, e);
        }
    }
    
    private static Claims getClaimsFromToken(String token) {
        String[] w = token.split("\\.");
        String unsignedToken = w[0] + "." + w[1] + ".";
        Jwt<?, ?> jwt = (Jwt<?, ?>) new DefaultJwtParser().parse(unsignedToken);
        return (Claims) jwt.getBody();
    }
}
