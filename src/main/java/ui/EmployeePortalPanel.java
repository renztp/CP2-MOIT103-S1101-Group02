package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class EmployeePortalPanel extends JPanel {

    private final JComboBox<String> empIdSelectorDropdown;
    private final JLabel empNumberDisplayLabel;
    private final JLabel empNameDisplayLabel;
    private final JLabel empBirthdayDisplayLabel;
    private final JTextArea empPayrollResultsArea;
    private final JTextField empPayCoverageField;
    private final JButton computePayslipButton;
    private final JButton logoutButton;

    public EmployeePortalPanel() {
        super(new BorderLayout());
        setBackground(MainFrame.COLOR_BACKGROUND);
        add(MainFrame.buildApplicationHeader(
                "Employee Portal", "Select an employee to view details and payslip"), BorderLayout.NORTH);

        JPanel contentArea = MainFrame.createStyledPanel(new BorderLayout(16, 16), MainFrame.COLOR_BACKGROUND);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel infoCard = MainFrame.createStyledPanel(new GridBagLayout(), MainFrame.COLOR_SURFACE);
        infoCard.setBorder(new CompoundBorder(
                new LineBorder(MainFrame.COLOR_BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        infoCard.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints infoConstraints = new GridBagConstraints();
        infoConstraints.fill = GridBagConstraints.HORIZONTAL;
        infoConstraints.gridx = 0;
        infoConstraints.weightx = 1;
        infoConstraints.insets = new Insets(4, 0, 4, 0);

        infoConstraints.gridy = 0;
        infoCard.add(MainFrame.createLabel(
                "Select Employee Account", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY),
                infoConstraints);

        empIdSelectorDropdown = new JComboBox<>();
        empIdSelectorDropdown.setFont(new Font(MainFrame.FONT_UI, Font.PLAIN, 13));
        empIdSelectorDropdown.setBackground(MainFrame.COLOR_INPUT_BACKGROUND);
        empIdSelectorDropdown.setForeground(MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 1;
        infoConstraints.insets = new Insets(6, 0, 12, 0);
        infoCard.add(empIdSelectorDropdown, infoConstraints);
        infoConstraints.insets = new Insets(4, 0, 4, 0);

        infoConstraints.gridy = 2;
        infoCard.add(MainFrame.createSeparatorLine(), infoConstraints);

        infoConstraints.gridy = 3;
        infoCard.add(MainFrame.createLabel(
                "Employee Number", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empNumberDisplayLabel = MainFrame.createLabel(
                "-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 4;
        infoCard.add(empNumberDisplayLabel, infoConstraints);

        infoConstraints.gridy = 5;
        infoCard.add(MainFrame.createLabel(
                "Full Name", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empNameDisplayLabel = MainFrame.createLabel(
                "-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 6;
        infoCard.add(empNameDisplayLabel, infoConstraints);

        infoConstraints.gridy = 7;
        infoCard.add(MainFrame.createLabel(
                "Birthday", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empBirthdayDisplayLabel = MainFrame.createLabel(
                "-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 8;
        infoCard.add(empBirthdayDisplayLabel, infoConstraints);

        infoConstraints.gridy = 9;
        infoConstraints.insets = new Insets(16, 0, 4, 0);
        infoCard.add(MainFrame.createLabel(
                "Pay Coverage Month (6-12)", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empPayCoverageField = MainFrame.createStyledTextField();
        empPayCoverageField.setToolTipText("Enter a month number: 6 = June, 7 = July ... 12 = December");
        infoConstraints.gridy = 10;
        infoConstraints.insets = new Insets(4, 0, 8, 0);
        infoCard.add(empPayCoverageField, infoConstraints);

        computePayslipButton = MainFrame.createStyledButton("View My Payslip", MainFrame.COLOR_ACCENT_PRIMARY);
        infoConstraints.gridy = 11;
        infoConstraints.insets = new Insets(4, 0, 4, 0);
        infoCard.add(computePayslipButton, infoConstraints);

        logoutButton = MainFrame.createStyledButton("Log Out", MainFrame.COLOR_ACCENT_DANGER);
        infoConstraints.gridy = 12;
        infoConstraints.insets = new Insets(12, 0, 0, 0);
        infoCard.add(logoutButton, infoConstraints);

        empPayrollResultsArea = MainFrame.createStyledTextArea(
                "Your payslip will appear here after you click 'View My Payslip'.");
        JScrollPane payslipScrollPane = MainFrame.createStyledScrollPane(empPayrollResultsArea);

        JPanel resultsCard = MainFrame.createStyledPanel(new BorderLayout(0, 8), MainFrame.COLOR_BACKGROUND);
        resultsCard.add(MainFrame.createLabel(
                "Payslip", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY), BorderLayout.NORTH);
        resultsCard.add(payslipScrollPane, BorderLayout.CENTER);

        contentArea.add(infoCard, BorderLayout.WEST);
        contentArea.add(resultsCard, BorderLayout.CENTER);
        add(contentArea, BorderLayout.CENTER);
    }

    public JComboBox<String> getEmpIdSelectorDropdown() {
        return empIdSelectorDropdown;
    }

    public JLabel getEmpNumberDisplayLabel() {
        return empNumberDisplayLabel;
    }

    public JLabel getEmpNameDisplayLabel() {
        return empNameDisplayLabel;
    }

    public JLabel getEmpBirthdayDisplayLabel() {
        return empBirthdayDisplayLabel;
    }

    public JTextArea getEmpPayrollResultsArea() {
        return empPayrollResultsArea;
    }

    public JTextField getEmpPayCoverageField() {
        return empPayCoverageField;
    }

    public JButton getComputePayslipButton() {
        return computePayslipButton;
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }
}
