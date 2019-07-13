package gateway72.jetty;

import javax.servlet.http.HttpServletResponse;

import gateway72.Gateway72Logger;

public class CORS {
    private final boolean active;
    
    public CORS(boolean active) {
        this.active = active;
        if (active) {
            Gateway72Logger.instance.log("Allow... headers activated (CORS)");
        }
    }
    
    public void handle(HttpServletResponse response) {
        if (active) {
            try {
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Allow-Methods", "*");
            } catch (Exception ignore) {
            }
        }
    }
}
