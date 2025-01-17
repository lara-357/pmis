/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package components;

import pmis.POSManagerForm;

/**
 *
 * @author Dell
 */
public class card extends javax.swing.JPanel implements StockUpdateListener {
public int productID;
    /**
     * Creates new form card
     */
    public card(POSManagerForm parentForm, int productID) {
        this.parent = parentForm;   
        this.productID = productID;
        initComponents();
    }
    
    private POSManagerForm parent;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productName = new javax.swing.JLabel();
        productType = new javax.swing.JLabel();
        stocksLbl = new javax.swing.JLabel();
        priceLbl = new javax.swing.JLabel();
        addItemBtn = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setForeground(new java.awt.Color(255, 255, 255));

        productName.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        productName.setText("Product Name");

        productType.setText("Product Type: ");

        stocksLbl.setText("Stocks: ");

        priceLbl.setText("Price");

        addItemBtn.setBackground(new java.awt.Color(38, 41, 207));
        addItemBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        addItemBtn.setForeground(new java.awt.Color(255, 255, 255));
        addItemBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/add_1.png"))); // NOI18N
        addItemBtn.setText("Add Item");
        addItemBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(priceLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                        .addComponent(addItemBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(productName)
                            .addComponent(productType)
                            .addComponent(stocksLbl))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(productName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(productType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stocksLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(priceLbl)
                    .addComponent(addItemBtn))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addItemBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemBtnActionPerformed
        // TODO add your handling code here:
                if (parent != null) {
            parent.loadItems(productID);
        }

    }//GEN-LAST:event_addItemBtnActionPerformed


    public void setProductData(int productID, String name, String prdctType, int stocks, double price) {
        this.productID = productID;
        productName.setText(name);
        productType.setText("Product type: " + prdctType);
        stocksLbl.setText("Stocks: " + stocks);
        priceLbl.setText(price + " PHP");
    }
    
    @Override
    public void onStockUpdated(int productID, int newStock) {
        if (this.productID == productID) {
            stocksLbl.setText("Stocks: " + newStock);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addItemBtn;
    private javax.swing.JLabel priceLbl;
    private javax.swing.JLabel productName;
    private javax.swing.JLabel productType;
    private javax.swing.JLabel stocksLbl;
    // End of variables declaration//GEN-END:variables
}
