package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// The payroll staff's main panel. Shows the employee roster with search,
// CRUD actions, and payroll report generation.
public class StaffPortalPanel extends JPanel {

    private static final String[] TABLE_COLUMN_HEADERS = {
            "Emp #", "Last Name", "First Name", "Position", "Birthday"
    };

    private final JTable staffRosterTable;
    private final DefaultTableModel staffRosterModel;
    private final JTextArea staffPayrollResultsArea;
    private final JLabel staffStatusLabel;

    // Action buttons
    private final JButton loadRosterButton;
    private final JButton singlePayslipButton;
    private final JButton allEmployeesPayrollButton;
    private final JButton computeSalariesButton;
    private final JButton generateSummaryButton;
    private final JButton addEmployeeButton;
    private final JButton editEmployeeButton;
    private final JButton deleteEmployeeButton;
    private final JButton logoutButton;

    // Shared pay-period input used by both Compute Salaries and Generate Summary,
    // since both operate on a single pay-period month (6-12).
    private final JTextField payPeriodField;

    // Search field
    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton clearSearchButton;

    public StaffPortalPanel() {
        super(new BorderLayout());
        setBackground(MainFrame.COLOR_BACKGROUND);
        add(MainFrame.buildApplicationHeader(
                "Payroll Staff Portal", "Manage employee records and generate payroll reports"), BorderLayout.NORTH);

        JPanel contentArea = MainFrame.createStyledPanel(new BorderLayout(0, 10), MainFrame.COLOR_BACKGROUND);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        staffRosterModel = new DefaultTableModel(TABLE_COLUMN_HEADERS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        staffRosterTable = new JTable(staffRosterModel);
        MainFrame.applyTableStyling(staffRosterTable);

        // Search bar sits above the table, full width
        searchField = MainFrame.createStyledTextField();
        searchField.setToolTipText("Search by employee number, name, or position");

        searchButton = MainFrame.createStyledButton("Search", MainFrame.COLOR_ACCENT_PRIMARY);
        searchButton.setPreferredSize(new Dimension(80, 30));

        clearSearchButton = MainFrame.createStyledButton("Clear", MainFrame.COLOR_ACCENT_WARNING);
        clearSearchButton.setPreferredSize(new Dimension(60, 30));

        JPanel searchButtons = MainFrame.createStyledPanel(new FlowLayout(FlowLayout.LEFT, 4, 0), MainFrame.COLOR_BACKGROUND);
        searchButtons.add(searchButton);
        searchButtons.add(clearSearchButton);

        JPanel searchBar = MainFrame.createStyledPanel(new BorderLayout(10, 0), MainFrame.COLOR_BACKGROUND);
        searchBar.add(MainFrame.createLabel(
                "Employee Roster", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY), BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButtons, BorderLayout.EAST);

        // Roster table gets its own scroll pane, no artificial width cap this time
        JScrollPane rosterScrollPane = MainFrame.createStyledScrollPane(staffRosterTable);

        // Results panel
        staffPayrollResultsArea = MainFrame.createStyledTextArea(
                "Select an employee and click a Generate button to produce a payroll report. "
                        + "Double-click any row to view full profile details.");
        JScrollPane staffResultsScrollPane = MainFrame.createStyledScrollPane(staffPayrollResultsArea);

        JPanel staffResultsPanel = MainFrame.createStyledPanel(new BorderLayout(0, 8), MainFrame.COLOR_BACKGROUND);
        staffResultsPanel.add(MainFrame.createLabel(
                "Payroll Report", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY),
                BorderLayout.NORTH);
        staffResultsPanel.add(staffResultsScrollPane, BorderLayout.CENTER);

        // Table and report side-by-side, with most of the width given to the table.
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rosterScrollPane, staffResultsPanel);
        mainSplit.setResizeWeight(0.62);
        mainSplit.setDividerLocation(640);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(null);
        mainSplit.setBackground(MainFrame.COLOR_BACKGROUND);
        mainSplit.setContinuousLayout(true);

        JPanel tableAndReportArea = MainFrame.createStyledPanel(new BorderLayout(0, 10), MainFrame.COLOR_BACKGROUND);
        tableAndReportArea.add(searchBar, BorderLayout.NORTH);
        tableAndReportArea.add(mainSplit, BorderLayout.CENTER);

        // Action buttons, arranged in two compact toolbar rows
        loadRosterButton = MainFrame.createStyledButton("Load Roster", MainFrame.COLOR_ACCENT_PRIMARY);
        singlePayslipButton = MainFrame.createStyledButton("Selected Payslip", MainFrame.COLOR_ACCENT_SUCCESS);
        allEmployeesPayrollButton = MainFrame.createStyledButton("All-Employee Payroll", MainFrame.COLOR_ACCENT_WARNING);
        computeSalariesButton = MainFrame.createStyledButton("Compute Salaries", MainFrame.COLOR_ACCENT_SUCCESS);
        generateSummaryButton = MainFrame.createStyledButton("Generate Summary", MainFrame.COLOR_ACCENT_PRIMARY);
        addEmployeeButton = MainFrame.createStyledButton("Add", MainFrame.COLOR_ACCENT_SUCCESS);
        editEmployeeButton = MainFrame.createStyledButton("Edit", MainFrame.COLOR_ACCENT_PRIMARY);
        deleteEmployeeButton = MainFrame.createStyledButton("Delete", MainFrame.COLOR_ACCENT_DANGER);
        logoutButton = MainFrame.createStyledButton("Log Out", MainFrame.COLOR_ACCENT_DANGER);

        payPeriodField = MainFrame.createStyledTextField();
        payPeriodField.setToolTipText("Month number 6 (June) through 12 (December)");
        payPeriodField.setPreferredSize(new Dimension(40, 26));
        payPeriodField.setHorizontalAlignment(JTextField.CENTER);

        JButton[] toolbarButtons = {
                loadRosterButton, addEmployeeButton, editEmployeeButton, deleteEmployeeButton,
                singlePayslipButton, allEmployeesPayrollButton, computeSalariesButton,
                generateSummaryButton, logoutButton
        };
        for (JButton button : toolbarButtons) {
            button.setBorder(new EmptyBorder(5, 12, 5, 12));
            button.setFont(new Font(MainFrame.FONT_UI, Font.BOLD, 11));
        }

        staffStatusLabel = MainFrame.createLabel(
                "", MainFrame.FONT_UI, Font.ITALIC, 11, MainFrame.COLOR_TEXT_SECONDARY);

        JPanel rosterActionsRow = MainFrame.createStyledPanel(new FlowLayout(FlowLayout.LEFT, 6, 4), MainFrame.COLOR_BACKGROUND);
        rosterActionsRow.add(loadRosterButton);
        rosterActionsRow.add(addEmployeeButton);
        rosterActionsRow.add(editEmployeeButton);
        rosterActionsRow.add(deleteEmployeeButton);

        JPanel payrollActionsRow = MainFrame.createStyledPanel(new FlowLayout(FlowLayout.LEFT, 6, 4), MainFrame.COLOR_BACKGROUND);
        payrollActionsRow.add(singlePayslipButton);
        payrollActionsRow.add(allEmployeesPayrollButton);
        payrollActionsRow.add(MainFrame.createLabel(
                "Pay Period (6-12):", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY));
        payrollActionsRow.add(payPeriodField);
        payrollActionsRow.add(computeSalariesButton);
        payrollActionsRow.add(generateSummaryButton);

        JPanel payrollRowAndLogout = MainFrame.createStyledPanel(new BorderLayout(6, 0), MainFrame.COLOR_BACKGROUND);
        payrollRowAndLogout.add(payrollActionsRow, BorderLayout.CENTER);
        payrollRowAndLogout.add(logoutButton, BorderLayout.EAST);

        JPanel toolbarStack = MainFrame.createStyledPanel(new GridLayout(2, 1, 0, 2), MainFrame.COLOR_BACKGROUND);
        toolbarStack.add(rosterActionsRow);
        toolbarStack.add(payrollRowAndLogout);

        JPanel bottomBar = MainFrame.createStyledPanel(new BorderLayout(0, 4), MainFrame.COLOR_BACKGROUND);
        bottomBar.add(toolbarStack, BorderLayout.NORTH);
        bottomBar.add(staffStatusLabel, BorderLayout.SOUTH);

        contentArea.add(tableAndReportArea, BorderLayout.CENTER);
        contentArea.add(bottomBar, BorderLayout.SOUTH);
        add(contentArea, BorderLayout.CENTER);
    }

    public JTable getStaffRosterTable() {
        return staffRosterTable;
    }

    public DefaultTableModel getStaffRosterModel() {
        return staffRosterModel;
    }

    public JTextArea getStaffPayrollResultsArea() {
        return staffPayrollResultsArea;
    }

    public JLabel getStaffStatusLabel() {
        return staffStatusLabel;
    }

    public JButton getLoadRosterButton() {
        return loadRosterButton;
    }

    public JButton getSinglePayslipButton() {
        return singlePayslipButton;
    }

    public JButton getAllEmployeesPayrollButton() {
        return allEmployeesPayrollButton;
    }

    public JButton getComputeSalariesButton() {
        return computeSalariesButton;
    }

    public JButton getGenerateSummaryButton() {
        return generateSummaryButton;
    }

    public JTextField getPayPeriodField() {
        return payPeriodField;
    }

    public JButton getAddEmployeeButton() {
        return addEmployeeButton;
    }

    public JButton getEditEmployeeButton() {
        return editEmployeeButton;
    }

    public JButton getDeleteEmployeeButton() {
        return deleteEmployeeButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getClearSearchButton() {
        return clearSearchButton;
    }
}
