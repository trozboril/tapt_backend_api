import static spark.Spark.*;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;


import spark.Response;
import spark.Route;
import spark.Filter;

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
        String databaseUrl = System.getenv("HEROKU_POSTGRESQL_AQUA_URL");
        if (databaseUrl == null) {
            databaseUrl = "postgresql://localhost/tapt_api";
        } else {
            databaseUrl = databaseUrl.replaceAll("postgres", "postgresql");
        }
        System.out.println(databaseUrl);
        cpds.setJdbcUrl("jdbc:postgresql://ec2-23-23-199-72.compute-1.amazonaws.com:5432/dcp0qcse5pokul?user=yuvzyisevdvoan&password=SYJCWMboRrPG_BXM5Uu3GWeyHI&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory");
        port(getHerokuAssignedPort());
        enableCORS("*", "*", "*");
        post("/users", users);
        get("/", home);
        get("/beertypes", beertypes);
        get("/beertypes/:id", beertypesId);
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        before(new Filter() {
            public void handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            }
        });
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
            Connection connection = cpds.getConnection();
            String query = "INSERT INTO users";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            return "This is your stuff";
        }
    };

    private static Route beertypesId = new Route() {
        public Object handle(Request request, Response response) throws Exception {
            Connection connection = cpds.getConnection();
            String query = "SELECT * FROM beertypes WHERE id = " + request.params("id");
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData rsmd = resultSet.getMetaData();
            JSONObject oneBeer = new JSONObject();
            if (resultSet.next()) {
                for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                    oneBeer.put(rsmd.getColumnName(i), resultSet.getString(i));
                }
                return oneBeer;
            } else {
                oneBeer.put("Nothing", "Found");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return oneBeer;
        }
    };
}


