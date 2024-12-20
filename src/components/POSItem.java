/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package components;

import javax.swing.JPanel;
import components.ItemRemovalListener;

/**
 *
 * @author Dell
 */
public class POSItem extends javax.swing.JPanel {

    /**
     * Creates new form POSItem
     */
    
    private JPanel parentPanel;
    private int productID; // Store the productID for this POSItem
    private ItemRemovalListener removalListener;
    private double price;
    private int count;
    private int stocks;
    
    private TotalPriceUpdateListener totalPriceListener;
    
    
    public int getProductID() {
        return productID;
    }
    
    public int getCount() {
    try {
        return Integer.parseInt(countField.getText());
    } catch (NumberFormatException e) {
        return 0;
    }
}
    

    public void setTotalPriceUpdateListener(TotalPriceUpdateListener listener) {
    this.totalPriceListener = listener;
    }
    
    private void updateTotalPrice() {
    int quantity = getCount();
    double totalPrice = quantity * price;

    // Notify POSManagerForm about the updated total price
    if (totalPriceListener != null) {
        totalPriceListener.onTotalPriceUpdated();
    }
    }

    public POSItem() {
        initComponents();
        countField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            notifyPriceQuantityChanged();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            notifyPriceQuantityChanged();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            notifyPriceQuantityChanged();
        }
    });
    }
    
    public interface TotalPriceUpdateListener {

        void onTotalPriceUpdated();
    }
    

    public void setPriceQuantityChangeListener(TotalPriceUpdateListener listener) {
        this.totalPriceListener = listener;
        notifyPriceQuantityChanged();
    }

    // Call listener whenever quantity or price changes
    private void notifyPriceQuantityChanged() {
        if (totalPriceListener != null) {
        totalPriceListener.onTotalPriceUpdated();
    }
    }

    public double getPrice() {
        return Double.parseDouble(priceLbl.getText().replace("₱", "").trim());
    }
    
    public void setParentPanel(JPanel parentPanel) {
        this.parentPanel = parentPanel;
    }
    
    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setRemovalListener(ItemRemovalListener removalListener) {
        this.removalListener = removalListener;
    }
     

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        minusBtn = new javax.swing.JButton();
        addBtn = new javax.swing.JButton();
        priceLbl = new javax.swing.JLabel();
        deleteBtn = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        productName = new javax.swing.JLabel();
        countField = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setForeground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(303, 52));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        minusBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        minusBtn.setForeground(new java.awt.Color(51, 102, 255));
        minusBtn.setText("-");
        minusBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 255), 2));
        minusBtn.setContentAreaFilled(false);
        minusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusBtnActionPerformed(evt);
            }
        });
        add(minusBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 15, 17, 17));

        addBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        addBtn.setForeground(new java.awt.Color(32, 172, 25));
        addBtn.setText("+");
        addBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(32, 172, 25), 2));
        addBtn.setContentAreaFilled(false);
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });
        add(addBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 15, 17, 17));

        priceLbl.setText("Price");
        add(priceLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 15, -1, -1));

        deleteBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/del2.png"))); // NOI18N
        deleteBtn.setBorder(null);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBtnActionPerformed(evt);
            }
        });
        add(deleteBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 0, 20, 50));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        productName.setText("ProductName");
        jPanel1.add(productName);

        add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 170, 50));

        countField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countFieldActionPerformed(evt);
            }
        });
        add(countField, new org.netbeans.lib.awtextra.AbsoluteConstraints(234, 12, 30, -1));
    }// </editor-fold>//GEN-END:initComponents

    private void countFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countFieldActionPerformed
        // TODO add your handling code here:
        notifyPriceQuantityChanged();
    updateTotalPrice();
    }//GEN-LAST:event_countFieldActionPerformed

    private void minusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusBtnActionPerformed
        // TODO add your handling code here:
        try {
        int count = getCount();
        if (count > 0) {
            count--;
            countField.setText(String.valueOf(count));
        }
    } catch (NumberFormatException e) {
        countField.setText("0");
    }
    updateTotalPrice();
    }//GEN-LAST:event_minusBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        // TODO add your handling code here:
    try {
        int count = getCount();
        if (count < stocks) { // Ensure the count doesn't exceed available stock
            count++;
            countField.setText(String.valueOf(count));
        } else {
            // Optionally show a warning
            javax.swing.JOptionPane.showMessageDialog(this, "Cannot exceed available stock!");
        }
    } catch (NumberFormatException e) {
        countField.setText("1");
    }
    updateTotalPrice();
    }//GEN-LAST:event_addBtnActionPerformed

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
        // TODO add your handling code here:
        if (parentPanel != null) {
            parentPanel.remove(POSItem.this); // Remove the current POSItem from its parent
            parentPanel.revalidate(); // Revalidate the layout
            parentPanel.repaint(); // Repaint the parent to reflect changes
        }
        if (removalListener != null) {
            removalListener.onItemRemoved(productID); // Notify listener to remove the ID
        }
    }//GEN-LAST:event_deleteBtnActionPerformed

    public void setItemData(String prdctName, double price, int stocks){
        this.price = price; // Store the price
        this.stocks = stocks;
        productName.setText("<html>" + prdctName + "</html>");
        priceLbl.setText("₱" + price);
        this.count = 1;
        countField.setText(String.valueOf(this.count));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JTextField countField;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton minusBtn;
    private javax.swing.JLabel priceLbl;
    private javax.swing.JLabel productName;
    // End of variables declaration//GEN-END:variables
}
