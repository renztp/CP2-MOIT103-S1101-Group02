package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffPortalPanel extends JPanel {

    private static final String[] TABLE_COLUMN_HEADERS = {
            "Emp #", "Last Name", "First Name", "Birthday"
    };

    private final JTable staffRosterTable;
    private final DefaultTableModel staffRosterModel;
    private final JTextArea staffPayrollResultsArea;
    private final JLabel staffStatusLabel;
    private final JButton loadRosterButton;
    private final JButton singlePayslipButton;
    private final JButton allEmployeesPayrollButton;
    private final JButton logoutButton;

    public StaffPortalPanel() {
        super(new BorderLayout());
        setBackground(MainFrame.COLOR_BACKGROUND);
        add(MainFrame.buildApplicationHeader(
                "Payroll Staff Portal", "Generate employee payroll reports"), BorderLayout.NORTH);

        JPanel contentArea = MainFrame.createStyledPanel(new BorderLayout(16, 16), MainFrame.COLOR_BACKGROUND);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        staffRosterModel = new DefaultTableModel(TABLE_COLUMN_HEADERS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        staffRosterTable = new JTable(staffRosterModel);
        MainFrame.applyTableStyling(staffRosterTable);

        JScrollPane rosterScrollPane = MainFrame.createStyledScrollPane(staffRosterTable);
        rosterScrollPane.setPreferredSize(new Dimension(320, 0));

        loadRosterButton = MainFrame.createStyledButton("Load Employee Roster", MainFrame.COLOR_ACCENT_PRIMARY);
        singlePayslipButton = MainFrame.createStyledButton(
                "Generate Selected Payslip", MainFrame.COLOR_ACCENT_SUCCESS);
        allEmployeesPayrollButton = MainFrame.createStyledButton(
                "Generate All-Employee Payroll", MainFrame.COLOR_ACCENT_WARNING);
        logoutButton = MainFrame.createStyledButton("Log Out", MainFrame.COLOR_ACCENT_DANGER);

        staffStatusLabel = MainFrame.createLabel(
                "", MainFrame.FONT_UI, Font.ITALIC, 11, MainFrame.COLOR_TEXT_SECONDARY);

        JPanel actionButtonPanel = MainFrame.createStyledPanel(new GridLayout(0, 1, 0, 8), MainFrame.COLOR_BACKGROUND);
        actionButtonPanel.add(loadRosterButton);
        actionButtonPanel.add(singlePayslipButton);
        actionButtonPanel.add(allEmployeesPayrollButton);
        actionButtonPanel.add(logoutButton);
        actionButtonPanel.add(staffStatusLabel);

        JPanel rosterPanel = MainFrame.createStyledPanel(new BorderLayout(0, 10), MainFrame.COLOR_BACKGROUND);
        rosterPanel.add(MainFrame.createLabel(
                "Employee Roster", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY),
                BorderLayout.NORTH);
        rosterPanel.add(rosterScrollPane, BorderLayout.CENTER);
        rosterPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        staffPayrollResultsArea = MainFrame.createStyledTextArea(
                "Select an employee and click a Generate button to produce a payroll report. "
                        + "Double-click any row to view full file details.");
        JScrollPane staffResultsScrollPane = MainFrame.createStyledScrollPane(staffPayrollResultsArea);

        JPanel staffResultsPanel = MainFrame.createStyledPanel(new BorderLayout(0, 8), MainFrame.COLOR_BACKGROUND);
        staffResultsPanel.add(MainFrame.createLabel(
                "Payroll Report", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY),
                BorderLayout.NORTH);
        staffResultsPanel.add(staffResultsScrollPane, BorderLayout.CENTER);

        contentArea.add(rosterPanel, BorderLayout.WEST);
        contentArea.add(staffResultsPanel, BorderLayout.CENTER);
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

    public JButton getLogoutButton() {
        return logoutButton;
    }
}
