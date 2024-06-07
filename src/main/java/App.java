import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class App {

    public static void main(String[] args) throws Exception {
        AppProperties appProps = new AppProperties();

        Server server = new Server(Integer.parseInt(appProps.getProperty("port")));

        FhirServlet fhirServlet = new FhirServlet(appProps);

        ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(fhirServlet), "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
