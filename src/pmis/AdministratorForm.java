/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pmis;

import components.DatabaseHelper;
import components.DatabaseInventoryHelper;
import components.DatabaseInventoryHelper.Product;
import components.MostSellingProduct;
import components.inventoryItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.List;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;

/**
 *
 * @author Dell
 */
public class AdministratorForm extends javax.swing.JFrame {

    /**
     * Creates new form AdministratorForm
     */
    public AdministratorForm() throws SQLException, ParseException {
        initComponents();
        setupSearchFieldPlaceholder();
        updateNetIncomeLabel();
        updateCurvedLineChart();
        loadMostSellingProducts();
        loadProducts("");
        
        SearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    updateProducts();
                } catch (ParseException ex) {
                    Logger.getLogger(AdministratorForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    updateProducts();
                } catch (ParseException ex) {
                    Logger.getLogger(AdministratorForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    updateProducts();
                } catch (ParseException ex) {
                    Logger.getLogger(AdministratorForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            private void updateProducts() throws ParseException {
            String searchTerm = SearchField.getText();
            loadProducts(searchTerm);
        }
        });
    }
    
    private void updateNetIncomeLabel() {
        double netIncome = DatabaseHelper.getNetIncomeForCurrentMonth();
        double dailynetIncome = DatabaseHelper.getNetIncomeForCurrentDay();
        int totalProducts = DatabaseHelper.getTotalProducts();
        int totalTransactions = DatabaseHelper.getTotalTransaction();
        int totalProductsSold = DatabaseHelper.getTotalProductSold();
        double overAllSales = DatabaseHelper.getOverallSales();
        
        netIncomeLbl.setText(String.format("₱%.2f", netIncome));
        dailySalesLbl.setText(String.format("₱%.2f", dailynetIncome));
        totalProductsLbl.setText(String.format("%d", totalProducts));
        totalTransactionLbl.setText(String.format("%d", totalTransactions));
        productSoldLbl.setText(String.format("%d", totalProductsSold));
        overAllSalesLbl.setText(String.format("Overall Sales: ₱%.2f", overAllSales));
        
        updateIncomeComparisonLabel();
        updateDailyIncomeComparisonLabel();
    }
    
    private void updateIncomeComparisonLabel() {
        // Get current date
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentYear = calendar.get(java.util.Calendar.YEAR);
        int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // Months are 0-based

        // Get net income for current and last month
        double currentMonthIncome = DatabaseHelper.getNetIncomeForMonth(currentYear, currentMonth);
        double lastMonthIncome = DatabaseHelper.getNetIncomeForMonth(
            currentMonth == 1 ? currentYear - 1 : currentYear, // Go to last year if current month is January
            currentMonth == 1 ? 12 : currentMonth - 1          // Go to December if current month is January
        );

        // Calculate difference
        double difference = currentMonthIncome - lastMonthIncome;

        // Update label
        String comparison = difference > 0 ? "higher" : "lower";
        incomeComparisonLbl.setText(String.format(
            "<html><strong>₱%.2f %s</strong> than last month.</html>", Math.abs(difference), comparison
        ));
    }
    
    private void updateDailyIncomeComparisonLabel() {
        // Get current and yesterday's date
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);

        // Fetch net income for today and yesterday
        double todayIncome = DatabaseHelper.getNetIncomeForDate(today.toString());
        double yesterdayIncome = DatabaseHelper.getNetIncomeForDate(yesterday.toString());

        // Calculate the difference
        double difference = todayIncome - yesterdayIncome;

        // Update the label
        String comparison = difference > 0 ? "higher" : "lower";
        dailyComparisonLabel.setText(String.format(
            "<html><strong>₱%.2f %s</strong> than yesterday.</html>", Math.abs(difference), comparison
        ));

        int todayProducts = DatabaseHelper.getTotalProductsForDate(today.toString());
        int yesterdayProducts = DatabaseHelper.getTotalProductsForDate(yesterday.toString());

        // Calculate the product difference
        int productDifference = todayProducts - yesterdayProducts;
        String productComparison = productDifference > 0 ? "more" : "fewer";

        // Update the product comparison label
        nonexpiredLbl.setText(String.format(
            "<html><strong>%d %s</strong> than yesterday.</html>", 
            Math.abs(productDifference), productComparison
        ));

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.YearMonth currentMonth = java.time.YearMonth.from(now);
        java.time.YearMonth lastMonth = currentMonth.minusMonths(1);

        // Fetch transaction counts for current and last month
        int currentMonthTransactions = DatabaseHelper.getTotalTransactionsForMonth(currentMonth.toString());
        int lastMonthTransactions = DatabaseHelper.getTotalTransactionsForMonth(lastMonth.toString());

        // Calculate the difference
        int transactionDifference = currentMonthTransactions - lastMonthTransactions;

        // Determine if it's higher or lower
        String transactionComparison = transactionDifference > 0 ? "higher" : "lower";

        // Update the label
        monthlyComparisonLbl.setText(String.format(
            "<html><strong>%d %s</strong> than last month.<br>",
            Math.abs(transactionDifference), transactionComparison, currentMonthTransactions, lastMonthTransactions
        ));

        int currentMonthProductsSold = DatabaseHelper.getTotalProductsSoldForMonth(currentMonth.toString());
        int lastMonthProductsSold = DatabaseHelper.getTotalProductsSoldForMonth(lastMonth.toString());

        // Calculate the difference
        productDifference = currentMonthProductsSold - lastMonthProductsSold;

        // Determine if it's higher or lower
        String monthlyProductComparison = productDifference > 0 ? "higher" : "lower";

        // Update the label
        monthlyProductComparisonLabel.setText(String.format(
            "<html><strong>%d %s</strong> than last month.<br>",
            Math.abs(productDifference), monthlyProductComparison, currentMonthProductsSold, lastMonthProductsSold
        ));
    }
    private void updateCurvedLineChart() throws SQLException {
        try {
            List<SalesData> data = SalesDataFetcher.getSalesData();

            if (data.isEmpty()) {
                System.out.println("No sales data available.");
                return;
            }

            System.out.println("Loaded sales data: " + data.size());

            curvedLineChartPanel.removeAll();
            CurvedLineChart chart = new CurvedLineChart(data);

            curvedLineChartPanel.add(chart, java.awt.BorderLayout.CENTER);
            curvedLineChartPanel.validate();
            curvedLineChartPanel.repaint();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }
    
    private void setupSearchFieldPlaceholder() {
    SearchField.setText("Search product"); // Initial placeholder text
    SearchField.setForeground(Color.GRAY); // Placeholder color

    // Add focus listener for placeholder behavior
    SearchField.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (SearchField.getText().equals("Search product")) {
                SearchField.setText(""); // Clear placeholder
                SearchField.setForeground(Color.BLACK); // Normal text color
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (SearchField.getText().isEmpty()) {
                SearchField.setText("Search product"); // Restore placeholder
                SearchField.setForeground(Color.GRAY); // Placeholder color
            }
        }
    });
}

    private void loadMostSellingProducts() {
    mostSellingProdPanel.removeAll();

    try (Connection conn = DbConnection.ConnectionDB();
         PreparedStatement stmt = conn.prepareStatement(
            "SELECT p.ProductName, SUM(s.QuantitySold) AS TotalQuantitySold " +
            "FROM Sales s " +
            "JOIN Products p ON s.ProductID = p.ProductID " +
            "GROUP BY p.ProductID, p.ProductName " +
            "ORDER BY TotalQuantitySold DESC " +
            "LIMIT 5"
         );
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String productName = rs.getString("ProductName");
            int totalQuantitySold = rs.getInt("TotalQuantitySold");

            MostSellingProduct productPanel = new MostSellingProduct();
            productPanel.setProductData(productName, totalQuantitySold);
            mostSellingProdPanel.add(productPanel);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    mostSellingProdPanel.revalidate();
    mostSellingProdPanel.repaint();
}
    
    //INVENTORY SIDE
    private void loadProducts(String searchTerm) throws ParseException {
        inventoryPanel.removeAll();
        List<Product> products = DatabaseInventoryHelper.fetchInventoryItems();

        for (Product product : products) {
            if (searchTerm.isEmpty() || product.name.toLowerCase().contains(searchTerm.toLowerCase())) {
                inventoryItem CardPanel = new inventoryItem();

                try {
                    // Convert Date to String (simple format)
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    String deliveryDateStr = product.deliveryDate != null ? dateFormat.format(product.deliveryDate) : null;
                    String manufacturedDateStr = product.manufacturedDate != null ? dateFormat.format(product.manufacturedDate) : null;
                    String expirationDateStr = product.expirationDate != null ? dateFormat.format(product.expirationDate) : null;

                    CardPanel.setProductData(
                        product.name,
                        product.productID,
                        product.prdctType,
                        product.price,
                        product.stocks,
                        deliveryDateStr,
                        manufacturedDateStr,
                        expirationDateStr
                    );

                    inventoryPanel.add(CardPanel);
                    // addStockUpdateListener(CardPanel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }






    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        dashboardBtn = new javax.swing.JButton();
        transactionsBtn = new javax.swing.JButton();
        inventoryBtn = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        notificationBtn = new javax.swing.JButton();
        settingsBtn = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        DashboardPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        mainDashboard = new javax.swing.JPanel();
        netIncomePanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        netIncomeLbl = new javax.swing.JLabel();
        incomeComparisonLbl = new javax.swing.JLabel();
        dailySalesPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        dailySalesLbl = new javax.swing.JLabel();
        dailyComparisonLabel = new javax.swing.JLabel();
        totalProductsPanel = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        totalProductsLbl = new javax.swing.JLabel();
        nonexpiredLbl = new javax.swing.JLabel();
        totalTransactionPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        totalTransactionLbl = new javax.swing.JLabel();
        monthlyComparisonLbl = new javax.swing.JLabel();
        productsSoldPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        productSoldLbl = new javax.swing.JLabel();
        monthlyProductComparisonLabel = new javax.swing.JLabel();
        curvedLineChartPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mostSellingProdPanel = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        overAllSalesLbl = new javax.swing.JLabel();
        TransactionPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        InventoryPanel = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        inventoryPanel = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        SearchField = new javax.swing.JTextField();
        addItemBtn = new javax.swing.JButton();
        NotificationPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        SettingPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(153, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 19)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ASP-LOGO-FINAL 1@2x.png"))); // NOI18N

        dashboardBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        dashboardBtn.setForeground(new java.awt.Color(12, 18, 66));
        dashboardBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/dash.png"))); // NOI18N
        dashboardBtn.setText("Dashboard");
        dashboardBtn.setAutoscrolls(true);
        dashboardBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        dashboardBtn.setContentAreaFilled(false);
        dashboardBtn.setMargin(new java.awt.Insets(10, 100, 3, 50));
        dashboardBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardBtnActionPerformed(evt);
            }
        });

        transactionsBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        transactionsBtn.setForeground(new java.awt.Color(12, 18, 66));
        transactionsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/trans.png"))); // NOI18N
        transactionsBtn.setText("Transactions");
        transactionsBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        transactionsBtn.setContentAreaFilled(false);
        transactionsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transactionsBtnActionPerformed(evt);
            }
        });

        inventoryBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        inventoryBtn.setForeground(new java.awt.Color(12, 18, 66));
        inventoryBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/inv.png"))); // NOI18N
        inventoryBtn.setText("Inventory");
        inventoryBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        inventoryBtn.setContentAreaFilled(false);
        inventoryBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inventoryBtnActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(12, 18, 66));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/log.png"))); // NOI18N
        jButton2.setText("Log out");
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        notificationBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        notificationBtn.setForeground(new java.awt.Color(12, 18, 66));
        notificationBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/notif.png"))); // NOI18N
        notificationBtn.setText("Notification");
        notificationBtn.setBorder(null);
        notificationBtn.setContentAreaFilled(false);
        notificationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notificationBtnActionPerformed(evt);
            }
        });

        settingsBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        settingsBtn.setForeground(new java.awt.Color(12, 18, 66));
        settingsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/set.png"))); // NOI18N
        settingsBtn.setText("Settings");
        settingsBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dashboardBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transactionsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(inventoryBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(notificationBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(settingsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addGap(20, 20, 20)
                .addComponent(dashboardBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transactionsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inventoryBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notificationBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 117, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 210, 720));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(12, 18, 66));
        jLabel7.setText("Dashboard");

        jButton1.setForeground(new java.awt.Color(12, 18, 66));
        jButton1.setText("Notif");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel7)
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1064, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        mainDashboard.setBackground(new java.awt.Color(255, 255, 255));

        netIncomePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        netIncomePanel.setPreferredSize(new java.awt.Dimension(193, 121));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("NET INCOME");
        jLabel1.setToolTipText("");

        netIncomeLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        netIncomeLbl.setForeground(new java.awt.Color(51, 51, 51));
        netIncomeLbl.setText("jLabel5");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(netIncomeLbl))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netIncomeLbl)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        incomeComparisonLbl.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        incomeComparisonLbl.setText("jLabel14");

        javax.swing.GroupLayout netIncomePanelLayout = new javax.swing.GroupLayout(netIncomePanel);
        netIncomePanel.setLayout(netIncomePanelLayout);
        netIncomePanelLayout.setHorizontalGroup(
            netIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(netIncomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(netIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(netIncomePanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(incomeComparisonLbl)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        netIncomePanelLayout.setVerticalGroup(
            netIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(netIncomePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(incomeComparisonLbl)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dailySalesPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        dailySalesPanel.setPreferredSize(new java.awt.Dimension(193, 100));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jLabel6.setText("DAILY SALES");
        jLabel6.setToolTipText("");

        dailySalesLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        dailySalesLbl.setForeground(new java.awt.Color(51, 51, 51));
        dailySalesLbl.setText("jLabel5");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(dailySalesLbl))
                .addContainerGap(106, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dailySalesLbl)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        dailyComparisonLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        dailyComparisonLabel.setText("jLabel15");

        javax.swing.GroupLayout dailySalesPanelLayout = new javax.swing.GroupLayout(dailySalesPanel);
        dailySalesPanel.setLayout(dailySalesPanelLayout);
        dailySalesPanelLayout.setHorizontalGroup(
            dailySalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dailySalesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dailySalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dailySalesPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(dailyComparisonLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        dailySalesPanelLayout.setVerticalGroup(
            dailySalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dailySalesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dailyComparisonLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        totalProductsPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        totalProductsPanel.setPreferredSize(new java.awt.Dimension(193, 100));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setText("TOTAL PRODUCT");
        jLabel8.setToolTipText("");

        totalProductsLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        totalProductsLbl.setForeground(new java.awt.Color(51, 51, 51));
        totalProductsLbl.setText("jLabel5");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(totalProductsLbl))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalProductsLbl)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        nonexpiredLbl.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        nonexpiredLbl.setText("jLabel16");

        javax.swing.GroupLayout totalProductsPanelLayout = new javax.swing.GroupLayout(totalProductsPanel);
        totalProductsPanel.setLayout(totalProductsPanelLayout);
        totalProductsPanelLayout.setHorizontalGroup(
            totalProductsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalProductsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(totalProductsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(totalProductsPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(nonexpiredLbl)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        totalProductsPanelLayout.setVerticalGroup(
            totalProductsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalProductsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nonexpiredLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        totalTransactionPanel.setForeground(new java.awt.Color(204, 204, 204));
        totalTransactionPanel.setPreferredSize(new java.awt.Dimension(183, 100));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setText("TOTAL TRANSACTIONS");
        jLabel10.setToolTipText("");

        totalTransactionLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        totalTransactionLbl.setForeground(new java.awt.Color(51, 51, 51));
        totalTransactionLbl.setText("jLabel5");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(totalTransactionLbl))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalTransactionLbl)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        monthlyComparisonLbl.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        monthlyComparisonLbl.setText("jLabel17");

        javax.swing.GroupLayout totalTransactionPanelLayout = new javax.swing.GroupLayout(totalTransactionPanel);
        totalTransactionPanel.setLayout(totalTransactionPanelLayout);
        totalTransactionPanelLayout.setHorizontalGroup(
            totalTransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalTransactionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(totalTransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(totalTransactionPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(monthlyComparisonLbl)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        totalTransactionPanelLayout.setVerticalGroup(
            totalTransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(totalTransactionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthlyComparisonLbl)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        productsSoldPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        productsSoldPanel.setPreferredSize(new java.awt.Dimension(193, 100));

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setText("PRODUCTS SOLD");
        jLabel12.setToolTipText("");

        productSoldLbl.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        productSoldLbl.setText("jLabel5");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(productSoldLbl))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productSoldLbl)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        monthlyProductComparisonLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        monthlyProductComparisonLabel.setText("jLabel18");

        javax.swing.GroupLayout productsSoldPanelLayout = new javax.swing.GroupLayout(productsSoldPanel);
        productsSoldPanel.setLayout(productsSoldPanelLayout);
        productsSoldPanelLayout.setHorizontalGroup(
            productsSoldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productsSoldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(productsSoldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(productsSoldPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(monthlyProductComparisonLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        productsSoldPanelLayout.setVerticalGroup(
            productsSoldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(productsSoldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(monthlyProductComparisonLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        curvedLineChartPanel.setBackground(new java.awt.Color(12, 18, 66));
        curvedLineChartPanel.setLayout(new java.awt.BorderLayout());

        jPanel11.setBackground(new java.awt.Color(255, 255, 255));
        jPanel11.setPreferredSize(new java.awt.Dimension(183, 43));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(51, 51, 51));
        jLabel5.setText("Most Selling Products");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.LINE_AXIS));

        mostSellingProdPanel.setBackground(new java.awt.Color(204, 204, 204));
        mostSellingProdPanel.setLayout(new java.awt.GridLayout(5, 1, 2, 0));
        jScrollPane1.setViewportView(mostSellingProdPanel);

        jPanel12.add(jScrollPane1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(12, 18, 66));

        overAllSalesLbl.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        overAllSalesLbl.setForeground(new java.awt.Color(255, 255, 255));
        overAllSalesLbl.setText("OVERALL SALES:");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(overAllSalesLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(411, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(overAllSalesLbl)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainDashboardLayout = new javax.swing.GroupLayout(mainDashboard);
        mainDashboard.setLayout(mainDashboardLayout);
        mainDashboardLayout.setHorizontalGroup(
            mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainDashboardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainDashboardLayout.createSequentialGroup()
                        .addComponent(netIncomePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(dailySalesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(totalProductsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(totalTransactionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(productsSoldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainDashboardLayout.createSequentialGroup()
                        .addGroup(mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(curvedLineChartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mainDashboardLayout.setVerticalGroup(
            mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainDashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(totalTransactionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(totalProductsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(dailySalesPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(netIncomePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(productsSoldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(mainDashboardLayout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(curvedLineChartPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout DashboardPanelLayout = new javax.swing.GroupLayout(DashboardPanel);
        DashboardPanel.setLayout(DashboardPanelLayout);
        DashboardPanelLayout.setHorizontalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        DashboardPanelLayout.setVerticalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainDashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("tab1", DashboardPanel);

        jLabel2.setText("transaction");

        javax.swing.GroupLayout TransactionPanelLayout = new javax.swing.GroupLayout(TransactionPanel);
        TransactionPanel.setLayout(TransactionPanelLayout);
        TransactionPanelLayout.setHorizontalGroup(
            TransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TransactionPanelLayout.createSequentialGroup()
                .addGap(451, 451, 451)
                .addComponent(jLabel2)
                .addContainerGap(560, Short.MAX_VALUE))
        );
        TransactionPanelLayout.setVerticalGroup(
            TransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TransactionPanelLayout.createSequentialGroup()
                .addGap(208, 208, 208)
                .addComponent(jLabel2)
                .addContainerGap(501, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("tab2", TransactionPanel);

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(12, 18, 66));
        jLabel3.setText("Inventory");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(19, 19, 19))
        );

        jPanel15.setLayout(new javax.swing.BoxLayout(jPanel15, javax.swing.BoxLayout.LINE_AXIS));

        inventoryPanel.setLayout(new java.awt.GridLayout(0, 4, 15, 20));
        jScrollPane2.setViewportView(inventoryPanel);

        jPanel15.add(jScrollPane2);

        jPanel16.setBackground(new java.awt.Color(255, 255, 255));

        SearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchFieldActionPerformed(evt);
            }
        });

        addItemBtn.setBackground(new java.awt.Color(12, 18, 66));
        addItemBtn.setForeground(new java.awt.Color(255, 255, 255));
        addItemBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/add_1.png"))); // NOI18N
        addItemBtn.setText("Add Item");
        addItemBtn.setBorder(null);
        addItemBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 675, Short.MAX_VALUE)
                .addComponent(addItemBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addItemBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout InventoryPanelLayout = new javax.swing.GroupLayout(InventoryPanel);
        InventoryPanel.setLayout(InventoryPanelLayout);
        InventoryPanelLayout.setHorizontalGroup(
            InventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(InventoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        InventoryPanelLayout.setVerticalGroup(
            InventoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(InventoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("tab3", InventoryPanel);

        jLabel4.setText("notification");

        javax.swing.GroupLayout NotificationPanelLayout = new javax.swing.GroupLayout(NotificationPanel);
        NotificationPanel.setLayout(NotificationPanelLayout);
        NotificationPanelLayout.setHorizontalGroup(
            NotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotificationPanelLayout.createSequentialGroup()
                .addGap(445, 445, 445)
                .addComponent(jLabel4)
                .addContainerGap(564, Short.MAX_VALUE))
        );
        NotificationPanelLayout.setVerticalGroup(
            NotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotificationPanelLayout.createSequentialGroup()
                .addGap(216, 216, 216)
                .addComponent(jLabel4)
                .addContainerGap(493, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("tab4", NotificationPanel);

        javax.swing.GroupLayout SettingPanelLayout = new javax.swing.GroupLayout(SettingPanel);
        SettingPanel.setLayout(SettingPanelLayout);
        SettingPanelLayout.setHorizontalGroup(
            SettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1070, Short.MAX_VALUE)
        );
        SettingPanelLayout.setVerticalGroup(
            SettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 725, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("tab5", SettingPanel);

        getContentPane().add(jTabbedPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, -35, 1070, 760));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(0);
    }//GEN-LAST:event_dashboardBtnActionPerformed

    private void transactionsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transactionsBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(1);
        //loadTransactionTable();
    }//GEN-LAST:event_transactionsBtnActionPerformed

    private void inventoryBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inventoryBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(2);
    }//GEN-LAST:event_inventoryBtnActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        RoleForm roleForm = new RoleForm();
        roleForm.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void notificationBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notificationBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(3);
    }//GEN-LAST:event_notificationBtnActionPerformed

    private void settingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane2.setSelectedIndex(4);
    }//GEN-LAST:event_settingsBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void SearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SearchFieldActionPerformed

    private void addItemBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemBtnActionPerformed
        // TODO add your handling code here:
        AddNewItem addNewItem = new AddNewItem();
        addNewItem.setVisible(true);
    }//GEN-LAST:event_addItemBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdministratorForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdministratorForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdministratorForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdministratorForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new AdministratorForm().setVisible(true);
                } catch (SQLException ex) {
                    Logger.getLogger(AdministratorForm.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(AdministratorForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel DashboardPanel;
    private javax.swing.JPanel InventoryPanel;
    private javax.swing.JPanel NotificationPanel;
    private javax.swing.JTextField SearchField;
    private javax.swing.JPanel SettingPanel;
    private javax.swing.JPanel TransactionPanel;
    private javax.swing.JButton addItemBtn;
    private javax.swing.JPanel curvedLineChartPanel;
    private javax.swing.JLabel dailyComparisonLabel;
    private javax.swing.JLabel dailySalesLbl;
    private javax.swing.JPanel dailySalesPanel;
    private javax.swing.JButton dashboardBtn;
    private javax.swing.JLabel incomeComparisonLbl;
    private javax.swing.JButton inventoryBtn;
    private javax.swing.JPanel inventoryPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JPanel mainDashboard;
    private javax.swing.JLabel monthlyComparisonLbl;
    private javax.swing.JLabel monthlyProductComparisonLabel;
    private javax.swing.JPanel mostSellingProdPanel;
    private javax.swing.JLabel netIncomeLbl;
    private javax.swing.JPanel netIncomePanel;
    private javax.swing.JLabel nonexpiredLbl;
    private javax.swing.JButton notificationBtn;
    private javax.swing.JLabel overAllSalesLbl;
    private javax.swing.JLabel productSoldLbl;
    private javax.swing.JPanel productsSoldPanel;
    private javax.swing.JButton settingsBtn;
    private javax.swing.JLabel totalProductsLbl;
    private javax.swing.JPanel totalProductsPanel;
    private javax.swing.JLabel totalTransactionLbl;
    private javax.swing.JPanel totalTransactionPanel;
    private javax.swing.JButton transactionsBtn;
    // End of variables declaration//GEN-END:variables
}
