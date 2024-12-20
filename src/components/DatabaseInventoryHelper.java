/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package components;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import pmis.DbConnection;

/**
 *
 * @author Dell
 */
public class DatabaseInventoryHelper {
    public static class Product {
        public String name;
        public String prdctType;
        public int stocks;
        public double price;
        public int productID;
        public Date deliveryDate;
        public Date manufacturedDate;
        public Date expirationDate;

        public Product(String name, String prdctType, int stocks, double price, int productID, Date deliveryDate, Date manufacturedDate, Date expirationDate) {
            this.name = name;
            this.prdctType = prdctType;
            this.stocks = stocks;
            this.price = price;
            this.productID = productID;
            this.deliveryDate = deliveryDate;
            this.manufacturedDate = manufacturedDate;
            this.expirationDate = expirationDate;
        }
    }
    
    public static List<Product> fetchInventoryItems() throws ParseException {
        List<Product> inventoryList = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DbConnection.ConnectionDB();

            String query = """
                SELECT ProductID, ProductName, ProductType, Stocks, Price, DeliveryDate, ManufacturedDate, ExpirationDate
                FROM Products
                ORDER BY ProductName ASC
            """;

            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            while (rs.next()) {
                String name = rs.getString("ProductName");
                String prdctType = rs.getString("ProductType");
                int stocks = rs.getInt("Stocks");
                double price = rs.getDouble("Price");
                int productID = rs.getInt("ProductID");

                String deliveryDateStr = rs.getString("DeliveryDate");
            String manufacturedDateStr = rs.getString("ManufacturedDate");
            String expirationDateStr = rs.getString("ExpirationDate");

            Date deliveryDate = deliveryDateStr != null ? new java.sql.Date(sdf.parse(deliveryDateStr).getTime()) : null;
            Date manufacturedDate = manufacturedDateStr != null ? new java.sql.Date(sdf.parse(manufacturedDateStr).getTime()) : null;
            Date expirationDate = expirationDateStr != null ? new java.sql.Date(sdf.parse(expirationDateStr).getTime()) : null;



                // Add the product to the list
                inventoryList.add(new Product(name, prdctType, stocks, price, productID, deliveryDate, manufacturedDate, expirationDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return inventoryList;
    }

}
