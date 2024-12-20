/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pmis;

import java.sql.*;
/**
 *
 * @author Dell
 */
public class DbConnection {
    private static Connection connection;

    public static Connection ConnectionDB() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:PMISdb.db");
        }
        return connection;
    }
}
