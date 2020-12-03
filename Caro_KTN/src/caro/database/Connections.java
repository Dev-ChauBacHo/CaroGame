package caro.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connections {
    private final static String USER_NAME = "sa";
    private final static String PASSWORD = "Vinh1106";
    private final static String dbClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final static String dbUrl = "jdbc:sqlserver://localhost\\VINHTRAN:1433;" +
            "databaseName = caro";
     public static Connection getConnection(){
        Connection con = null;
        try
        {
            Class.forName(dbClass);
            con=DriverManager.getConnection(dbUrl, USER_NAME, PASSWORD);
        }
        catch (ClassNotFoundException e)
        {
            System.out.println( "Conection fail!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println( "Conection fail!");
            e.printStackTrace();
        }
         return con;
    }
}
