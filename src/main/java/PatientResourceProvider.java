import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4b.model.IdType;
import org.hl7.fhir.r4b.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientResourceProvider implements IResourceProvider {
    private Connection dbConnection;

    public PatientResourceProvider(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    private Patient parsePatientResult(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        Date dob = resultSet.getDate("date_of_birth");

        Patient patient = new Patient();
        patient.setId(String.valueOf(id));
        patient.addName().addGiven(firstName).setFamily(lastName);
        patient.setBirthDate(dob);

        return patient;
    }

    @Read()
    public Patient read(@IdParam IdType id) throws SQLException {
        int patientId = 0;
        try {
            patientId = Integer.parseInt(id.getIdPart());
        } catch (Exception e) {
            throw new ResourceNotFoundException(id);
        }

        PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM patients WHERE id = ?");
        statement.setInt(1, patientId);
        ResultSet patientResultSet = statement.executeQuery();

        if (patientResultSet.next()) {
            Patient patient = parsePatientResult(patientResultSet);
            return patient;
        } else {
            throw new ResourceNotFoundException(id);
        }
    }

    @Search()
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();

        PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM patients");
        ResultSet patientResultSet = statement.executeQuery();

        while (patientResultSet.next()) {
            Patient patient = parsePatientResult(patientResultSet);

            patients.add(patient);
        }

        return patients;
    }
}