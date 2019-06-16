package gateway72;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Testserver1 implements Runnable {
    public static String contextPath = "/rest/mouse/*";
    
    @Override
    public void run() {
        Server server = new Server(6066);
        ConnectHandler proxy = new ConnectHandler();
        server.setHandler(proxy);
        ConnectHandler parent = proxy;
        String contextPath = "/";
        int options = ServletContextHandler.SESSIONS;
        ServletContextHandler context = new ServletContextHandler(parent, contextPath, options);
        
        ServletHolder servlet = new ServletHolder(Testservlet1.class);
        context.addServlet(servlet, contextPath);
        System.out.println("Testserver1 6066 >>");
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class Testservlet1 extends HttpServlet {
        
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                System.out.println("[Testserver1] GET " + req.getRequestURL().toString() + (req.getQueryString() == null ? "": "?" + req.getQueryString()));
                //dumpHeaders(req);
                String path = req.getRequestURI();
                if ("/do-logout".equals(path)) {
                    resp.getWriter().print("ok");
                    resp.setStatus(200);
                } else if (path.startsWith("/person/")) {
                    int n;
                    try {
                        n = Integer.parseInt(path.replace("/person/", ""));
                    } catch (Exception e) {
                        resp.getWriter().print("Illegal person number: " + path);
                        resp.setStatus(500);
                        return;
                    }
                    if (n == 2) {
                        resp.getWriter().print("Christian");
                        resp.setStatus(200);
                    } else if (n == 10) {
                        resp.getWriter().print("Marcus");
                        resp.setStatus(200);
                    } else {
                        resp.getWriter().print("Person #" + n + " is unknown.");
                        resp.setStatus(404);
                    }
                } else {
                    resp.getWriter().print("ok by mouse, " + System.currentTimeMillis());
                    resp.setStatus(200);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void dumpHeaders(HttpServletRequest req) {
            Enumeration<String> en = req.getHeaderNames();
            while (en.hasMoreElements()) {
                String headerName = en.nextElement();
                System.out.println("\t"+headerName + ": " + req.getHeader(headerName));
            }
        }
        
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            try {
                System.out.println("[Testserver1] POST " + req.getRequestURL().toString() + (req.getQueryString() == null ? "": "?" + req.getQueryString()));
                
                String data = "";
                try (BufferedInputStream s = new BufferedInputStream(req.getInputStream())) {
                    final int l = (int) req.getContentLength();
                    byte[] b = new byte[l];
                    s.read(b, 0, l);
                    data = new String(b);
                    System.out.println("[Testserver1] req.entity: »" + data + "«");
                }
                
//                Enumeration<String> en = req.getHeaderNames();
//                while (en.hasMoreElements()) {
//                    String headerName = en.nextElement();
//                    System.out.println("\t"+headerName + ": " + req.getHeader(headerName));
//                }

                if ("/do-login".equals(req.getRequestURI()) && "login=Mickey&password=secret".equals(data)) {
                    resp.getWriter().print("{\"id\":\"4711\",\"roles\":[{\"name\":\"USER\"}]}");
                } else {
                    resp.getWriter().print("ok by Postmaus, " + System.currentTimeMillis());
                }
                resp.setStatus(200);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
