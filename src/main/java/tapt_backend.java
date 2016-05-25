import static spark.Spark.*;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * Created by tylerrozboril on 5/23/16.
 */
public class tapt_backend {
    /**
     * The pool of database connections
     */
    private static ComboPooledDataSource cpds;

    public static void main (String [] args) {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl("jdbc:postgresql://localhost/tapt_api");
        port(8200);
        post("/users", users);
        get("/", home);
        get("/beertypes", beertypes);
    }

    private static Route home = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            Connection connection = cpds.getConnection();
            //Prepared Statment (protect from malicious SQL queries.
            String query = "SELECT name FROM users WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, 1);
            ResultSet resultSet = preparedStatement.executeQuery();
            //looping through if resultSet has next
            String returner = "Not found :(";
            if (resultSet.next()){
                returner = "Hello " + resultSet.getString("name");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return returner;
        }
    };

    private static Route beertypes = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            Connection connection = cpds.getConnection();
            String query = "SELECT * FROM beertypes";
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();
            //looping through if resultSet has next
//            String returner = "Not found :(";
            JSONArray beerTypeArray = new JSONArray();
            ResultSetMetaData rsmd = resultSet.getMetaData();
            while (resultSet.next()) {
                JSONObject temp = new JSONObject();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    temp.put(rsmd.getColumnName(i), resultSet.getString(i));
                }
                beerTypeArray.put(temp);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return beerTypeArray.toString();
        }
    };

    private static Route users = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            System.out.println(request.body());
            return "This is your stuff";
        }
    };
}


