import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObservationResourceProvider implements IResourceProvider {
    private Connection dbConnection;

    public ObservationResourceProvider(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    private Observation parseBloodPressureResult(ResultSet resultSet) throws SQLException {
        String id = String.format("%s-blood-pressure", resultSet.getInt("id"));
        int patientId = resultSet.getInt("patient_id");
        int systolic = resultSet.getInt("systolic");
        int diastolic = resultSet.getInt("diastolic");
        Date date = resultSet.getDate("date");

        Observation observation = new Observation();
        observation.setId(String.valueOf(id));
        observation.setStatus(Enumerations.ObservationStatus.FINAL);
        observation.getCode().setText("Blood pressure panel with all children optional");
        observation.getCode().addCoding().setCode("85354-9").setSystem("http://loinc.org");
        Observation.ObservationComponentComponent systolicComponent
                = new Observation.ObservationComponentComponent()
                .setValue(new Quantity().setValue(systolic).setUnit("mmHg"));
        observation.addComponent(systolicComponent);
        Observation.ObservationComponentComponent diastolicComponent
                = new Observation.ObservationComponentComponent()
                .setValue(new Quantity().setValue(diastolic).setUnit("mmHg"));
        observation.addComponent(diastolicComponent);
        observation.setIssued(date);
        observation.setSubject(new Reference("Patient/" + patientId));

        return observation;
    }

    private Observation parseHeartRateResult(ResultSet resultSet) throws SQLException {
        String id = String.format("%s-heart-rate", resultSet.getInt("id"));
        int patientId = resultSet.getInt("patient_id");
        int rate = resultSet.getInt("rate");
        Date date = resultSet.getDate("date");

        Observation observation = new Observation();
        observation.setId(String.valueOf(id));
        observation.setStatus(Enumerations.ObservationStatus.FINAL);
        observation.getCode().setText("Heart Rate");
        observation.getCode().addCoding().setCode("8867-4").setSystem("http://loinc.org");
        Observation.ObservationComponentComponent rateComponent
                = new Observation.ObservationComponentComponent()
                .setValue(new Quantity().setValue(rate).setUnit("/min"));
        observation.addComponent(rateComponent);
        observation.setIssued(date);
        observation.setSubject(new Reference("Patient/" + patientId));

        return observation;
    }

    @Read()
    public Observation read(@IdParam IdType id) throws SQLException {
        String[] idParts = id.getIdPart().split("-");
        if (idParts.length < 3) throw new ResourceNotFoundException(id);
        int tableId = 0;
        try {
            tableId = Integer.parseInt(idParts[0]);
        } catch (Exception e) {
            throw new ResourceNotFoundException(id);
        }
        String tableName = null;
        if (String.format("%s-%s", idParts[1], idParts[2]).toLowerCase().equals("blood-pressure")) {
            tableName = "blood_pressure";
        } else if (String.format("%s-%s", idParts[1], idParts[2]).toLowerCase().equals("heart-rate")) {
            tableName = "heart_rate";
        } else {
            throw new ResourceNotFoundException(id);
        }

        PreparedStatement statement = dbConnection.prepareStatement(String.format("SELECT * FROM %s WHERE id = ?", tableName));
        statement.setInt(1, tableId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Observation observation = tableName == "blood_pressure" ? parseBloodPressureResult(resultSet) : parseHeartRateResult(resultSet);
            return observation;
        } else {
            throw new ResourceNotFoundException(id);
        }
    }

    @Search()
    public List<Observation> getAllObservations() throws SQLException {
        List<Observation> observations = new ArrayList<>();

        PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM blood_pressure");
        ResultSet bloodPressureResultSet = statement.executeQuery();

        while (bloodPressureResultSet.next()) {
            Observation observation = parseBloodPressureResult(bloodPressureResultSet);
            observations.add(observation);
        }
        bloodPressureResultSet.close();

        statement = dbConnection.prepareStatement("SELECT * FROM heart_rate");
        ResultSet heartRateResultSet = statement.executeQuery();
        while (heartRateResultSet.next()) {
            Observation observation = parseHeartRateResult(heartRateResultSet);
            observations.add(observation);
        }
        heartRateResultSet.close();

        return observations;
    }
}