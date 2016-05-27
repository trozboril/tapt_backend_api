import static spark.Spark.*;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;

import org.mindrot.jbcrypt.BCrypt;

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
        String herokuAuth = System.getenv("HEROKU_AUTH");
        if (herokuAuth == null) {
            herokuAuth = "jdbc:postgresql://localhost/tapt_api";
        }
        if (databaseUrl == null) {
            databaseUrl = "postgresql://localhost/tapt_api";
        } else {
            databaseUrl = databaseUrl.replaceAll("postgres", "postgresql");
        }
        cpds.setJdbcUrl(herokuAuth);
        port(getHerokuAssignedPort());
        enableCORS("*", "*", "*");
        post("/userRegister", usersRegister);
//        post("/ownerRegister", ownerRegister);
        get("/", home);
        get("/beertypes", beertypes);
        get("/beertypes/:id", beertypesId);
//        get("/breweries", breweries);
//        get("userdash", userdash);
//        get("ownerdash", ownerdash);

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

    private static Route usersRegister = new Route() {
        public Object handle(Request request, Response response) throws Exception {

            String string = request.body();
            String[] parts = string.split("&");
            String name = parts[0];
            String email = parts[1];
            String password = parts[2];

            name = name.replace("name=", "");
            email = email.replace("email=", "");
            password = password.replace("password=", "");
            password = BCrypt.hashpw(password, BCrypt.gensalt(10));

            System.out.println(email);
            System.out.println(name);
            System.out.println(password);

            Connection connection = cpds.getConnection();

            String query = "SELECT email FROM users WHERE email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            JSONObject object = new JSONObject();

            if (resultSet.next()) {
                object.put("status", 409);
                object.put("message", "User already exists");
                response.status(409);
                response.type("application/json");
            } else {
                query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, password);

                System.out.println(preparedStatement);

                preparedStatement.execute();
                object.put("status", 201);
                object.put("message", "User created");
//                object.put("token", createJWT(email));
                response.status(201);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();

        }
    };

    private static Route ownerRegister = new Route() {
        public Object handle(Request request, Response response) throws Exception {

            String string = request.body();
            String[] parts = string.split("&");
            String email = parts[0];
            String first_name = parts[1];
            String last_name = parts[2];
            String phone_number = parts[3];
            String password = parts[4];



            password = BCrypt.hashpw(password, BCrypt.gensalt(10));

            Connection connection = cpds.getConnection();

            String query = "SELECT email FROM owners WHERE email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            System.out.println(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            JSONObject object = new JSONObject();

            if (resultSet.next()) {
                object.put("status", 409);
                object.put("message", "User already exists");
                response.status(409);
                response.type("application/json");
            } else {
                query = "INSERT INTO owners (email, first_name, last_name, phone_number, password) VALUES (?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, first_name);
                preparedStatement.setString(3, last_name);
                preparedStatement.setString(4, phone_number);
                preparedStatement.setString(5, password);


                System.out.println(preparedStatement);

                preparedStatement.execute();
                object.put("status", 201);
                object.put("message", "Owner created");
//                object.put("token", createJWT(email));
                response.status(201);
                response.type("application/json");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            return object.toString();
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


