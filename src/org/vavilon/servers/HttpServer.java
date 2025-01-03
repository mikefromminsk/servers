package org.vavilon.servers;

import org.vavilon.servers.model.Endpoint;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static org.vavilon.Utils.*;
import static org.vavilon.Utils.gson;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public abstract class HttpServer extends NanoHTTPD {
    static final String MIME_JSON = "application/json";
    private final String domain;

    public HttpServer(String domain, int port) {
        super(port);
        this.domain = domain;
    }

    abstract public Endpoint getEndpoint(String uri);

    static Map<String, String> parseParams(NanoHTTPD.IHTTPSession session) {
        Map<String, String> params = new HashMap<>();
        if (session.getMethod() == NanoHTTPD.Method.POST) {
            try {
                session.parseBody(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        params.putAll(session.getParms());
        return params;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Long start = time();
        String scriptPath = session.getUri().substring(1);
        Endpoint endpoint = getEndpoint(scriptPath);
        try {
            Map<String, String> params = parseParams(session);
            params.put("site_domain", domain);
            endpoint.run(scriptPath, params);
            endpoint.commit();
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            if (e.getMessage().charAt(0) == '{') {
                error.put("message", gson.fromJson(e.getMessage(), Map.class));
            } else {
                error.put("message", e.getMessage());
            }
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            LinkedList<String> stack = new LinkedList<>(Arrays.asList(writer.toString().split("\r\n\t")));
            stack.removeFirst();
            stack.removeLast();
            stack.removeLast();
            stack.removeLast();
            error.put("stack", stack);
            System.out.println("error: " + scriptPath);
            Response response = newFixedLengthResponse(INTERNAL_ERROR, MIME_JSON, gson.toJson(error));
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        }
        endpoint.response.put("success", "true");
        System.out.println("success: " + scriptPath + " took " + (time() - start) + "ms");
        Response response = newFixedLengthResponse(OK, MIME_JSON, gson.toJson(endpoint.response));
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }

    @Override
    public void start() throws IOException {
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println(this.getClass().getSimpleName() +  " started on " + getListeningPort());
    }
}
