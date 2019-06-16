package gateway72;

/**
 * API Gateway configuration by environment variables
 */
public class Gateway72Config {
    public static final int PORT = 4100;

    private Gateway72Config() {
    }
    
    public static String getDir() {
        String dir = System.getenv("GATEWAY_CONFIG_DIR");
        if (dir == null || dir.trim().isEmpty()) {
            dir = "gateway-config";
        }
        return dir;
    }
    
    public static int getPort() {
        final String name = "GATEWAY_PORT";
        try {
            return Integer.parseInt(System.getProperty(name, System.getenv(name)));
        } catch (NumberFormatException e) {
            return PORT;
        }
    }
    
    public static String getContextPath() {
        final String name = "GATEWAY_CONTEXT_PATH";
        String contextPath = System.getProperty(name, System.getenv(name));
        // contextPath can be "/" or e.g. "/api".
        if (contextPath == null || !contextPath.startsWith("/")) {
            contextPath = "/";
        }
        return contextPath;
    }

    public static String getJwtPublicSigningKey() {
        final String name = "GATEWAY_JWT_PUBLIC_KEY";
        String ret = System.getProperty(name, System.getenv(name));
        if (ret == null || ret.trim().isEmpty()) {
            throw new RuntimeException("Please set env var " + name);
        }
        return ret;
    }
}
