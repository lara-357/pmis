package components;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import pmis.DbConnection;

/**
 *
 * @author Dell
 */



public class DatabaseHelper {

    public static Product fetchProductDetails(int productID) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public static class Product {
        public String name;
        public String prdctType;
        public int stocks;
        public double price;
        public int productID;
        

        public Product(String name, String prdctType, int stocks, double price, int productID) {
            this.name = name;
            this.prdctType = prdctType;
            this.stocks = stocks;
            this.price = price;
            this.productID = productID;
        }
    }
     
    public static List<Product> fetchItem(int productID) {
        List<Product> itemList = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DbConnection.ConnectionDB();

            String query = "SELECT ProductID, ProductName, Stocks, Price, ProductType FROM Products WHERE ProductID = ?";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, productID);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("ProductName");
                int stocks = rs.getInt("Stocks");
                double price = rs.getDouble("Price");
                String prdctType = rs.getString("ProductType");
                itemList.add(new Product(name,prdctType, stocks, price, productID));
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

        return itemList;
    }


    public static List<Product> fetchProducts(String searchTerm) {
        List<Product> productList = new ArrayList<>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Establish connection
            con = DbConnection.ConnectionDB();

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                // Default mode: Fetch one product per category
                String query = """
                    SELECT ProductID, ProductName, ProductType, Stocks, Price 
                    FROM Products 
                    WHERE ProductID IN (
                        SELECT MIN(ProductID) 
                        FROM Products 
                        GROUP BY ProductType
                    );
                """;
                pstmt = con.prepareStatement(query);
            } else {
                // Search mode: Fetch products matching the search term
                String query = "SELECT ProductName, ProductType, Stocks, Price, ProductID FROM Products WHERE ProductName LIKE ?";
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, "%" + searchTerm + "%");
            }

            // Execute query
            rs = pstmt.executeQuery();

            // Populate product list
            while (rs.next()) {
                String name = rs.getString("ProductName");
                String prdctType = rs.getString("ProductType");
                int stocks = rs.getInt("Stocks");
                double price = rs.getDouble("Price");
                int productID = rs.getInt("ProductID");
                productList.add(new Product(name, prdctType, stocks, price, productID));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return productList;
    }
    
    public static boolean updateProductStock(int productID, int newStock) {
        try {
            // Use try-with-resources to automatically close resources
            try (Connection con = DbConnection.ConnectionDB();
                 PreparedStatement pstmt = con.prepareStatement("UPDATE Products SET Stocks = ? WHERE ProductID = ?")) {

                pstmt.setInt(1, newStock);
                pstmt.setInt(2, productID);

                int rowsAffected = pstmt.executeUpdate();

                System.out.println("Rows updated: " + rowsAffected);
                return rowsAffected > 0;

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean insertSale(int productID, int quantitySold, String saleDate, double unitPrice) {
        try (Connection con = DbConnection.ConnectionDB();
                PreparedStatement pstmt = con.prepareStatement("INSERT INTO Sales (ProductID, QuantitySold, SaleDate, UnitPrice) VALUES (?, ?, ?, ?)")) {

            pstmt.setInt(1, productID);
            pstmt.setInt(2, quantitySold);
            pstmt.setString(3, saleDate);
            pstmt.setDouble(4, unitPrice);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0; // Return true if the insert was successful
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            return false; // Return false if there was an error
        }
    }
    
    //TRANSACTOIN SIDE
    
    public static List<String[]> fetchSalesDataAsList() {
        List<String[]> data = new ArrayList<>();
        try (Connection con = DbConnection.ConnectionDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Sales")) {

            while (rs.next()) {
                data.add(new String[]{
                    String.valueOf(rs.getInt("SaleID")),
                    String.valueOf(rs.getInt("ProductID")),
                    String.valueOf(rs.getInt("QuantitySold")),
                    rs.getString("SaleDate"),
                    String.format("%.2f", rs.getDouble("UnitPrice"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public static double getNetIncomeForCurrentMonth() {
        String query = "SELECT SUM(QuantitySold * UnitPrice) AS TotalIncome " +
                       "FROM Sales " +
                       "WHERE strftime('%Y-%m', SaleDate) = strftime('%Y-%m', 'now')";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalIncome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Default to 0 if query fails
    }
    
    public static double getNetIncomeForMonth(int year, int month) {
        double netIncome = 0.0;
        String sql = "SELECT SUM(UnitPrice * QuantitySold) AS NetIncome " +
                     "FROM Sales " +
                     "WHERE strftime('%Y', SaleDate) = ? AND strftime('%m', SaleDate) = ?";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, String.valueOf(year));
            pstmt.setString(2, String.format("%02d", month));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                netIncome = rs.getDouble("NetIncome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return netIncome;
    }
    
    public static double getNetIncomeForCurrentDay() {
        String query = "SELECT SUM(QuantitySold * UnitPrice) AS TotalIncome " +
                       "FROM Sales " +
                       "WHERE DATE(SaleDate) = DATE('now', 'localtime')";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalIncome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Default to 0 if query fails
    }
    
    public static double getNetIncomeForDate(String date) {
        double netIncome = 0.0;
        String sql = "SELECT SUM(UnitPrice * QuantitySold) AS NetIncome " +
                     "FROM Sales " +
                     "WHERE SaleDate = ?";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, date);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                netIncome = rs.getDouble("NetIncome");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return netIncome;
    }
    
    public static int getTotalProducts() {
        String query = "SELECT count(ProductID) AS TotalProducts FROM Products";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TotalProducts");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if query fails
    }
    
    public static int getTotalProductsForDate(String currentDate) {
        int totalProduct = 0;
        String sql = "SELECT count(ProductID) AS TotalProduct " +
                     "FROM Products " +
                     "WHERE ExpirationDate > ?";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, currentDate);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totalProduct = rs.getInt("TotalProduct");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalProduct;
    }
    
    public static int getTotalTransaction() {
        String query = "SELECT count(SaleID) AS TotalTransactions FROM Sales";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TotalTransactions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if query fails
    }
    
    public static int getTotalTransactionsForMonth(String yearMonth) {
        String query = "SELECT COUNT(SaleID) AS TotalTransactions " +
                       "FROM Sales " +
                       "WHERE strftime('%Y-%m', SaleDate) = ?";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("TotalTransactions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if query fails
    }
    
    public static int getTotalProductSold() {
        String query = "SELECT sum(QuantitySold) AS TotalProductsSold FROM Sales WHERE strftime('%Y-%m', SaleDate) = strftime('%Y-%m', 'now')";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TotalProductsSold");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if query fails
    }
    
    public static int getTotalProductsSoldForMonth(String yearMonth) {
        String query = "SELECT SUM(QuantitySold) AS TotalProductsSold " +
                       "FROM Sales " +
                       "WHERE strftime('%Y-%m', SaleDate) = ?";

        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("TotalProductsSold");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Default to 0 if query fails
    }
    
    public static List<Double> getOverallSalesData() {
        List<Double> salesData = new ArrayList<>();
        try (Connection conn = DbConnection.ConnectionDB()) {
            String query = "SELECT SaleDate, SUM(QuantitySold * UnitPrice) AS TotalSales FROM Sales GROUP BY SaleDate ORDER BY SaleDate ASC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double totalSales = rs.getDouble("TotalSales");
                salesData.add(totalSales);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesData;
    }
    public static double getOverallSales() {
        String query = "SELECT SaleDate, SUM(QuantitySold * UnitPrice) AS TotalSales FROM Sales";
        
        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("TotalSales");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    
    // INVENTORY SIDE
    
    public static boolean insertProduct(String productName, String productType, double price, String dosage, int stocks, 
                                    String deliveryDate, String manufacturedDate, String expirationDate) {
        String query = "INSERT INTO Products (ProductName, ProductType, Price, Dosage, Stocks, DeliveryDate, ManufacturedDate, ExpirationDate) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            // Set parameters
            pstmt.setString(1, productName);
            pstmt.setString(2, productType);
            pstmt.setDouble(3, price);
            pstmt.setString(4, dosage);
            pstmt.setInt(5, stocks);
            pstmt.setString(6, deliveryDate);
            pstmt.setString(7, manufacturedDate);
            pstmt.setString(8, expirationDate);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0; // Return true if the insert was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if there's an error
        }
    }
    
    
    
   public static boolean updateProduct(int productID, String productName, String productType, double price, String dosage, int stocks, 
                                    String deliveryDate, String manufacturedDate, String expirationDate) {
        String query = "UPDATE Products SET ProductName = ?, ProductType = ?, Price = ?, Dosage = ?, Stocks = ?, "
                     + "DeliveryDate = ?, ManufacturedDate = ?, ExpirationDate = ? WHERE ProductID = ?";
        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            // Set parameters
            pstmt.setString(1, productName);
            pstmt.setString(2, productType);
            pstmt.setDouble(3, price);
            pstmt.setString(4, dosage);
            pstmt.setInt(5, stocks);
            pstmt.setString(6, deliveryDate);
            pstmt.setString(7, manufacturedDate);
            pstmt.setString(8, expirationDate);
            pstmt.setInt(9, productID); // Where clause identifies the product to update

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; // Return true if the update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Return false if there's an error
        }
    }

    public static Object[] getProductById(int productID) {
        String query = "SELECT ProductName, ProductType, Price, Dosage, Stocks, DeliveryDate, ManufacturedDate, ExpirationDate "
                     + "FROM Products WHERE ProductID = ?";
        try (Connection con = DbConnection.ConnectionDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, productID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getString("ProductName"),
                        rs.getString("ProductType"),
                        rs.getDouble("Price"),
                        rs.getString("Dosage"),
                        rs.getInt("Stocks"),
                        rs.getString("DeliveryDate"),
                        rs.getString("ManufacturedDate"),
                        rs.getString("ExpirationDate")
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no product is found
    }
    
    
    
    
}
