import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// ======================================================================
// Main Application Launcher
// ======================================================================
/**
 * This is the main entry point of our application.
 * Its only job is to set a nice Look and Feel and start the first dialog window.
 * We run our GUI on the Event Dispatch Thread (EDT) using SwingUtilities.invokeLater()
 * to ensure that all UI updates are thread-safe, which is a best practice in Swing.
 */
public class EmergencyMedicineSupply {
    public static void main(String[] args) {
        try {
            // I set the UI to match the user's operating system for a native, modern feel.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If the system L&F fails, I print an error, but the app will still run with the default Java L&F.
            e.printStackTrace();
        }

        // I start the GUI on the Event Dispatch Thread. This is the standard and safe way to launch a Swing app.
        SwingUtilities.invokeLater(() -> {
            StartupDialog startupDialog = new StartupDialog();
            startupDialog.setVisible(true);
        });
    }
}

// ======================================================================
// Dialogs for User Interaction (Startup, Role Selection, Forms)
// ======================================================================

/**
 * This is the first window the user sees, the one you liked!
 * I use this to collect the user's name and their current location, which is essential for calculating distances.
 */
class StartupDialog extends JDialog {
    private final JTextField nameField;
    private final JComboBox<NamedLocation> locationComboBox;

    public StartupDialog() {
        setTitle("Welcome - Setup Your Session");
        setModal(true);
        setSize(400, 220);
        setLocationRelativeTo(null); // This centers the dialog on the screen.
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // I get the list of predefined locations from our data service.
        Map<String, Location> locations = DataService.getPredefinedLocations();

        // I use GridBagLayout here because it gives me precise control over where each component goes.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // This adds a little padding around components.
        gbc.fill = GridBagConstraints.HORIZONTAL; // This makes components fill the horizontal space.

        // Row 0: Name Label and Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Your Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField();
        panel.add(nameField, gbc);

        // Row 1: Location Label and ComboBox (the dropdown menu)
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Select Your Location:"), gbc);
        gbc.gridx = 1;
        locationComboBox = new JComboBox<>();
        // I use a loop to add each location to the dropdown.
        locations.forEach((name, loc) -> locationComboBox.addItem(new NamedLocation(name, loc)));
        panel.add(locationComboBox, gbc);

        // Row 2: Continue Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST; // I align the button to the right for a clean look.
        JButton startButton = new JButton("Continue →");
        panel.add(startButton, gbc);
        
        add(panel, BorderLayout.CENTER);

        // This is the action listener for the continue button. It triggers when the button is clicked.
        startButton.addActionListener(e -> {
            String customerName = nameField.getText();
            // I check if the user actually entered a name.
            if (customerName == null || customerName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return; // I stop the process if the name is empty.
            }
            dispose(); // I close this dialog.
            User user = new User(customerName.trim());
            Location selectedLocation = ((NamedLocation) locationComboBox.getSelectedItem()).getLocation();
            // Now, I open the role selection dialog, passing the user's info.
            new RoleSelectionDialog(user, selectedLocation).setVisible(true);
        });
    }
}

/**
 * This dialog lets the user choose between the Buyer and Shopkeeper roles.
 * This is a key part of the user flow, enabling access to different dashboards.
 */
class RoleSelectionDialog extends JDialog {
    public RoleSelectionDialog(User user, Location location) {
        setTitle("Choose Your Role");
        setModal(true);
        setSize(450, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // I use a custom rounded panel here for a modern, soft look.
        RoundedPanel panel = new RoundedPanel(15, new Color(245, 245, 245));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 40));

        JButton buyerButton = new JButton("👤 I'm a Buyer");
        buyerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyerButton.setPreferredSize(new Dimension(180, 50));
        
        JButton adminButton = new JButton("🔧 I'm a Shopkeeper");
        adminButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adminButton.setPreferredSize(new Dimension(180, 50));
        
        panel.add(buyerButton);
        panel.add(adminButton);
        add(panel);

        // This listener opens the Buyer Dashboard.
        buyerButton.addActionListener(e -> {
            dispose();
            new BuyerDashboardFrame(user, location).setVisible(true);
        });

        // This listener opens the Admin Dashboard.
        adminButton.addActionListener(e -> {
            dispose();
            new AdminDashboardFrame(user, location).setVisible(true);
        });
    }
}

/**
 * This is a reusable dialog form for both adding and editing a medicine.
 * If a 'medicineToEdit' object is passed to the constructor, it populates the fields for editing.
 * Otherwise, it shows blank fields for adding a new medicine. This is an efficient way to reuse code.
 */
class MedicineFormDialog extends JDialog {
    // I declare all the form fields here.
    private final JTextField nameField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField supplierField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField stockField = new JTextField();
    private final JComboBox<String> pharmacyComboBox = new JComboBox<>();
    private boolean isSaved = false; // This flag tracks if the user clicked "Save".

    public MedicineFormDialog(Frame owner, String title, Medicine medicineToEdit) {
        super(owner, title, true);
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Panel for the input fields.
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // I use arrays and a loop to create the form, which is cleaner than adding each line manually.
        String[] labels = {"Name:", "Category:", "Supplier:", "Price (BDT):", "Stock:", "Pharmacy:"};
        JComponent[] components = {nameField, categoryField, supplierField, priceField, stockField, pharmacyComboBox};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0; // The label doesn't stretch.
            fieldsPanel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1; // The text field stretches to fill the space.
            fieldsPanel.add(components[i], gbc);
        }
        
        // I populate the pharmacy dropdown from our data source.
        DataService.getPharmacies().forEach(p -> pharmacyComboBox.addItem(p.getName()));

        // If we are editing, I pre-fill the form fields with the existing data.
        if (medicineToEdit != null) {
            nameField.setText(medicineToEdit.getName());
            categoryField.setText(medicineToEdit.getCategory());
            supplierField.setText(medicineToEdit.getSupplier());
            priceField.setText(String.valueOf(medicineToEdit.getPrice()));
            stockField.setText(String.valueOf(medicineToEdit.getStock()));
            pharmacyComboBox.setSelectedItem(medicineToEdit.getPharmacyName());
            // I disable these fields because you usually don't change a medicine's name or its pharmacy when editing.
            pharmacyComboBox.setEnabled(false);
            nameField.setEditable(false);
        }

        // Panel for the action buttons.
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBorder(new EmptyBorder(0, 15, 10, 15));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Action Listeners for the buttons.
        saveButton.addActionListener(e -> {
            // I only save and close if the input is valid.
            if (validateInput()) {
                isSaved = true;
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose()); // Cancel just closes the dialog.
    }

    /**
     * This method validates the user input in the form fields. It's a simple way to prevent bad data.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInput() {
        try {
            if (nameField.getText().trim().isEmpty() || categoryField.getText().trim().isEmpty()) {
                throw new Exception("Name and Category cannot be empty.");
            }
            // I try to convert text to numbers. If it fails, I catch the error.
            Double.parseDouble(priceField.getText());
            Integer.parseInt(stockField.getText());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * This method is called from the Admin Dashboard to get the data from the form.
     * It returns a new Medicine object if the user clicked "Save", or null if they cancelled.
     * @return A Medicine object, or null.
     */
    public Medicine getMedicine() {
        if (!isSaved) return null;
        return new Medicine(
            nameField.getText().trim(),
            categoryField.getText().trim(),
            supplierField.getText().trim(),
            Double.parseDouble(priceField.getText()),
            Integer.parseInt(stockField.getText()),
            (String) pharmacyComboBox.getSelectedItem()
        );
    }
}


// ======================================================================
// Dashboards (Buyer and Admin)
// ======================================================================

/**
 * This is the main window for the "Buyer" role.
 * It features live search, distance calculation, and a "Buy" functionality.
 * The closest pharmacy is highlighted in the table for easy identification.
 */
class BuyerDashboardFrame extends JFrame {
    private final PharmacyService pharmacyService = new PharmacyService();
    private final DefaultTableModel tableModel;
    private final JTable resultsTable;
    private final JTextField searchField;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final User user;
    private final Location userLocation;
    private String closestPharmacyName = "";

    public BuyerDashboardFrame(User user, Location userLocation) {
        this.user = user;
        this.userLocation = userLocation;

        setTitle("Buyer Dashboard - Emergency Medicine Supply");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        // --- I use a custom GradientPanel for a modern header ---
        JPanel headerPanel = new GradientPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Welcome, " + user.getName() + " (Buyer View)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        // --- This is the key "Back to Roles" button for seamless switching ---
        JButton backButton = new JButton("↩ Back to Roles");
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(backButton, BorderLayout.EAST);

        // --- Main Content Panel ---
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        contentPanel.setOpaque(false); // I make it transparent to see the frame's nice background.
        
        // --- I use a TitledBorder to group the search components visually ---
        TitledBorder searchBorder = BorderFactory.createTitledBorder("Find a Medicine");
        searchBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(searchBorder);
        searchPanel.add(new JLabel("Type to search:"));
        searchField = new JTextField(30);
        searchPanel.add(searchField);
        
        // --- I define the columns for our results table ---
        String[] columnNames = {"Name", "Category", "Price (BDT)", "Stock", "Pharmacy", "Distance (km)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // I override this method to make the table cells not editable by the user.
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resultsTable = new JTable(tableModel);
        setupTableStyle(resultsTable);

        // The Sorter is what enables the awesome live filtering of the table.
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Footer Panel for the main action button ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBorder(new EmptyBorder(0, 15, 10, 15));
        footerPanel.setOpaque(false);
        JButton buyButton = new JButton("🛒 Buy Selected Medicine");
        buyButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        footerPanel.add(buyButton);

        // --- I add all the panels to the main frame window ---
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        // I load the data as soon as the window opens so it's not empty.
        populateInitialData();
        // I apply the custom renderer to highlight the closest pharmacy in green.
        resultsTable.setDefaultRenderer(Object.class, new ClosestPharmacyRenderer(closestPharmacyName));

        // --- Action Listeners for user interactions ---
        // This listener updates the table filter every time the user types a character.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });
        
        buyButton.addActionListener(e -> performBuy());
        // The back button disposes this window and opens the role selection dialog again.
        backButton.addActionListener(e -> {
            this.dispose();
            new RoleSelectionDialog(this.user, this.userLocation).setVisible(true);
        });
    }

    /**
     * A helper method to apply a consistent style to our tables.
     */
    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(220, 220, 220));
        table.setFillsViewportHeight(true);
    }

    /**
     * This method fetches all medicines and populates the table.
     * It's called initially and after a purchase to refresh the data.
     */
    private void populateInitialData() {
        List<SearchResult> results = pharmacyService.searchMedicine("", this.userLocation);
        // I find and store the name of the closest pharmacy to use in the renderer.
        if (!results.isEmpty()) {
            this.closestPharmacyName = results.get(0).getPharmacyName();
        }
        tableModel.setRowCount(0); // I clear any previous data.
        for (SearchResult result : results) {
             tableModel.addRow(new Object[]{
                result.getMedicine().getName(), result.getMedicine().getCategory(),
                String.format("%.2f", result.getMedicine().getPrice()), result.getMedicine().getStock(),
                result.getPharmacyName(), String.format("%.2f", result.getDistance()),
            });
        }
    }

    /**
     * This method applies the filter to the table based on the search text.
     */
    private void filterTable() {
        String text = searchField.getText();
        // I apply a case-insensitive regex filter on the "Name" (col 0) and "Pharmacy" (col 4) columns.
        sorter.setRowFilter(text.trim().length() == 0 ? null : RowFilter.regexFilter("(?i)" + text, 0, 4));
    }

    /**
     * This method handles the logic for buying a medicine.
     */
    private void performBuy() {
        int selectedViewRow = resultsTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I convert the view row to the model row to get the correct data even when the table is sorted or filtered.
        int modelRow = resultsTable.convertRowIndexToModel(selectedViewRow);
        
        // I retrieve the data from the service to ensure it's the absolute latest.
        String pharmacyName = (String) tableModel.getValueAt(modelRow, 4);
        String medicineName = (String) tableModel.getValueAt(modelRow, 0);
        Medicine med = pharmacyService.getMedicineDetails(pharmacyName, medicineName);
        
        if (med == null) {
            JOptionPane.showMessageDialog(this, "Could not find medicine details. The stock might have just run out.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity for " + medicineName + ":", "Buy Medicine", JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0 || quantity > med.getStock()) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity. Must be between 1 and " + med.getStock() + ".", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = pharmacyService.buyMedicine(this.user, pharmacyName, medicineName, quantity);
                if (success) {
                    showReceipt(pharmacyName, medicineName, quantity, med.getPrice());
                    populateInitialData(); // I refresh the table to show the updated stock.
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * This method displays a formatted receipt in a dialog box after a successful purchase.
     */
    private void showReceipt(String pharmacyName, String medicineName, int quantity, double pricePerUnit) {
        double total = quantity * pricePerUnit;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String receiptText = "          *** RECEIPT ***\n\n" +
                             "Customer: " + this.user.getName() + "\n" +
                             "Pharmacy: " + pharmacyName + "\n" +
                             "-----------------------------------------\n" +
                             "Item: " + medicineName + "\n" +
                             "Quantity: " + quantity + "\n" +
                             "Price/Unit: BDT " + df.format(pricePerUnit) + "\n" +
                             "-----------------------------------------\n" +
                             "TOTAL: BDT " + df.format(total) + "\n\n" +
                             "Thank you for your purchase!";
        JTextArea textArea = new JTextArea(receiptText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
    }
}

/**
 * The main window for the "Shopkeeper" (Admin) role.
 * Features a full inventory view and fully functional buttons for Add, Edit, Delete,
 * and viewing the sales history.
 */
class AdminDashboardFrame extends JFrame {
    private final PharmacyService pharmacyService = new PharmacyService();
    private final DefaultTableModel tableModel;
    private final JTable inventoryTable;
    private final User user;
    private final Location location;

    public AdminDashboardFrame(User user, Location location) {
        this.user = user;
        this.location = location;
        
        setTitle("Admin Dashboard - Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        // Admin Header with Gradient
        JPanel headerPanel = new GradientPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel titleLabel = new JLabel("Shopkeeper Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        
        JButton backButton = new JButton("↩ Back to Roles");
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(backButton, BorderLayout.EAST);

        // Main Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        contentPanel.setOpaque(false);
        TitledBorder tableBorder = BorderFactory.createTitledBorder("Full Medicine Inventory");
        tableBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.setBorder(tableBorder);

        String[] columnNames = {"Name", "Category", "Supplier", "Price (BDT)", "Stock", "Pharmacy"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        setupTableStyle(inventoryTable);
        contentPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        
        // Admin Footer Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footerPanel.setOpaque(false);
        JButton addButton = new JButton("➕ Add Medicine");
        JButton editButton = new JButton("✏️ Edit Medicine");
        JButton deleteButton = new JButton("❌ Delete Medicine");
        JButton historyButton = new JButton("📜 View Sales History");
        footerPanel.add(addButton);
        footerPanel.add(editButton);
        footerPanel.add(deleteButton);
        footerPanel.add(historyButton);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        populateAdminTable();
        
        // --- Action Listeners ---
        backButton.addActionListener(e -> {
            this.dispose();
            new RoleSelectionDialog(this.user, this.location).setVisible(true);
        });
        historyButton.addActionListener(e -> new SalesHistoryDialog(this).setVisible(true));
        addButton.addActionListener(e -> performAdd());
        editButton.addActionListener(e -> performEdit());
        deleteButton.addActionListener(e -> performDelete());
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(220, 220, 220));
    }

    private void populateAdminTable() {
        // Admin sees all medicines, unsorted by distance.
        List<SearchResult> results = pharmacyService.searchMedicine("", null);
        tableModel.setRowCount(0);
        for (SearchResult result : results) {
            tableModel.addRow(new Object[]{
                result.getMedicine().getName(), result.getMedicine().getCategory(), result.getMedicine().getSupplier(),
                String.format("%.2f", result.getMedicine().getPrice()), result.getMedicine().getStock(),
                result.getPharmacyName()
            });
        }
    }

    private void performAdd() {
        MedicineFormDialog dialog = new MedicineFormDialog(this, "Add New Medicine", null);
        dialog.setVisible(true);
        Medicine newMedicine = dialog.getMedicine();
        // I check if the user actually saved the form before trying to add the medicine.
        if (newMedicine != null) {
            pharmacyService.addMedicine(newMedicine);
            populateAdminTable(); // I refresh the table to show the new entry.
        }
    }

    private void performEdit() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String medName = (String) tableModel.getValueAt(selectedRow, 0);
        String pharmacyName = (String) tableModel.getValueAt(selectedRow, 5);
        Medicine medicineToEdit = pharmacyService.getMedicineDetails(pharmacyName, medName);

        if (medicineToEdit != null) {
            MedicineFormDialog dialog = new MedicineFormDialog(this, "Edit Medicine", medicineToEdit);
            dialog.setVisible(true);
            Medicine updatedMedicine = dialog.getMedicine();
            if (updatedMedicine != null) {
                pharmacyService.updateMedicine(updatedMedicine);
                populateAdminTable(); // I refresh the table to show the changes.
            }
        }
    }

    private void performDelete() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String medName = (String) tableModel.getValueAt(selectedRow, 0);
        String pharmacyName = (String) tableModel.getValueAt(selectedRow, 5);
        
        // I ask for confirmation before deleting to prevent accidents.
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete " + medName + " from " + pharmacyName + "?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            pharmacyService.deleteMedicine(pharmacyName, medName);
            populateAdminTable(); // I refresh the table after deletion.
        }
    }
}


// ======================================================================
// Helper Dialogs, Renderers, and Custom Components
// ======================================================================
class SalesHistoryDialog extends JDialog {
    public SalesHistoryDialog(Frame owner) {
        super(owner, "Sales Transaction History", true);
        setSize(800, 500);
        setLocationRelativeTo(owner);
        
        String[] columnNames = {"Timestamp", "Customer", "Medicine", "Quantity", "Total Price (BDT)", "Pharmacy"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        
        List<PurchaseRecord> records = PharmacyService.getPurchaseHistory();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // I loop backwards to show the most recent purchase at the top.
        for (int i = records.size() - 1; i >= 0; i--) {
            PurchaseRecord record = records.get(i);
            model.addRow(new Object[]{
                sdf.format(record.getTimestamp()), record.getCustomerName(), record.getMedicineName(),
                record.getQuantity(), df.format(record.getTotalPrice()), record.getPharmacyName()
            });
        }
        add(new JScrollPane(table));
    }
}

class ClosestPharmacyRenderer extends DefaultTableCellRenderer {
    private final String closestPharmacyName;

    public ClosestPharmacyRenderer(String closestPharmacyName) {
        this.closestPharmacyName = closestPharmacyName;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        int modelRow = table.convertRowIndexToModel(row);
        String currentPharmacy = (String) table.getModel().getValueAt(modelRow, 4); 

        // I check if the pharmacy in this row is the closest one.
        if (currentPharmacy != null && currentPharmacy.equals(closestPharmacyName)) {
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            c.setForeground(new Color(0, 120, 0)); // A nice dark green color.
        } else {
             c.setForeground(table.getForeground());
             c.setFont(c.getFont().deriveFont(Font.PLAIN));
        }
        return c;
    }
}

/**
 * This is a custom JPanel with a gradient background. I use it for the headers to make them look cool.
 */
class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        Color color1 = new Color(60, 63, 65);
        Color color2 = new Color(90, 93, 95);
        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}

/**
 * This is a custom JPanel with rounded corners. I use it for dialog backgrounds for a soft, modern UI.
 */
class RoundedPanel extends JPanel {
    private final int cornerRadius;
    private final Color backgroundColor;
    public RoundedPanel(int radius, Color bgColor) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(cornerRadius, cornerRadius);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
        graphics.setColor(getForeground());
    }
}

// ======================================================================
// Service and Data Layer
// ======================================================================
class PharmacyService {
    // I make these lists 'static' so that the data persists throughout the entire application's session.
    // This means the stock levels and sales history are shared across all windows.
    private static final List<Pharmacy> pharmacies = DataService.getPharmacies();
    private static final List<PurchaseRecord> purchaseHistory = new ArrayList<>();

    /**
     * This method searches for medicines.
     * If a userLocation is provided, it calculates and sorts by distance.
     * If userLocation is null (for admin), it just returns all matching medicines.
     */
    public List<SearchResult> searchMedicine(String searchTerm, Location userLocation) {
        List<SearchResult> results = new ArrayList<>();
        for (Pharmacy pharmacy : pharmacies) {
            for (Medicine medicine : pharmacy.getInventory()) {
                boolean matches = searchTerm == null || searchTerm.trim().isEmpty() ||
                                  medicine.getName().toLowerCase().contains(searchTerm.trim().toLowerCase());

                if (matches && medicine.getStock() > 0) {
                    double distance = (userLocation != null) ? 
                        DistanceCalculator.calculate(userLocation.getLatitude(), userLocation.getLongitude(),
                        pharmacy.getLocation().getLatitude(), pharmacy.getLocation().getLongitude()) : -1.0;
                    results.add(new SearchResult(pharmacy.getId(), pharmacy.getName(), medicine, distance));
                }
            }
        }
        // I only sort by distance if it's a buyer (who has a location).
        if (userLocation != null) {
            results.sort(Comparator.comparingDouble(SearchResult::getDistance));
        }
        return results;
    }

    /**
     * This method handles the logic for a user buying a medicine.
     * It reduces the stock and creates a sales record.
     */
    public boolean buyMedicine(User user, String pharmacyName, String medicineName, int quantity) {
        Medicine med = getMedicineDetails(pharmacyName, medicineName);
        if (med != null && med.getStock() >= quantity) {
            med.setStock(med.getStock() - quantity);
            // I log the purchase to our history list.
            purchaseHistory.add(new PurchaseRecord(user.getName(), medicineName, quantity, quantity * med.getPrice(), pharmacyName));
            return true;
        }
        return false;
    }
    
    /**
     * A helper method to find a specific medicine in a specific pharmacy.
     */
    public Medicine getMedicineDetails(String pharmacyName, String medicineName) {
        for (Pharmacy p : pharmacies) {
            if (p.getName().equals(pharmacyName)) {
                for (Medicine m : p.getInventory()) {
                    if (m.getName().equals(medicineName)) {
                        return m;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * This method adds a new medicine to a pharmacy's inventory.
     */
    public void addMedicine(Medicine newMedicine) {
        for (Pharmacy p : pharmacies) {
            if (p.getName().equals(newMedicine.getPharmacyName())) {
                // To prevent duplicates, a real app would check if the medicine already exists.
                // For simplicity, I just add it.
                p.addMedicine(newMedicine);
                return;
            }
        }
    }

    /**
     * This method updates the details of an existing medicine.
     */
    public void updateMedicine(Medicine updatedMedicine) {
        Medicine toUpdate = getMedicineDetails(updatedMedicine.getPharmacyName(), updatedMedicine.getName());
        if (toUpdate != null) {
            toUpdate.setCategory(updatedMedicine.getCategory());
            toUpdate.setSupplier(updatedMedicine.getSupplier());
            toUpdate.setPrice(updatedMedicine.getPrice());
            toUpdate.setStock(updatedMedicine.getStock());
        }
    }

    /**
     * This method removes a medicine from a pharmacy's inventory.
     */
    public void deleteMedicine(String pharmacyName, String medicineName) {
        for (Pharmacy p : pharmacies) {
            if (p.getName().equals(pharmacyName)) {
                // I use removeIf, which is a clean way to remove an item from a list based on a condition.
                p.getInventory().removeIf(med -> med.getName().equals(medicineName));
                return;
            }
        }
    }

    /**
     * This method returns the entire sales history.
     */
    public static List<PurchaseRecord> getPurchaseHistory() {
        return purchaseHistory;
    }
}

class DataService {
    private static List<Pharmacy> pharmacyCache = null;

    public static List<Pharmacy> getPharmacies() {
        // I use a cache (a stored copy) so that the data is created only once.
        // This makes our data persistent for the app's session (stock updates are remembered).
        if (pharmacyCache == null) {
            pharmacyCache = new ArrayList<>();
        
            Pharmacy p1 = new Pharmacy("p1", "Lazz Pharma (Uttara)", new Location(23.8737, 90.3965));
            p1.addMedicine(new Medicine("Napa Extend", "Painkiller", "Beximco", 6.00, 200, p1.getName()));
            p1.addMedicine(new Medicine("Fexo 120", "Antihistamine", "Square", 8.00, 150, p1.getName()));
            p1.addMedicine(new Medicine("Monas 10", "Asthma", "Acme", 12.50, 90, p1.getName()));
            pharmacyCache.add(p1);

            Pharmacy p2 = new Pharmacy("p2", "Medex Pharmacy (Gulshan)", new Location(23.7949, 90.4143));
            p2.addMedicine(new Medicine("Seclo 20", "Antacid", "Square", 7.00, 300, p2.getName()));
            p2.addMedicine(new Medicine("Ceevit", "Vitamin", "GSK", 3.00, 500, p2.getName()));
            p2.addMedicine(new Medicine("Napa Extend", "Painkiller", "Beximco", 6.10, 180, p2.getName()));
            pharmacyCache.add(p2);
            
            Pharmacy p3 = new Pharmacy("p3", "Health Hub (Dhanmondi)", new Location(23.7465, 90.3765));
            p3.addMedicine(new Medicine("Tufnil", "Painkiller", "Opsonin", 5.00, 120, p3.getName()));
            p3.addMedicine(new Medicine("Azithromycin 500", "Antibiotic", "Beximco", 35.00, 80, p3.getName()));
            p3.addMedicine(new Medicine("Finix 20", "Antacid", "Opsonin", 7.50, 220, p3.getName()));
            pharmacyCache.add(p3);

            Pharmacy p4 = new Pharmacy("p4", "Mirpur City Pharma", new Location(23.8059, 90.3493));
            p4.addMedicine(new Medicine("Napa Extend", "Painkiller", "Beximco", 5.90, 250, p4.getName()));
            p4.addMedicine(new Medicine("Seclo 20", "Antacid", "Square", 7.10, 180, p4.getName()));
            p4.addMedicine(new Medicine("Fexo 120", "Antihistamine", "Square", 8.25, 130, p4.getName()));
            pharmacyCache.add(p4);
        }
        return pharmacyCache;
    }

    public static Map<String, Location> getPredefinedLocations() {
        // I use a TreeMap to keep the list of locations sorted alphabetically in the dropdown.
        Map<String, Location> locations = new TreeMap<>();
        locations.put("Banani", new Location(23.7925, 90.4078));
        locations.put("Bashundhara R/A", new Location(23.8153, 90.4253));
        locations.put("Dhanmondi", new Location(23.7465, 90.3765));
        locations.put("Gulshan", new Location(23.7949, 90.4143));
        locations.put("Mirpur-10", new Location(23.8059, 90.3693));
        locations.put("Motijheel", new Location(23.7313, 90.4158));
        locations.put("Uttara", new Location(23.8737, 90.3965));
        return locations;
    }
}

// ======================================================================
// Model and Helper Classes (The Blueprints for our Data)
// ======================================================================
class DistanceCalculator {
    public static double calculate(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
class PurchaseRecord {
    private final String customerName, medicineName, pharmacyName;
    private final int quantity;
    private final double totalPrice;
    private final Date timestamp;
    public PurchaseRecord(String cName, String mName, int qty, double total, String pName) {
        this.customerName = cName; this.medicineName = mName; this.quantity = qty;
        this.totalPrice = total; this.pharmacyName = pName; this.timestamp = new Date();
    }
    public String getCustomerName() { return customerName; }
    public String getMedicineName() { return medicineName; }
    public String getPharmacyName() { return pharmacyName; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public Date getTimestamp() { return timestamp; }
}
class Medicine {
    private String name, category, supplier, pharmacyName;
    private double price;
    private int stock;
    public Medicine(String name, String cat, String sup, double price, int stock, String pName) {
        this.name = name; this.category = cat; this.supplier = sup; this.price = price; this.stock = stock; this.pharmacyName = pName;
    }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSupplier() { return supplier; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getPharmacyName() { return pharmacyName; }
    public void setStock(int stock) { this.stock = stock; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
}
class Pharmacy {
    private final String id, name;
    private final Location location;
    private final List<Medicine> inventory = new ArrayList<>();
    public Pharmacy(String id, String name, Location location) { this.id = id; this.name = name; this.location = location; }
    public void addMedicine(Medicine medicine) { this.inventory.add(medicine); }
    public String getId() { return id; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public List<Medicine> getInventory() { return inventory; }
}
class Location {
    private final double latitude, longitude;
    public Location(double lat, double lon) { this.latitude = lat; this.longitude = lon; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
class NamedLocation {
    private final String name;
    private final Location location;
    public NamedLocation(String name, Location location) { this.name = name; this.location = location; }
    public Location getLocation() { return location; }
    @Override
    public String toString() { return name; }
}
class User {
    private final String name;
    public User(String name) { this.name = name; }
    public String getName() { return name; }
}
class SearchResult {
    private final String pharmacyId, pharmacyName;
    private final Medicine medicine;
    private final double distance;
    public SearchResult(String pId, String pName, Medicine med, double dist) {
        this.pharmacyId = pId; this.pharmacyName = pName; this.medicine = med; this.distance = dist;
    }
    public String getPharmacyId() { return pharmacyId; }
    public String getPharmacyName() { return pharmacyName; }
    public Medicine getMedicine() { return medicine; }
    public double getDistance() { return distance; }
}
