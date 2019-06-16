package gateway72.configuration;

/**
 * HTTP method
 */
public enum Verb {
    
    GET, POST, PUT, DELETE;
    
    public static Verb fromString(String str) {
        if ("GET".equalsIgnoreCase(str)) {
            return GET;
        } else if ("POST".equalsIgnoreCase(str)) {
            return POST;
        } else if ("PUT".equalsIgnoreCase(str)) {
            return PUT;
        } else if ("DELETE".equalsIgnoreCase(str)) {
            return DELETE;
        } else {
            throw new RuntimeException("Unsupported verb: '" + str + "'");
        }
    }
}
