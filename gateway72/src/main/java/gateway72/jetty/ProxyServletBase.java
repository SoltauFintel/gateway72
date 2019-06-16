package gateway72.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.Callback;

/**
 * Keep away Jetty-specific classes from Gateway72Servlet
 */
public abstract class ProxyServletBase extends ProxyServlet {
    
    @Override
    protected final void onResponseContent(HttpServletRequest request, HttpServletResponse response, Response proxyResponse,
            byte[] buffer, int offset, int length, Callback callback) {
        onResponseContent(request, response, buffer);
        super.onResponseContent(request, response, proxyResponse, buffer, offset, length, callback);
    }

    protected abstract void onResponseContent(HttpServletRequest request, HttpServletResponse response, byte[] buffer);
}
