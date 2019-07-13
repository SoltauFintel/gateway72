package gateway72.jetty;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

/**
 * We show our own error message
 */
public class GatewayErrorHandler extends ErrorHandler {
    private final CORS cors;

    public GatewayErrorHandler(CORS cors) {
        this.cors = cors;
    }

    @Override
    public void doError(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        cors.handle(response);
        super.doError(target, baseRequest, request, response);
    }
    
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        writeErrorPageMessage(request, writer, code, message, request.getRequestURI());
        // don't print stacktrace, don't print Jetty version
    }

    protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message, String uri) throws IOException {
        writer.write("<div style=\"font-family: Verdana; text-align: center;\"><h1>Gateway error</h1>");

        writer.write("<h2>\r\n");
        writer.write(Integer.toString(code));
        writer.write(" - ");
        writer.write(message);
        writer.write("\r\n</h2>");

        writer.write("<p style=\"margin-top: 5cm;\"><a href=\"/\" style=\"text-decoration: none;\">Go to homepage &gt;&gt;</a></p></div>");
    }
}
