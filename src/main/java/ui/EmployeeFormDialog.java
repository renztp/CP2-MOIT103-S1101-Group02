package ui;

import core.models.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// A modal dialog used for both adding new employees and editing existing ones.
// Presents all editable fields in a two-column layout and validates required
// and money fields before confirming.
public class EmployeeFormDialog extends JDialog {

    private final JTextField[] fields;
    private boolean confirmed = false;

    // Field labels matching Employee column order.
    private static final String[] FIELD_LABELS = {
        "Employee #", "Last Name", "First Name", "Birthday (MM/DD/YYYY)",
        "Address", "Phone Number", "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #",
        "Status", "Position", "Immediate Supervisor", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
        "Gross Semi-Monthly Rate", "Hourly Rate"
    };

    // Indices of fields that must not be blank.
    private static final int[] REQUIRED_FIELD_INDICES = {0, 1, 2, 3, 11, 18};

    // Indices of fields that hold money amounts and must parse as numbers.
    private static final int[] MONETARY_FIELD_INDICES = {
        Employee.COL_BASIC_SALARY, Employee.COL_RICE_SUBSIDY, Employee.COL_PHONE_ALLOWANCE,
        Employee.COL_CLOTHING_ALLOWANCE, Employee.COL_GROSS_SEMI_MONTHLY, Employee.COL_HOURLY_RATE
    };

    public EmployeeFormDialog(Frame parent, String dialogTitle, Employee existingEmployee) {
        super(parent, dialogTitle, true);
        setBackground(MainFrame.COLOR_BACKGROUND);

        fields = new JTextField[FIELD_LABELS.length];

        JPanel formPanel = MainFrame.createStyledPanel(new GridBagLayout(), MainFrame.COLOR_SURFACE);
        formPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.weightx = 0.5;

        // Lay out fields in two columns
        for (int i = 0; i < FIELD_LABELS.length; i++) {
            int col = i % 2;
            int row = i / 2;

            gbc.gridx = col * 2;
            gbc.gridy = row * 2;
            gbc.weightx = 0;
            JLabel label = MainFrame.createLabel(
                    FIELD_LABELS[i], MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY);
            formPanel.add(label, gbc);

            fields[i] = MainFrame.createStyledTextField();
            if (existingEmployee != null) {
                fields[i].setText(existingEmployee.getValueAt(i));
            }

            // Lock the ID field when editing to prevent accidental changes.
            if (i == Employee.COL_EMP_ID && existingEmployee != null) {
                fields[i].setEditable(false);
                fields[i].setForeground(MainFrame.COLOR_TEXT_SECONDARY);
            }

            gbc.gridx = col * 2;
            gbc.gridy = row * 2 + 1;
            gbc.weightx = 0.5;
            formPanel.add(fields[i], gbc);
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(MainFrame.COLOR_SURFACE);

        JButton confirmButton = MainFrame.createStyledButton("Save", MainFrame.COLOR_ACCENT_SUCCESS);
        JButton cancelButton = MainFrame.createStyledButton("Cancel", MainFrame.COLOR_ACCENT_DANGER);

        confirmButton.addActionListener(e -> {
            if (validateRequiredFields()) {
                confirmed = true;
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonRow = MainFrame.createStyledPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8), MainFrame.COLOR_BACKGROUND);
        buttonRow.add(cancelButton);
        buttonRow.add(confirmButton);

        getContentPane().setBackground(MainFrame.COLOR_BACKGROUND);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonRow, BorderLayout.SOUTH);

        setSize(720, 500);
        setLocationRelativeTo(parent);
    }

    // Checks that all required fields have non-blank values, and that every money
    // field that has a value actually parses as a number, before allowing save.
    private boolean validateRequiredFields() {
        for (JTextField field : fields) {
            MainFrame.clearFieldHighlight(field);
        }

        for (int idx : REQUIRED_FIELD_INDICES) {
            if (fields[idx].getText().trim().isEmpty()) {
                MainFrame.markFieldInvalid(fields[idx]);
                MainFrame.showWarningDialog(this, "\"" + FIELD_LABELS[idx] + "\" is required.");
                fields[idx].requestFocus();
                return false;
            }
        }

        for (int idx : MONETARY_FIELD_INDICES) {
            String rawValue = fields[idx].getText().trim();
            if (rawValue.isEmpty()) {
                continue;
            }
            try {
                Double.parseDouble(rawValue.replace(",", ""));
            } catch (NumberFormatException ex) {
                MainFrame.markFieldInvalid(fields[idx]);
                MainFrame.showWarningDialog(this, "\"" + FIELD_LABELS[idx] + "\" must be a valid number.");
                fields[idx].requestFocus();
                return false;
            }
        }

        return true;
    }

    // Returns true if the user clicked Save and all validation passed.
    public boolean isConfirmed() {
        return confirmed;
    }

    // Builds an Employee from the current field values.
    public Employee buildEmployee() {
        return Employee.fromFields(
                fields[0].getText(), fields[1].getText(), fields[2].getText(), fields[3].getText(),
                fields[4].getText(), fields[5].getText(), fields[6].getText(), fields[7].getText(),
                fields[8].getText(), fields[9].getText(), fields[10].getText(), fields[11].getText(),
                fields[12].getText(), fields[13].getText(), fields[14].getText(), fields[15].getText(),
                fields[16].getText(), fields[17].getText(), fields[18].getText()
        );
    }
}
