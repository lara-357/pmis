/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pmis;


import java.awt.Component;
import components.DatabaseHelper;
import components.DatabaseHelper.Product;
import components.POSItem;
import components.POSItem.TotalPriceUpdateListener;
import components.ResultSetTableModel;
import components.card;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;  
import java.sql.*;


/**
 *
 * @author Dell
 */
public class POSManagerForm extends javax.swing.JFrame implements TotalPriceUpdateListener{

    
    /**
     * Creates new form POSManagerForm
     */
    @Override
    public void onTotalPriceUpdated() {
        updateTotalPrice();
    }
    
    
    public POSManagerForm() {
        initComponents();
        setupSearchFieldPlaceholder();
        initDiscountButtons();
        initAmountReceivedListener();
        
        loadProducts("");
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateProducts();
            }
            private void updateProducts() {
            String searchTerm = searchField.getText();
            loadProducts(searchTerm);
        }
        });
        
    }
    
    private Set<components.StockUpdateListener> stockUpdateListeners = new HashSet<>();

    public void addStockUpdateListener(components.StockUpdateListener listener) {
        stockUpdateListeners.add(listener);
    }

    public void removeStockUpdateListener(components.StockUpdateListener listener) {
        stockUpdateListeners.remove(listener);
    }

    private void notifyStockUpdate(int productID, int newStock) {
        for (components.StockUpdateListener listener : stockUpdateListeners) {
            listener.onStockUpdated(productID, newStock);
        }
    }
    
    public void updateStockAndNotify(int productID, int newStock) {
        boolean success = DatabaseHelper.updateProductStock(productID, newStock);
        if (success) {
            notifyStockUpdate(productID, newStock); // Notify here
        }
    }
    
    private void loadProducts(String searchTerm) {
        mainPanel.removeAll(); // Clear previous results
        List<Product> products = DatabaseHelper.fetchProducts(searchTerm);

        for (Product product : products) {
            card CardPanel = new card(this, product.productID);
            CardPanel.setProductData(product.productID, product.name, product.prdctType, product.stocks, product.price);
            mainPanel.add(CardPanel);
            
            addStockUpdateListener(CardPanel);
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private Set<Integer> addedProductIDs = new HashSet<>();
    
    public void loadItems(int productID) {
        if (addedProductIDs.contains(productID)) {
        System.out.println("Product ID " + productID + " is already added.");
        return;
        }

        List<DatabaseHelper.Product> items = DatabaseHelper.fetchItem(productID);

        for (DatabaseHelper.Product item : items) {
            System.out.println("Adding item: " + item.productID);
            POSItem posPanel = new POSItem();
            posPanel.setItemData(item.name, item.price, item.stocks);
            posPanel.setParentPanel(itemPanel);
            posPanel.setProductID(productID);


            posPanel.setRemovalListener(removedID -> {
            itemPanel.remove(posPanel);
            addedProductIDs.remove(removedID);
            itemPanel.revalidate();
            itemPanel.repaint();
            updateTotalPrice();
        });
            posPanel.setTotalPriceUpdateListener(this::onTotalPriceUpdated);
            itemPanel.add(posPanel);
        }
            addedProductIDs.add(productID);
            itemPanel.revalidate();
            itemPanel.repaint();
            updateTotalPrice(); 
    }
    private void updateTotalPrice() {
        double total = 0;
        double totalAmount = 0;
        double change = 0; // Change to give back
        double amountReceived = 0;
        
        try {
            amountReceived = Double.parseDouble(amountReceivedField.getText());
        } catch (NumberFormatException e) {
            amountReceived = 0; // Default to 0 if invalid input
        }

    for (java.awt.Component comp : itemPanel.getComponents()) {
        if (comp instanceof POSItem) {
            POSItem item = (POSItem) comp;
            try {
                int count = item.getCount();
                double price = item.getPrice();
                total += count * price;
                totalAmount += count * price;
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        }
    }
    
    double discount = total * discountRate;
    double totalfinal = totalAmount - discount;
    change = amountReceived - totalfinal;
    totalLbl.setText(String.format("Total: ₱%.2f", total));
    totalAmountLbl.setText(String.format("Total: ₱%.2f", totalfinal));
    changeLbl.setText(String.format("Change: ₱%.2f", change));
    
    if (change <= 0){
        changeLbl.setText(String.format("Change: ₱0.00"));
    }else if(change >= 1){
        changeLbl.setText(String.format("Change: ₱%.2f", change));
    }
    }
    
    private double discountRate = 0;
    private void initDiscountButtons(){
        regularRbtn.addActionListener(e -> setDiscountRate(0));
        seniorRbtn.addActionListener(e -> setDiscountRate(0.10));
        pwdRbtn.addActionListener(e -> setDiscountRate(0.10));
    }
    
    private void setDiscountRate(double rate) {
        discountRate = rate;
        updateTotalPrice(); // Recalculate total with the discount
    }
    private void initAmountReceivedListener() {
        amountReceivedField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTotalPrice();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTotalPrice();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTotalPrice();
            }
        });
    }
    
    
    private void setupSearchFieldPlaceholder() {
    searchField.setText("Search product"); // Initial placeholder text
    searchField.setForeground(Color.GRAY); // Placeholder color

    // Add focus listener for placeholder behavior
    searchField.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            if (searchField.getText().equals("Search product")) {
                searchField.setText(""); // Clear placeholder
                searchField.setForeground(Color.BLACK); // Normal text color
            }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            if (searchField.getText().isEmpty()) {
                searchField.setText("Search product"); // Restore placeholder
                searchField.setForeground(Color.GRAY); // Placeholder color
            }
        }
    });
}
    
    //TRANSACTION PANEL SIDE
    
    private void loadTransactionTable() {
    List<String[]> salesData = DatabaseHelper.fetchSalesDataAsList();
    ResultSetTableModel tableModel = new ResultSetTableModel(salesData);
    transactionTable.setModel(tableModel);
    }

    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        posBtn = new javax.swing.JButton();
        transactionsBtn = new javax.swing.JButton();
        settingsBtn = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        POSPanel = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        itemPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        paymentPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        proceedBtn = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        totalLbl = new javax.swing.JLabel();
        changeLbl = new javax.swing.JLabel();
        regularRbtn = new javax.swing.JRadioButton();
        seniorRbtn = new javax.swing.JRadioButton();
        pwdRbtn = new javax.swing.JRadioButton();
        totalAmountLbl = new javax.swing.JLabel();
        amountReceivedField = new javax.swing.JTextField();
        TransactionPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        transactionTable = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 204, 255));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 19)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ASP-LOGO-FINAL 1@2x.png"))); // NOI18N

        posBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        posBtn.setForeground(new java.awt.Color(12, 18, 66));
        posBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/pos.png"))); // NOI18N
        posBtn.setText("POS");
        posBtn.setBorder(null);
        posBtn.setContentAreaFilled(false);
        posBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posBtnActionPerformed(evt);
            }
        });

        transactionsBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        transactionsBtn.setForeground(new java.awt.Color(12, 18, 66));
        transactionsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/trans.png"))); // NOI18N
        transactionsBtn.setText("Transactions");
        transactionsBtn.setBorder(null);
        transactionsBtn.setContentAreaFilled(false);
        transactionsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transactionsBtnActionPerformed(evt);
            }
        });

        settingsBtn.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        settingsBtn.setForeground(new java.awt.Color(12, 18, 66));
        settingsBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/set.png"))); // NOI18N
        settingsBtn.setText("Settings");
        settingsBtn.setBorder(null);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsBtnActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(12, 18, 66));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/log.png"))); // NOI18N
        jButton2.setText("Log out");
        jButton2.setBorder(null);
        jButton2.setContentAreaFilled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(posBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transactionsBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(settingsBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addGap(20, 20, 20)
                .addComponent(posBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transactionsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 289, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 210, 720));

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1050, 760));

        POSPanel.setBackground(new java.awt.Color(238, 238, 238));

        jPanel13.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(12, 18, 66));
        jLabel3.setText("Point of Sales");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(19, 19, 19))
        );

        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        mainPanel.setBackground(new java.awt.Color(238, 238, 238));
        mainPanel.setLayout(new java.awt.GridLayout(15, 4, 5, 5));
        jScrollPane1.setViewportView(mainPanel);

        jPanel2.add(jScrollPane1);

        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        itemPanel.setLayout(new java.awt.GridLayout(10, 1));
        jScrollPane2.setViewportView(itemPanel);

        jPanel8.add(jScrollPane2);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 104, Short.MAX_VALUE)
        );

        jLabel1.setText("Subtotal:");

        jLabel5.setText("Discount:");

        jLabel6.setText("Total Amount:");

        jButton1.setBackground(new java.awt.Color(32, 172, 25));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Print");

        proceedBtn.setBackground(new java.awt.Color(44, 47, 239));
        proceedBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        proceedBtn.setForeground(new java.awt.Color(255, 255, 255));
        proceedBtn.setText("Proceed");
        proceedBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proceedBtnActionPerformed(evt);
            }
        });

        jLabel7.setText("Amount Received:");

        jLabel8.setText("Change:");

        jLabel9.setText("Payment Type:");

        totalLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalLbl.setText("Total: ₱0.00");

        changeLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        changeLbl.setText("₱0.00");

        buttonGroup1.add(regularRbtn);
        regularRbtn.setText("Regular");

        buttonGroup1.add(seniorRbtn);
        seniorRbtn.setText("Senior");
        seniorRbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seniorRbtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(pwdRbtn);
        pwdRbtn.setText("PWD");
        pwdRbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdRbtnActionPerformed(evt);
            }
        });

        totalAmountLbl.setText("₱0.00");

        amountReceivedField.setMinimumSize(new java.awt.Dimension(67, 22));
        amountReceivedField.setPreferredSize(new java.awt.Dimension(67, 22));
        amountReceivedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                amountReceivedFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout paymentPanelLayout = new javax.swing.GroupLayout(paymentPanel);
        paymentPanel.setLayout(paymentPanelLayout);
        paymentPanelLayout.setHorizontalGroup(
            paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paymentPanelLayout.createSequentialGroup()
                .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paymentPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(paymentPanelLayout.createSequentialGroup()
                                .addComponent(regularRbtn)
                                .addGap(18, 18, 18)
                                .addComponent(seniorRbtn)
                                .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(paymentPanelLayout.createSequentialGroup()
                                        .addGap(36, 36, 36)
                                        .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(amountReceivedField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(totalAmountLbl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(paymentPanelLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(pwdRbtn)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(totalLbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(changeLbl, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(paymentPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                        .addComponent(proceedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        paymentPanelLayout.setVerticalGroup(
            paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paymentPanelLayout.createSequentialGroup()
                .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(paymentPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(totalLbl))
                        .addGap(18, 18, 18)
                        .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(regularRbtn)
                            .addComponent(seniorRbtn)
                            .addComponent(pwdRbtn))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                        .addComponent(jLabel9))
                    .addGroup(paymentPanelLayout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(totalAmountLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amountReceivedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeLbl)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proceedBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout POSPanelLayout = new javax.swing.GroupLayout(POSPanel);
        POSPanel.setLayout(POSPanelLayout);
        POSPanelLayout.setHorizontalGroup(
            POSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, POSPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(POSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(POSPanelLayout.createSequentialGroup()
                        .addGroup(POSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 644, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 6, Short.MAX_VALUE))
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(POSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .addComponent(paymentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        POSPanelLayout.setVerticalGroup(
            POSPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(POSPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(POSPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paymentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", POSPanel);

        TransactionPanel.setBackground(new java.awt.Color(238, 238, 238));

        transactionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(transactionTable);

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(12, 18, 66));
        jLabel12.setText("Transactions");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(813, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addGap(19, 19, 19))
        );

        javax.swing.GroupLayout TransactionPanelLayout = new javax.swing.GroupLayout(TransactionPanel);
        TransactionPanel.setLayout(TransactionPanelLayout);
        TransactionPanelLayout.setHorizontalGroup(
            TransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TransactionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(TransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1058, Short.MAX_VALUE))
                .addContainerGap())
        );
        TransactionPanelLayout.setVerticalGroup(
            TransactionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, TransactionPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 167, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 472, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("tab2", TransactionPanel);

        jPanel5.setBackground(new java.awt.Color(238, 238, 238));

        jLabel4.setText("Settings");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(324, 324, 324)
                .addComponent(jLabel4)
                .addContainerGap(704, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(jLabel4)
                .addContainerGap(587, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab4", jPanel5);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, -39, 1070, 760));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void transactionsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transactionsBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(1);
                loadTransactionTable();
    }//GEN-LAST:event_transactionsBtnActionPerformed

    private void settingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_settingsBtnActionPerformed

    private void posBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posBtnActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_posBtnActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchFieldActionPerformed

    private void pwdRbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdRbtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pwdRbtnActionPerformed

    private void seniorRbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seniorRbtnActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_seniorRbtnActionPerformed

    private void amountReceivedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_amountReceivedFieldActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_amountReceivedFieldActionPerformed
    private JPanel getCartPanel() {
        return this.itemPanel;  // Replace 'cartPanel' with your actual panel reference
    }
    private void proceedBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proceedBtnActionPerformed
        // TODO add your handling code here:
        JPanel cartPanel = this.getCartPanel();  // A method to reference your cart panel

        for (Component comp : cartPanel.getComponents()) {
            if (comp instanceof POSItem) {
                POSItem item = (POSItem) comp;

                int productID = item.getProductID();
                int count = item.getCount();
                double unitPrice = item.getPrice();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String saleDate = sdf.format(new Date());
                
                if (count > 0) { // Only insert if quantity is valid
                // Insert into Sales table
                    boolean success = DatabaseHelper.insertSale(productID, count, saleDate, unitPrice);
                    if (!success) {
                        JOptionPane.showMessageDialog(this, "Failed to insert sale for Product ID: " + productID,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                // Get product information from the database to check current stock
                List<DatabaseHelper.Product> productList = DatabaseHelper.fetchItem(productID);
                if (!productList.isEmpty()) {
                    DatabaseHelper.Product product = productList.get(0);

                    int currentStock = product.stocks;
                    int newStockCount = currentStock - count;
                    
                    System.out.println("POSItem ID: " + item.getProductID() + ", Count: " + item.getCount() + ", New Stock: " + newStockCount);

                    if (newStockCount < 0) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock for " + product.name);
                    } else {
                        boolean success = DatabaseHelper.updateProductStock(productID, newStockCount);
                    }
                }
            }
            mainPanel.removeAll();
            mainPanel.revalidate();
            mainPanel.repaint();

            // Reload the products with the current search term
            String searchTerm = searchField.getText();
            loadProducts(searchTerm);
        }
        
    javax.swing.JOptionPane.showMessageDialog(this, "Purchase completed!");
    
    
    }//GEN-LAST:event_proceedBtnActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        RoleForm roleForm = new RoleForm();
        roleForm.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(POSManagerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(POSManagerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(POSManagerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(POSManagerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new POSManagerForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel POSPanel;
    private javax.swing.JPanel TransactionPanel;
    private javax.swing.JTextField amountReceivedField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel changeLbl;
    private javax.swing.JPanel itemPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel paymentPanel;
    private javax.swing.JButton posBtn;
    private javax.swing.JButton proceedBtn;
    private javax.swing.JRadioButton pwdRbtn;
    private javax.swing.JRadioButton regularRbtn;
    private javax.swing.JTextField searchField;
    private javax.swing.JRadioButton seniorRbtn;
    private javax.swing.JButton settingsBtn;
    private javax.swing.JLabel totalAmountLbl;
    private javax.swing.JLabel totalLbl;
    private javax.swing.JTable transactionTable;
    private javax.swing.JButton transactionsBtn;
    // End of variables declaration//GEN-END:variables
}
