import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    Properties properties;

    AppProperties(){
        properties = new Properties();
        try (InputStream is = getClass().getResourceAsStream("application.properties")) {
            properties.load(is);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public String getProperty(String propertyName){
        return properties.getProperty(propertyName);
    }
}
