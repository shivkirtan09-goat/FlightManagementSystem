import java.sql.Connection;
import java.sql.DriverManager;

public class DB {

    public static Connection connection() {

        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/your_database_name",
                    "root",
                    "shivkirtan"
            );

        } catch (Exception e) {
            System.out.println("DB Connection Error: " + e.getMessage());
        }

        return con;
    }
}