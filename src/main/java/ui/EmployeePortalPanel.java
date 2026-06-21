package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

// The employee's self-service panel. Displays their profile and lets them view their own payslip.
// Employees can only see their own records after logging in with their employee number.
public class EmployeePortalPanel extends JPanel {

    private final JLabel empNumberDisplayLabel;
    private final JLabel empNameDisplayLabel;
    private final JLabel empBirthdayDisplayLabel;
    private final JLabel empPositionDisplayLabel;
    private final JTextArea empPayrollResultsArea;
    private final JTextField empPayCoverageField;
    private final JButton computePayslipButton;
    private final JButton logoutButton;

    public EmployeePortalPanel() {
        super(new BorderLayout());
        setBackground(MainFrame.COLOR_BACKGROUND);
        add(MainFrame.buildApplicationHeader(
                "Employee Portal", "View your profile and payslip"), BorderLayout.NORTH);

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
                "My Profile", MainFrame.FONT_UI, Font.BOLD, 14, MainFrame.COLOR_TEXT_PRIMARY),
                infoConstraints);

        infoConstraints.gridy = 1;
        infoConstraints.insets = new Insets(8, 0, 12, 0);
        infoCard.add(MainFrame.createSeparatorLine(), infoConstraints);
        infoConstraints.insets = new Insets(4, 0, 4, 0);

        infoConstraints.gridy = 2;
        infoCard.add(MainFrame.createLabel(
                "Employee Number", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empNumberDisplayLabel = MainFrame.createLabel("-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 3;
        infoCard.add(empNumberDisplayLabel, infoConstraints);

        infoConstraints.gridy = 4;
        infoCard.add(MainFrame.createLabel(
                "Full Name", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empNameDisplayLabel = MainFrame.createLabel("-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 5;
        infoCard.add(empNameDisplayLabel, infoConstraints);

        infoConstraints.gridy = 6;
        infoCard.add(MainFrame.createLabel(
                "Position", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empPositionDisplayLabel = MainFrame.createLabel("-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 7;
        infoCard.add(empPositionDisplayLabel, infoConstraints);

        infoConstraints.gridy = 8;
        infoCard.add(MainFrame.createLabel(
                "Birthday", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empBirthdayDisplayLabel = MainFrame.createLabel("-", MainFrame.FONT_UI, Font.BOLD, 13, MainFrame.COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 9;
        infoCard.add(empBirthdayDisplayLabel, infoConstraints);

        infoConstraints.gridy = 10;
        infoConstraints.insets = new Insets(16, 0, 4, 0);
        infoCard.add(MainFrame.createLabel(
                "Pay Coverage Month (6-12)", MainFrame.FONT_UI, Font.PLAIN, 11, MainFrame.COLOR_TEXT_SECONDARY),
                infoConstraints);
        empPayCoverageField = MainFrame.createStyledTextField();
        empPayCoverageField.setToolTipText("Enter a month number: 6 = June, 7 = July ... 12 = December");
        infoConstraints.gridy = 11;
        infoConstraints.insets = new Insets(4, 0, 8, 0);
        infoCard.add(empPayCoverageField, infoConstraints);

        computePayslipButton = MainFrame.createStyledButton("View My Payslip", MainFrame.COLOR_ACCENT_PRIMARY);
        infoConstraints.gridy = 12;
        infoConstraints.insets = new Insets(4, 0, 4, 0);
        infoCard.add(computePayslipButton, infoConstraints);

        logoutButton = MainFrame.createStyledButton("Log Out", MainFrame.COLOR_ACCENT_DANGER);
        infoConstraints.gridy = 13;
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

    public JLabel getEmpNumberDisplayLabel() {
        return empNumberDisplayLabel;
    }

    public JLabel getEmpNameDisplayLabel() {
        return empNameDisplayLabel;
    }

    public JLabel getEmpBirthdayDisplayLabel() {
        return empBirthdayDisplayLabel;
    }

    public JLabel getEmpPositionDisplayLabel() {
        return empPositionDisplayLabel;
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
