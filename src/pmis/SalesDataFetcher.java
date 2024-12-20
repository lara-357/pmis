/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pmis;

/**
 *
 * @author Dell
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesDataFetcher {
    public static List<SalesData> getSalesData() throws SQLException {
        List<SalesData> salesList = new ArrayList<>();
        String query = "SELECT SaleDate, SUM(UnitPrice * QuantitySold) AS DailyTotal FROM Sales GROUP BY SaleDate ORDER BY SaleDate ASC;";

        try (Connection conn = DbConnection.ConnectionDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String date = rs.getString("SaleDate");
                double totalSales = rs.getDouble("DailyTotal");
                salesList.add(new SalesData(date, totalSales));
            }
        }
        return salesList;
    }
}

class SalesData {
    String date;
    double totalSales;

    public SalesData(String date, double totalSales) {
        this.date = date;
        this.totalSales = totalSales;
    }
}
