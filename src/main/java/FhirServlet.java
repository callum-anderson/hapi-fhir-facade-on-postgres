import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import jakarta.servlet.ServletException;

import java.sql.Connection;
import java.sql.DriverManager;

public class FhirServlet extends RestfulServer {
    AppProperties appProps;
    FhirServlet(AppProperties appProps){
        this.appProps = appProps;
    }

    @Override
    protected void initialize() throws ServletException {
        setFhirContext(FhirContext.forR4B());

        Connection dbConnection;
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(
                    appProps.getProperty("db_jdbc_url"),
                    appProps.getProperty("db_username"),
                    appProps.getProperty("db_password")
            );
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        registerProvider(new PatientResourceProvider(dbConnection));
        registerProvider(new ObservationResourceProvider(dbConnection));
        registerInterceptor(new ResponseHighlighterInterceptor());
    }

}
