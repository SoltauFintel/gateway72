package gateway72;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

import gateway72.configuration.Configuration;
import gateway72.configuration.ConfigurationReader;

/**
 * JWT token based authentication and authorization test
 */
public class JwtAuthTest {
    private String token = null;
    private RestCaller client = new RestCaller() {
        protected void init(org.apache.http.client.methods.HttpRequestBase request) {
            if (token != null) {
                request.setHeader("Authorization", token);
            }
        }
    };

    @Test
    public void test() throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        Testserver1.contextPath = "/*";
        threadPool.execute(new Testserver1());
        String config = "GET /protected -> t1 -> /protected {ROLE_USER}"
                + "\nservices:\nt1 -> http://localhost:6066";
        Configuration model = new ConfigurationReader().parse(config);
        threadPool.execute(() -> Gateway72App.start(Gateway72Config.PORT, model));

        CloseableHttpClient client = HttpClientBuilder.create().build();
        CloseableHttpResponse res;
        HttpGet get;
        
        // Test 1
        // protected Test
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/protected");
        res = client.execute(get);
        Assert.assertEquals(401, res.getStatusLine().getStatusCode());
        
        // Test 2
        // now try it with a JWT token
        System.out.println("---------------------------------------");
        get = new HttpGet("http://localhost:4100/protected");
        get.setHeader("Authorization", getToken());
        res = client.execute(get);
        Assert.assertEquals("JWT access does not work", 200, res.getStatusLine().getStatusCode());

        threadPool.shutdown();
    }
    
    // Ich hol mir einfach ein Token vom userdata Service :-)
    private String getToken() {
        LoginData login = new LoginData();
        login.setEmail(Secrets.JWT_MAILADDRESS);
        login.setPassword(Secrets.JWT_PASSWORD);
        try {
            return doLogin(login);
        } catch (Exception e) {
            if (e.getMessage().contains("Bad credentials")) {
                // hmm, es gibt den User nicht. Das passiert mglw. wenn userdata die Daten verloren hat. Dann leg ich den User halt an... :-)
                try {
                    client.post(Secrets.JWT_REGISTER_URL, new Gson().toJson(login), "application/json");
                    return doLogin(login);
                } catch (IOException ee) {
                    throw new RuntimeException(ee);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private String doLogin(LoginData login) throws IOException {
        String res = client.post(Secrets.JWT_LOGIN_URL, new Gson().toJson(login), "application/json");
        return res.replace("{\"token\":\"", "Bearer ").replace("\"}", "");
    }
    
    public static class LoginData {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
