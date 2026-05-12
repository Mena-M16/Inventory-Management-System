package com.inventory.view.staff;

import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.model.Product;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Staff stock panel — Stock IN and Stock OUT only (no ADJUST).
 */
public class StaffStock extends JPanel {

    private final StockController stockCtrl = new StockController();
    private final ProductController productCtrl = new ProductController();

    private JComboBox<Product> productCombo;
    private JComboBox<String> typeCombo;
    private JTextField qtyField;
    private JTextArea notesArea;
    private JLabel currentQtyLabel;

    public StaffStock() {
        setLayout(new BorderLayout());
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JPanel formCard = ThemeUtil.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setMaximumSize(new Dimension(400, 500));

        formCard.add(ThemeUtil.sectionTitle("Stock Movement"));
        formCard.add(Box.createVerticalStrut(16));

        List<Product> products = productCtrl.getActiveProducts();
        productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(ThemeUtil.FONT_BODY);
        productCombo.addActionListener(e -> updateCurrentQty());

        // Staff can only do IN and OUT
        typeCombo = new JComboBox<>(new String[]{"IN", "OUT"});
        typeCombo.setFont(ThemeUtil.FONT_BODY);

        qtyField = ThemeUtil.createTextField(10);
        notesArea = new JTextArea(3, 10);
        notesArea.setFont(ThemeUtil.FONT_BODY);
        notesArea.setBorder(BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR));

        currentQtyLabel = new JLabel("Current Stock: -");
        currentQtyLabel.setFont(ThemeUtil.FONT_BOLD);
        currentQtyLabel.setForeground(ThemeUtil.PRIMARY);

        addRow(formCard, "Product:", productCombo);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(currentQtyLabel);
        formCard.add(Box.createVerticalStrut(10));
        addRow(formCard, "Type:", typeCombo);
        addRow(formCard, "Quantity:", qtyField);
        addRow(formCard, "Notes:", new JScrollPane(notesArea));
        formCard.add(Box.createVerticalStrut(16));

        JButton applyBtn = ThemeUtil.primaryButton("Apply");
        applyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        applyBtn.addActionListener(e -> applyStock());
        formCard.add(applyBtn);

        updateCurrentQty();

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(formCard);
        add(wrapper, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, String label, Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeUtil.FONT_BOLD);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height + 4));
        ((JComponent) comp).setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comp);
        panel.add(Box.createVerticalStrut(10));
    }

    private void updateCurrentQty() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p != null) {
            currentQtyLabel.setText("Current Stock: " + p.getQuantity() + "  |  " + p.getStockStatus());
            currentQtyLabel.setForeground(ThemeUtil.stockStatusColor(p.getStockStatus()));
        }
    }

    private void applyStock() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) { NotificationUtils.showWarning(this, "Select a product."); return; }
        int qty;
        try { qty = Integer.parseInt(qtyField.getText().trim()); }
        catch (NumberFormatException ex) { NotificationUtils.showError(this, "Enter a valid quantity."); return; }

        String type = (String) typeCombo.getSelectedItem();
        if ("OUT".equals(type) && p.getQuantity() < qty) {
            NotificationUtils.showWarning(this, "Insufficient stock! Available: " + p.getQuantity());
            return;
        }
        if (!NotificationUtils.showConfirm(this, "Apply " + type + " of " + qty + " units for \"" + p.getName() + "\"?")) return;

        String err = stockCtrl.processStock(p.getId(), type, qty, notesArea.getText().trim());
        if (err == null) {
            NotificationUtils.showSuccess(this, "Stock " + type + " applied.");
            qtyField.setText(""); notesArea.setText("");
            List<Product> products = productCtrl.getActiveProducts();
            productCombo.removeAllItems();
            for (Product prod : products) productCombo.addItem(prod);
        } else {
            NotificationUtils.showError(this, err);
        }
    }
}
