package fr.anthonyquere.dumbcrud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * DummyCrudEndpoint creates a simple web server that handles GET and POST operations on a single endpoint.
 *
 * @param <Domain> domain manage by the crud
 */
public class DummyCrudEndpoint<Domain> {


    private final String domainName;
    private final CrudProvider<Domain> crudProvider;
    private final Class<Domain> domainClass;

    /**
     * Create a DummyCrudEndpoint
     * @param domainName Path ot the crud endpoint, will be appended at the end of the url
     * @param crudProvider Instance of a CrudProvider with the same domain as this class
     * @param domainClass A class object extracted from the domain object
     */
    public DummyCrudEndpoint(String domainName, CrudProvider<Domain> crudProvider, Class<Domain> domainClass) {
        this.domainName = domainName;
        this.crudProvider = crudProvider;
        this.domainClass = domainClass;
    }

    public record MessageResponse(String message) {
    }

    /**
     * Starts the web server on the given ports. It listens to any host (0.0.0.0).
     * @param port port to listen
     * @throws IOException fail to parse / un-parse from JSON
     */
    public void run(int port) throws IOException {
        var api = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);


        api.createContext("/" + domainName, exchange -> {
            var objectParser = new ObjectMapper();

            System.err.println("Received request");

            var method = exchange.getRequestMethod();
            var bodyWriter = exchange.getResponseBody();
            Object bodyContent;

            if (method.equals("GET")) {
                try {
                    bodyContent = crudProvider.list();
                } catch (Exception e) {
                    bodyContent = new MessageResponse("ERROR: " + e.getMessage());
                }
            } else if (method.equals("POST")) {
                try {
                    var domain = objectParser.readValue(exchange.getRequestBody(), domainClass);
                    crudProvider.add(domain);
                    bodyContent = new MessageResponse("inserted");
                } catch (Exception e) {
                    bodyContent = new MessageResponse("ERROR: " + e.getMessage());
                }
            } else {
                bodyContent = new MessageResponse("Invalid method");
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);

            var bodyBytes = objectParser.writeValueAsBytes(bodyContent);

            bodyWriter.write(bodyBytes);
            bodyWriter.flush();
            bodyWriter.close();
        });

        System.err.println("Starting web server on " + api.getAddress().toString());
        System.err.println("Thread priority " + Thread.currentThread().getPriority());
        api.start();
    }
}
