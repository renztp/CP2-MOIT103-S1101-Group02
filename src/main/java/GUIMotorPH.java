import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * MotorPH Payroll System — GUI
 *
 * Login Roles:
 * - Username: employee      | Password: 12345 (Select any Employee ID inside to view details + payslip)
 * - Username: payroll_staff | Password: 12345 (Access to structural roster + multi-payroll generation)
 *
 * Payroll covers June through December. Deductions are computed from the combined
 * monthly gross (cutoff 1 + cutoff 2) and applied in full on the second cutoff.
 */
public class GUIMotorPH {

    static final String EMPLOYEE_FILE_PATH   = "Employee Details.csv";
    static final String ATTENDANCE_FILE_PATH = "Attendance Record.csv";

    static final String ROLE_EMPLOYEE       = "employee";
    static final String ROLE_PAYROLL_STAFF  = "payroll_staff";
    static final String VALID_PASSWORD      = "12345";

    static final int PAYROLL_MONTH_START = 6;
    static final int PAYROLL_MONTH_END   = 12;

    static final int WORK_START_MINUTES        = 8 * 60;       // 8:00 AM
    static final int WORK_END_MINUTES          = 17 * 60;      // 5:00 PM
    static final int GRACE_PERIOD_END_MINUTES  = 8 * 60 + 10;  // 8:10 AM
    static final int LUNCH_BREAK_MINUTES       = 60;

    static final double SSS_BRACKET_BASE_SALARY      = 3250.0;
    static final double SSS_BRACKET_STEP             = 500.0;
    static final double SSS_BRACKET_BASE_CONTRIBUTION = 157.5;
    static final double SSS_BRACKET_STEP_AMOUNT      = 22.5;
    static final double SSS_MINIMUM_SALARY           = 3250.0;
    static final double SSS_MINIMUM_CONTRIBUTION     = 135.0;
    static final double SSS_MAXIMUM_SALARY           = 24750.0;
    static final double SSS_MAXIMUM_CONTRIBUTION     = 1125.0;

    static final double PHILHEALTH_RATE              = 0.03;
    static final double PHILHEALTH_MINIMUM_SALARY    = 10000.0;
    static final double PHILHEALTH_MINIMUM_PREMIUM   = 150.0;
    static final double PHILHEALTH_MAXIMUM_SALARY    = 60000.0;
    static final double PHILHEALTH_MAXIMUM_PREMIUM   = 900.0;

    static final double PAGIBIG_LOW_RATE             = 0.01;
    static final double PAGIBIG_HIGH_RATE            = 0.02;
    static final double PAGIBIG_LOW_SALARY_THRESHOLD = 1500.0;
    static final double PAGIBIG_MAXIMUM_CONTRIBUTION = 100.0;

    static final double TAX_BRACKET_1_MAX = 20832.0;
    static final double TAX_BRACKET_2_MAX = 33332.0;
    static final double TAX_BRACKET_3_MAX = 66666.0;
    static final double TAX_BRACKET_4_MAX = 166666.0;
    static final double TAX_BRACKET_5_MAX = 666666.0;

    static final int COL_EMP_ID       = 0;
    static final int COL_LAST_NAME    = 1;
    static final int COL_FIRST_NAME   = 2;
    static final int COL_BIRTHDAY     = 3;
    static final int COL_HOURLY_RATE  = 18;
    static final int COL_MINIMUM_REQUIRED_COLUMNS = 19;

    static final int ATT_EMP_ID   = 0;
    static final int ATT_DATE     = 3;
    static final int ATT_TIME_IN  = 4;
    static final int ATT_TIME_OUT = 5;
    static final int ATT_MINIMUM_REQUIRED_COLUMNS = 6;

    static final int PAY_FIRST_CUTOFF_GROSS  = 0;
    static final int PAY_SECOND_CUTOFF_GROSS = 1;
    static final int PAY_TOTAL_MONTHLY_GROSS = 2;
    static final int PAY_SSS                 = 3;
    static final int PAY_PHILHEALTH          = 4;
    static final int PAY_PAGIBIG             = 5;
    static final int PAY_WITHHOLDING_TAX     = 6;
    static final int PAY_TOTAL_DEDUCTIONS    = 7;
    static final int PAY_FIRST_CUTOFF_NET    = 8;
    static final int PAY_SECOND_CUTOFF_NET   = 9;

    static final String[] TABLE_COLUMN_HEADERS = {
            "Emp #", "Last Name", "First Name", "Birthday"
    };
    static final int[] TABLE_COLUMN_CSV_INDICES = {
            COL_EMP_ID, COL_LAST_NAME, COL_FIRST_NAME, COL_BIRTHDAY
    };

    static final int WINDOW_WIDTH  = 1000;
    static final int WINDOW_HEIGHT = 680;

    static final Color COLOR_BACKGROUND       = new Color(15, 23, 42);
    static final Color COLOR_SURFACE          = new Color(30, 41, 59);
    static final Color COLOR_CARD             = new Color(36, 46, 68);
    static final Color COLOR_ACCENT_PRIMARY   = new Color(59, 130, 246);
    static final Color COLOR_ACCENT_SUCCESS   = new Color(16, 185, 129);
    static final Color COLOR_ACCENT_DANGER    = new Color(239, 68, 68);
    static final Color COLOR_ACCENT_WARNING   = new Color(245, 158, 11);
    static final Color COLOR_TEXT_PRIMARY     = new Color(226, 232, 240);
    static final Color COLOR_TEXT_SECONDARY   = new Color(148, 163, 184);
    static final Color COLOR_BORDER           = new Color(51, 65, 85);
    static final Color COLOR_INPUT_BACKGROUND = new Color(15, 23, 42);
    static final Color COLOR_TABLE_ROW_ODD    = new Color(30, 41, 59);
    static final Color COLOR_TABLE_ROW_EVEN   = new Color(36, 46, 68);
    static final Color COLOR_TABLE_ROW_SEL    = new Color(59, 130, 246, 80);

    static final String FONT_UI   = "Segoe UI";
    static final String FONT_MONO = "Consolas";

    static final String PAYSLIP_SEPARATOR_MAJOR = "=".repeat(55);
    static final String PAYSLIP_SEPARATOR_MINOR = "-".repeat(55);

    static JFrame      applicationWindow;
    static JPanel      rootContentPanel;
    static CardLayout  screenNavigator;

    // Login screen fields
    static JTextField  loginUsernameField;
    static JPasswordField loginPasswordField;
    static JLabel      loginErrorLabel;

    // Employee screen fields
    static JComboBox<String> empIdSelectorDropdown;
    static JLabel      empNumberDisplayLabel;
    static JLabel      empNameDisplayLabel;
    static JLabel      empBirthdayDisplayLabel;
    static JTextArea   empPayrollResultsArea;
    static JTextField  empPayCoverageField;

    // Payroll staff screen fields
    static JTable            staffRosterTable;
    static DefaultTableModel staffRosterModel;
    static JTextArea         staffPayrollResultsArea;
    static JLabel            staffStatusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MotorPHGUI::buildAndLaunchApplication);
    }

    static void buildAndLaunchApplication() {
        applicationWindow = new JFrame("MotorPH Payroll System");
        applicationWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        applicationWindow.setLocationRelativeTo(null);
        applicationWindow.setResizable(false);

        screenNavigator = new CardLayout();
        rootContentPanel = new JPanel(screenNavigator);
        rootContentPanel.setBackground(COLOR_BACKGROUND);

        rootContentPanel.add(buildLoginPanel(),    "LOGIN");
        rootContentPanel.add(buildEmployeePanel(), "EMPLOYEE");
        rootContentPanel.add(buildStaffPanel(),    "STAFF");

        applicationWindow.setContentPane(rootContentPanel);
        applicationWindow.setVisible(true);

        navigateToScreen("LOGIN");
    }

    static void navigateToScreen(String screenName) {
        screenNavigator.show(rootContentPanel, screenName);
    }

    static JPanel buildLoginPanel() {
        JPanel outerPanel = createStyledPanel(new GridBagLayout(), COLOR_BACKGROUND);

        JPanel loginCard = createStyledPanel(new GridBagLayout(), COLOR_SURFACE);
        loginCard.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(40, 48, 40, 48)
        ));
        loginCard.setPreferredSize(new Dimension(400, 340));

        GridBagConstraints cardConstraints = new GridBagConstraints();
        cardConstraints.fill = GridBagConstraints.HORIZONTAL;
        cardConstraints.insets = new Insets(6, 0, 6, 0);
        cardConstraints.gridx = 0;
        cardConstraints.weightx = 1;

        JLabel titleLabel = createLabel("MotorPH Payroll System", FONT_UI, Font.BOLD, 20, COLOR_TEXT_PRIMARY);
        JLabel subtitleLabel = createLabel("Sign in to continue", FONT_UI, Font.PLAIN, 13, COLOR_TEXT_SECONDARY);

        cardConstraints.gridy = 0;
        loginCard.add(titleLabel, cardConstraints);
        cardConstraints.gridy = 1;
        cardConstraints.insets = new Insets(0, 0, 20, 0);
        loginCard.add(subtitleLabel, cardConstraints);
        cardConstraints.insets = new Insets(6, 0, 6, 0);

        cardConstraints.gridy = 2;
        loginCard.add(createLabel("Username", FONT_UI, Font.PLAIN, 12, COLOR_TEXT_SECONDARY), cardConstraints);
        loginUsernameField = createStyledTextField();
        cardConstraints.gridy = 3;
        loginCard.add(loginUsernameField, cardConstraints);

        cardConstraints.gridy = 4;
        loginCard.add(createLabel("Password", FONT_UI, Font.PLAIN, 12, COLOR_TEXT_SECONDARY), cardConstraints);
        loginPasswordField = new JPasswordField();
        styleTextField(loginPasswordField);
        cardConstraints.gridy = 5;
        loginCard.add(loginPasswordField, cardConstraints);

        loginErrorLabel = createLabel("", FONT_UI, Font.PLAIN, 12, COLOR_ACCENT_DANGER);
        loginErrorLabel.setVisible(false);
        cardConstraints.gridy = 6;
        loginCard.add(loginErrorLabel, cardConstraints);

        JButton loginButton = createStyledButton("Sign In", COLOR_ACCENT_PRIMARY);
        cardConstraints.gridy = 7;
        cardConstraints.insets = new Insets(12, 0, 0, 0);
        loginCard.add(loginButton, cardConstraints);

        loginButton.addActionListener(e -> handleLoginAttempt());
        loginPasswordField.addActionListener(e -> handleLoginAttempt());

        outerPanel.add(loginCard);
        return outerPanel;
    }

    static JPanel buildEmployeePanel() {
        JPanel outerPanel = createStyledPanel(new BorderLayout(), COLOR_BACKGROUND);
        outerPanel.add(buildApplicationHeader("Employee Portal", "Select an employee to view details and payslip"), BorderLayout.NORTH);

        JPanel contentArea = createStyledPanel(new BorderLayout(16, 16), COLOR_BACKGROUND);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel infoCard = createStyledPanel(new GridBagLayout(), COLOR_SURFACE);
        infoCard.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        infoCard.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints infoConstraints = new GridBagConstraints();
        infoConstraints.fill = GridBagConstraints.HORIZONTAL;
        infoConstraints.gridx = 0;
        infoConstraints.weightx = 1;
        infoConstraints.insets = new Insets(4, 0, 4, 0);

        infoConstraints.gridy = 0;
        infoCard.add(createLabel("Select Employee Account", FONT_UI, Font.BOLD, 14, COLOR_TEXT_PRIMARY), infoConstraints);

        // Employee Selector Dropdown Component
        empIdSelectorDropdown = new JComboBox<>();
        empIdSelectorDropdown.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        empIdSelectorDropdown.setBackground(COLOR_INPUT_BACKGROUND);
        empIdSelectorDropdown.setForeground(COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 1;
        infoConstraints.insets = new Insets(6, 0, 12, 0);
        infoCard.add(empIdSelectorDropdown, infoConstraints);
        infoConstraints.insets = new Insets(4, 0, 4, 0);

        infoConstraints.gridy = 2;
        infoCard.add(createSeparatorLine(), infoConstraints);

        infoConstraints.gridy = 3;
        infoCard.add(createLabel("Employee Number", FONT_UI, Font.PLAIN, 11, COLOR_TEXT_SECONDARY), infoConstraints);
        empNumberDisplayLabel = createLabel("—", FONT_UI, Font.BOLD, 13, COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 4;
        infoCard.add(empNumberDisplayLabel, infoConstraints);

        infoConstraints.gridy = 5;
        infoCard.add(createLabel("Full Name", FONT_UI, Font.PLAIN, 11, COLOR_TEXT_SECONDARY), infoConstraints);
        empNameDisplayLabel = createLabel("—", FONT_UI, Font.BOLD, 13, COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 6;
        infoCard.add(empNameDisplayLabel, infoConstraints);

        infoConstraints.gridy = 7;
        infoCard.add(createLabel("Birthday", FONT_UI, Font.PLAIN, 11, COLOR_TEXT_SECONDARY), infoConstraints);
        empBirthdayDisplayLabel = createLabel("—", FONT_UI, Font.BOLD, 13, COLOR_TEXT_PRIMARY);
        infoConstraints.gridy = 8;
        infoCard.add(empBirthdayDisplayLabel, infoConstraints);

        infoConstraints.gridy = 9;
        infoConstraints.insets = new Insets(16, 0, 4, 0);
        infoCard.add(createLabel("Pay Coverage Month (6–12)", FONT_UI, Font.PLAIN, 11, COLOR_TEXT_SECONDARY), infoConstraints);
        empPayCoverageField = createStyledTextField();
        empPayCoverageField.setToolTipText("Enter a month number: 6 = June, 7 = July ... 12 = December");
        infoConstraints.gridy = 10;
        infoConstraints.insets = new Insets(4, 0, 8, 0);
        infoCard.add(empPayCoverageField, infoConstraints);

        JButton computePayslipButton = createStyledButton("View My Payslip", COLOR_ACCENT_PRIMARY);
        infoConstraints.gridy = 11;
        infoConstraints.insets = new Insets(4, 0, 4, 0);
        infoCard.add(computePayslipButton, infoConstraints);

        JButton logoutButton = createStyledButton("Log Out", COLOR_ACCENT_DANGER);
        infoConstraints.gridy = 12;
        infoConstraints.insets = new Insets(12, 0, 0, 0);
        infoCard.add(logoutButton, infoConstraints);

        empPayrollResultsArea = createStyledTextArea("Your payslip will appear here after you click 'View My Payslip'.");
        JScrollPane payslipScrollPane = createStyledScrollPane(empPayrollResultsArea);

        JPanel resultsCard = createStyledPanel(new BorderLayout(0, 8), COLOR_BACKGROUND);
        resultsCard.add(createLabel("Payslip", FONT_UI, Font.BOLD, 14, COLOR_TEXT_PRIMARY), BorderLayout.NORTH);
        resultsCard.add(payslipScrollPane, BorderLayout.CENTER);

        // CONTROL — Action Listeners for Employee Interactions
        empIdSelectorDropdown.addActionListener(e -> handleEmployeeSelectionChange());
        computePayslipButton.addActionListener(e -> handleEmployeePayslipRequest());
        logoutButton.addActionListener(e -> handleLogout());

        contentArea.add(infoCard, BorderLayout.WEST);
        contentArea.add(resultsCard, BorderLayout.CENTER);
        outerPanel.add(contentArea, BorderLayout.CENTER);
        return outerPanel;
    }

    static JPanel buildStaffPanel() {
        JPanel outerPanel = createStyledPanel(new BorderLayout(), COLOR_BACKGROUND);
        outerPanel.add(buildApplicationHeader("Payroll Staff Portal", "Generate employee payroll reports"), BorderLayout.NORTH);

        JPanel contentArea = createStyledPanel(new BorderLayout(16, 16), COLOR_BACKGROUND);
        contentArea.setBorder(new EmptyBorder(16, 16, 16, 16));

        staffRosterModel = new DefaultTableModel(TABLE_COLUMN_HEADERS, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        staffRosterTable = new JTable(staffRosterModel);
        applyTableStyling(staffRosterTable);

        JScrollPane rosterScrollPane = createStyledScrollPane(staffRosterTable);
        rosterScrollPane.setPreferredSize(new Dimension(320, 0));

        JButton loadRosterButton         = createStyledButton("Load Employee Roster", COLOR_ACCENT_PRIMARY);
        JButton singlePayslipButton      = createStyledButton("Generate Selected Payslip", COLOR_ACCENT_SUCCESS);
        JButton allEmployeesPayrollButton = createStyledButton("Generate All-Employee Payroll", COLOR_ACCENT_WARNING);
        JButton staffLogoutButton         = createStyledButton("Log Out", COLOR_ACCENT_DANGER);

        staffStatusLabel = createLabel("", FONT_UI, Font.ITALIC, 11, COLOR_TEXT_SECONDARY);

        JPanel actionButtonPanel = createStyledPanel(new GridLayout(0, 1, 0, 8), COLOR_BACKGROUND);
        actionButtonPanel.add(loadRosterButton);
        actionButtonPanel.add(singlePayslipButton);
        actionButtonPanel.add(allEmployeesPayrollButton);
        actionButtonPanel.add(staffLogoutButton);
        actionButtonPanel.add(staffStatusLabel);

        JPanel rosterPanel = createStyledPanel(new BorderLayout(0, 10), COLOR_BACKGROUND);
        rosterPanel.add(createLabel("Employee Roster", FONT_UI, Font.BOLD, 14, COLOR_TEXT_PRIMARY), BorderLayout.NORTH);
        rosterPanel.add(rosterScrollPane, BorderLayout.CENTER);
        rosterPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        staffPayrollResultsArea = createStyledTextArea("Select an employee and click a Generate button to produce a payroll report. Double-click any row to view full file details.");
        JScrollPane staffResultsScrollPane = createStyledScrollPane(staffPayrollResultsArea);

        JPanel staffResultsPanel = createStyledPanel(new BorderLayout(0, 8), COLOR_BACKGROUND);
        staffResultsPanel.add(createLabel("Payroll Report", FONT_UI, Font.BOLD, 14, COLOR_TEXT_PRIMARY), BorderLayout.NORTH);
        staffResultsPanel.add(staffResultsScrollPane, BorderLayout.CENTER);

        loadRosterButton.addActionListener(e -> handleLoadEmployeeRoster());
        singlePayslipButton.addActionListener(e -> handleSingleEmployeePayrollRequest());
        allEmployeesPayrollButton.addActionListener(e -> handleAllEmployeesPayrollRequest());
        staffLogoutButton.addActionListener(e -> handleLogout());

        staffRosterTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewEmployeeRecord();
                }
            }
        });

        contentArea.add(rosterPanel, BorderLayout.WEST);
        contentArea.add(staffResultsPanel, BorderLayout.CENTER);
        outerPanel.add(contentArea, BorderLayout.CENTER);
        return outerPanel;
    }

    /** Login configuration handling specific matching roles ("employee" or "payroll_staff") */
    static void handleLoginAttempt() {
        String enteredUsername = loginUsernameField.getText().trim();
        String enteredPassword = new String(loginPasswordField.getPassword()).trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            displayLoginError("Username and password are required.");
            return;
        }

        if (!validateUserCredentials(enteredUsername, enteredPassword)) {
            displayLoginError("Incorrect username or password.");
            loginPasswordField.setText("");
            return;
        }

        loginErrorLabel.setVisible(false);
        loginUsernameField.setText("");
        loginPasswordField.setText("");

        if (enteredUsername.equalsIgnoreCase(ROLE_EMPLOYEE)) {
            initializeEmployeeIdSelector();
            navigateToScreen("EMPLOYEE");
        } else {
            navigateToScreen("STAFF");
        }
    }

    static void handleLogout() {
        clearEmployeePanelState();
        navigateToScreen("LOGIN");
    }

    /** Loads list of employee IDs into dropdown when logging in as general employee account */
    static void initializeEmployeeIdSelector() {
        empIdSelectorDropdown.removeAllItems();
        try {
            String[][] employeeRecords = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
            for (String[] row : employeeRecords) {
                empIdSelectorDropdown.addItem(row[COL_EMP_ID].trim());
            }
            if (empIdSelectorDropdown.getItemCount() > 0) {
                empIdSelectorDropdown.setSelectedIndex(0);
                handleEmployeeSelectionChange();
            }
        } catch (Exception ex) {
            showErrorDialog("Could not index employee profile lists: " + ex.getMessage());
        }
    }

    /** Event handler when employee switches target viewer context from the dropdown selection list */
    static void handleEmployeeSelectionChange() {
        Object selected = empIdSelectorDropdown.getSelectedItem();
        if (selected == null) return;

        String targetId = selected.toString();
        populateEmployeePanelForUser(targetId);
    }

    static void handleEmployeePayslipRequest() {
        String coverageInput = empPayCoverageField.getText().trim();

        if (!validateMonthInput(coverageInput)) {
            showWarningDialog("Pay Coverage must be a month number between 6 (June) and 12 (December).");
            return;
        }

        int selectedMonth = Integer.parseInt(coverageInput);
        String employeeId = empNumberDisplayLabel.getText();

        if (employeeId.equals("—")) {
            showWarningDialog("Please select a valid Employee ID from the dropdown.");
            return;
        }

        try {
            String[][] attendanceRecords = loadAttendanceFromFile(ATTENDANCE_FILE_PATH);
            String[][] employeeRecords   = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);

            String[] matchedEmployee = findEmployeeById(employeeRecords, employeeId);
            if (matchedEmployee == null) {
                showErrorDialog("Employee record not found in the file.");
                return;
            }

            double hourlyRate  = parseNumericAmount(matchedEmployee[COL_HOURLY_RATE]);
            int    payrollYear = resolveYearFromAttendance(attendanceRecords, employeeId);

            String payslipText = buildPayslipForEmployee(matchedEmployee, attendanceRecords, selectedMonth, payrollYear, hourlyRate);
            empPayrollResultsArea.setText(payslipText);
            empPayrollResultsArea.setCaretPosition(0);

        } catch (Exception loadException) {
            showErrorDialog("Failed to load data: " + loadException.getMessage());
        }
    }

    static void handleLoadEmployeeRoster() {
        try {
            String[][] employeeRecords = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
            populateRosterTable(employeeRecords);
            updateStaffStatusLabel(employeeRecords.length + " employees loaded. Double click rows to view profiles.");
        } catch (Exception loadException) {
            showErrorDialog("Failed to load employee file: " + loadException.getMessage());
        }
    }

    static void handleSingleEmployeePayrollRequest() {
        int selectedTableRow = staffRosterTable.getSelectedRow();

        if (selectedTableRow == -1) {
            showWarningDialog("Please select an employee from the roster table first.");
            return;
        }

        String selectedEmployeeId = staffRosterModel.getValueAt(selectedTableRow, 0).toString();

        try {
            String[][] employeeRecords   = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
            String[][] attendanceRecords = loadAttendanceFromFile(ATTENDANCE_FILE_PATH);

            String[] matchedEmployee = findEmployeeById(employeeRecords, selectedEmployeeId);
            if (matchedEmployee == null) {
                showErrorDialog("Employee record not found.");
                return;
            }

            String payrollReport = buildFullPayrollReportForEmployee(matchedEmployee, attendanceRecords);
            staffPayrollResultsArea.setText(payrollReport);
            staffPayrollResultsArea.setCaretPosition(0);
            updateStaffStatusLabel("Payslip generated for employee " + selectedEmployeeId + ".");

        } catch (Exception loadException) {
            showErrorDialog("Failed to generate payslip: " + loadException.getMessage());
        }
    }

    static void handleAllEmployeesPayrollRequest() {
        try {
            String[][] employeeRecords   = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
            String[][] attendanceRecords = loadAttendanceFromFile(ATTENDANCE_FILE_PATH);

            if (employeeRecords.length == 0) {
                showWarningDialog("No employee records found. Please load the roster first.");
                return;
            }

            String combinedReport = Arrays.stream(employeeRecords)
                    .map(employee -> buildFullPayrollReportForEmployee(employee, attendanceRecords))
                    .collect(Collectors.joining("\n\n" + "=".repeat(55) + "\n\n"));

            staffPayrollResultsArea.setText(combinedReport);
            staffPayrollResultsArea.setCaretPosition(0);
            updateStaffStatusLabel("All-employee payroll generated (" + employeeRecords.length + " employees).");

        } catch (Exception loadException) {
            showErrorDialog("Failed to generate payroll: " + loadException.getMessage());
        }
    }

    static void handleViewEmployeeRecord() {
        int selectedTableRow = staffRosterTable.getSelectedRow();
        if (selectedTableRow == -1) return;

        String selectedEmployeeId = staffRosterModel.getValueAt(selectedTableRow, 0).toString();

        try {
            String[][] employeeRecords = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
            String[] employee = findEmployeeById(employeeRecords, selectedEmployeeId);

            if (employee == null) {
                showErrorDialog("Details could not be fetched for this record.");
                return;
            }

            StringBuilder profileBuilder = new StringBuilder();
            profileBuilder.append("MOTORPH EMPLOYEE PROFILE RECORD\n");
            profileBuilder.append("=".repeat(45)).append("\n\n");

            String[] labels = {
                    "Employee ID", "Last Name", "First Name", "Birthday", "Address",
                    "Phone Number", "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #",
                    "Status", "Position", "Immediate Supervisor", "Basic Salary",
                    "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
                    "Gross Semi-Monthly Rate", "Hourly Rate"
            };

            for (int i = 0; i < labels.length; i++) {
                String value = safeGetArrayElement(employee, i);
                if (!value.isEmpty()) {
                    profileBuilder.append(String.format("%-25s: %s%n", labels[i], value));
                }
            }

            JTextArea recordTextArea = createStyledTextArea(profileBuilder.toString());
            recordTextArea.setFont(new Font(FONT_MONO, Font.PLAIN, 13));
            JScrollPane scrollPane = createStyledScrollPane(recordTextArea);
            scrollPane.setPreferredSize(new Dimension(500, 450));

            JOptionPane.showMessageDialog(applicationWindow, scrollPane,
                    "Employee Record — ID " + selectedEmployeeId, JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            showErrorDialog("Could not query record: " + ex.getMessage());
        }
    }

    static String buildFullPayrollReportForEmployee(String[] employeeRow, String[][] attendanceRecords) {
        double hourlyRate  = parseNumericAmount(employeeRow[COL_HOURLY_RATE]);
        int    payrollYear = resolveYearFromAttendance(attendanceRecords, employeeRow[COL_EMP_ID]);

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        reportBuilder.append(String.format("   EMPLOYEE: %s, %s  (ID: %s)%n",
                employeeRow[COL_LAST_NAME],
                employeeRow[COL_FIRST_NAME],
                employeeRow[COL_EMP_ID]));
        reportBuilder.append(String.format("   Birthday : %s%n", employeeRow[COL_BIRTHDAY]));
        reportBuilder.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");

        IntStream.rangeClosed(PAYROLL_MONTH_START, PAYROLL_MONTH_END).forEach(month -> {
            int    lastDayOfMonth    = resolveLastDayOfMonth(month, payrollYear);
            double firstCutoffHours  = calculateWorkedHoursInRange(attendanceRecords, employeeRow[COL_EMP_ID], month, 1, 15);
            double secondCutoffHours = calculateWorkedHoursInRange(attendanceRecords, employeeRow[COL_EMP_ID], month, 16, lastDayOfMonth);

            double[] payrollValues = computeMonthlyPayroll(firstCutoffHours, secondCutoffHours, hourlyRate);

            reportBuilder.append(formatMonthlyPayrollBlock(month, lastDayOfMonth, firstCutoffHours, secondCutoffHours, payrollValues));
        });

        return reportBuilder.toString();
    }

    static String buildPayslipForEmployee(
            String[] employeeRow,
            String[][] attendanceRecords,
            int targetMonth,
            int payrollYear,
            double hourlyRate) {

        int    lastDayOfMonth    = resolveLastDayOfMonth(targetMonth, payrollYear);
        double firstCutoffHours  = calculateWorkedHoursInRange(attendanceRecords, employeeRow[COL_EMP_ID], targetMonth, 1, 15);
        double secondCutoffHours = calculateWorkedHoursInRange(attendanceRecords, employeeRow[COL_EMP_ID], targetMonth, 16, lastDayOfMonth);

        double[] payrollValues = computeMonthlyPayroll(firstCutoffHours, secondCutoffHours, hourlyRate);

        StringBuilder payslip = new StringBuilder();
        payslip.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        payslip.append(String.format("   PAYSLIP — %s%n", resolveMonthName(targetMonth).toUpperCase()));
        payslip.append(String.format("   Employee : %s, %s  (ID: %s)%n",
                employeeRow[COL_LAST_NAME],
                employeeRow[COL_FIRST_NAME],
                employeeRow[COL_EMP_ID]));
        payslip.append(String.format("   Hourly Rate : %s%n", hourlyRate));
        payslip.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        payslip.append(formatMonthlyPayrollBlock(targetMonth, lastDayOfMonth, firstCutoffHours, secondCutoffHours, payrollValues));

        return payslip.toString();
    }

    static double[] computeMonthlyPayroll(double firstCutoffHours, double secondCutoffHours, double hourlyRate) {
        double firstCutoffGross  = firstCutoffHours  * hourlyRate;
        double secondCutoffGross = secondCutoffHours * hourlyRate;

        double totalMonthlyGross = firstCutoffGross + secondCutoffGross;

        double sssContribution        = (totalMonthlyGross > 0) ? computeSSSContribution(totalMonthlyGross)       : 0.0;
        double philHealthContribution = (totalMonthlyGross > 0) ? computePhilHealthContribution(totalMonthlyGross) : 0.0;
        double pagIbigContribution    = (totalMonthlyGross > 0) ? computePagIbigContribution(totalMonthlyGross)    : 0.0;

        double totalGovernmentDeductions = sssContribution + philHealthContribution + pagIbigContribution;
        double taxableIncome             = totalMonthlyGross - totalGovernmentDeductions;
        double withholdingTax            = (taxableIncome > 0) ? computeWithholdingTax(taxableIncome) : 0.0;

        double totalDeductions = totalGovernmentDeductions + withholdingTax;

        double firstCutoffNetPay  = firstCutoffGross;
        double secondCutoffNetPay = secondCutoffGross - totalDeductions;

        return new double[]{
                firstCutoffGross,
                secondCutoffGross,
                totalMonthlyGross,
                sssContribution,
                philHealthContribution,
                pagIbigContribution,
                withholdingTax,
                totalDeductions,
                firstCutoffNetPay,
                secondCutoffNetPay
        };
    }

    static double calculateWorkedHoursInRange(
            String[][] attendanceRecords,
            String targetEmployeeId,
            int targetMonth,
            int rangeStartDay,
            int rangeEndDay) {

        long totalMinutesWorked = Arrays.stream(attendanceRecords)
                .filter(row -> row.length > ATT_TIME_OUT)
                .filter(row -> row[ATT_EMP_ID].trim().equals(targetEmployeeId))
                .filter(row -> {
                    String[] dateParts = row[ATT_DATE].split("/");
                    if (dateParts.length < 2) return false;
                    int recordMonth = Integer.parseInt(dateParts[0].trim());
                    int recordDay   = Integer.parseInt(dateParts[1].trim());
                    return recordMonth == targetMonth
                            && recordDay >= rangeStartDay
                            && recordDay <= rangeEndDay;
                })
                .mapToLong(row -> {
                    int[] loginTime  = parseTimeToHoursMinutes(row[ATT_TIME_IN]);
                    int[] logoutTime = parseTimeToHoursMinutes(row[ATT_TIME_OUT]);

                    int loginMinutes  = loginTime[0]  * 60 + loginTime[1];
                    int logoutMinutes = logoutTime[0] * 60 + logoutTime[1];

                    if (loginMinutes <= GRACE_PERIOD_END_MINUTES) {
                        loginMinutes = WORK_START_MINUTES;
                    }
                    if (loginMinutes < WORK_START_MINUTES) {
                        loginMinutes = WORK_START_MINUTES;
                    }
                    if (logoutMinutes > WORK_END_MINUTES) {
                        logoutMinutes = WORK_END_MINUTES;
                    }

                    int effectiveMinutesWorked = logoutMinutes - loginMinutes - LUNCH_BREAK_MINUTES;
                    return Math.max(effectiveMinutesWorked, 0);
                })
                .sum();

        return totalMinutesWorked / 60.0;
    }

    static double computeSSSContribution(double grossSalary) {
        if (grossSalary < SSS_MINIMUM_SALARY)  return SSS_MINIMUM_CONTRIBUTION;
        if (grossSalary >= SSS_MAXIMUM_SALARY) return SSS_MAXIMUM_CONTRIBUTION;
        int salaryBracket = (int) ((grossSalary - SSS_BRACKET_BASE_SALARY) / SSS_BRACKET_STEP);
        return SSS_BRACKET_BASE_CONTRIBUTION + salaryBracket * SSS_BRACKET_STEP_AMOUNT;
    }

    static double computePhilHealthContribution(double grossSalary) {
        if (grossSalary <= PHILHEALTH_MINIMUM_SALARY) return PHILHEALTH_MINIMUM_PREMIUM;
        if (grossSalary >= PHILHEALTH_MAXIMUM_SALARY) return PHILHEALTH_MAXIMUM_PREMIUM;
        return (grossSalary * PHILHEALTH_RATE) / 2.0;
    }

    static double computePagIbigContribution(double grossSalary) {
        double contributionRate = (grossSalary <= PAGIBIG_LOW_SALARY_THRESHOLD)
                ? PAGIBIG_LOW_RATE
                : PAGIBIG_HIGH_RATE;
        return Math.min(grossSalary * contributionRate, PAGIBIG_MAXIMUM_CONTRIBUTION);
    }

    static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= TAX_BRACKET_1_MAX) return 0.0;
        if (taxableIncome <= TAX_BRACKET_2_MAX) return (taxableIncome - 20833)  * 0.20;
        if (taxableIncome <= TAX_BRACKET_3_MAX) return 2500.0   + (taxableIncome - 33333)  * 0.25;
        if (taxableIncome <= TAX_BRACKET_4_MAX) return 10833.0  + (taxableIncome - 66667)  * 0.30;
        if (taxableIncome <= TAX_BRACKET_5_MAX) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666677) * 0.35;
    }

    static String[][] loadEmployeesFromFile(String filePath) throws Exception {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(filePath))) {
            return csvReader.lines()
                    .skip(1)
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .filter(columns -> columns.length >= COL_MINIMUM_REQUIRED_COLUMNS)
                    .map(columns -> {
                        String[] cleaned = Arrays.stream(columns)
                                .map(cell -> cell.replace("\"", "").trim())
                                .toArray(String[]::new);
                        cleaned[COL_HOURLY_RATE] = cleaned[COL_HOURLY_RATE].replace(",", "");
                        return cleaned;
                    })
                    .toArray(String[][]::new);
        }
    }

    static String[][] loadAttendanceFromFile(String filePath) throws Exception {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(filePath))) {
            return csvReader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(columns -> columns.length >= ATT_MINIMUM_REQUIRED_COLUMNS)
                    .toArray(String[][]::new);
        }
    }

    /** Reverted to simple static validation checks for role configuration strings */
    static boolean validateUserCredentials(String username, String password) {
        boolean isKnownUsername = username.equalsIgnoreCase(ROLE_EMPLOYEE) || username.equalsIgnoreCase(ROLE_PAYROLL_STAFF);
        boolean isCorrectPassword = password.equals(VALID_PASSWORD);
        return isKnownUsername && isCorrectPassword;
    }

    static boolean validateMonthInput(String rawInput) {
        try {
            int parsedMonth = Integer.parseInt(rawInput);
            return parsedMonth >= PAYROLL_MONTH_START && parsedMonth <= PAYROLL_MONTH_END;
        } catch (NumberFormatException invalidInput) {
            return false;
        }
    }

    static String[] findEmployeeById(String[][] employeeRecords, String targetId) {
        return Arrays.stream(employeeRecords)
                .filter(row -> row[COL_EMP_ID].trim().equals(targetId))
                .findFirst()
                .orElse(null);
    }

    static int resolveYearFromAttendance(String[][] attendanceRecords, String employeeId) {
        return Arrays.stream(attendanceRecords)
                .filter(row -> row[ATT_EMP_ID].trim().equals(employeeId))
                .map(row -> row[ATT_DATE].split("/"))
                .filter(parts -> parts.length >= 3)
                .mapToInt(parts -> Integer.parseInt(parts[2].trim()))
                .findFirst()
                .orElse(2024);
    }

    static int resolveLastDayOfMonth(int month, int year) {
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
            return isLeapYear ? 29 : 28;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) return 30;
        return 31;
    }

    static String resolveMonthName(int monthNumber) {
        String[] monthNames = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return monthNames[monthNumber];
    }

    static int[] parseTimeToHoursMinutes(String timeString) {
        String[] parts = timeString.trim().split(":");
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        };
    }

    static double parseNumericAmount(String rawValue) {
        return Double.parseDouble(rawValue.replace("\"", "").replace(",", "").trim());
    }

    static String safeGetArrayElement(String[] arr, int index) {
        return (index < arr.length) ? arr[index] : "";
    }

    static String formatMonthlyPayrollBlock(
            int month,
            int lastDayOfMonth,
            double firstCutoffHours,
            double secondCutoffHours,
            double[] payrollValues) {

        String monthName = resolveMonthName(month);
        StringBuilder block = new StringBuilder();

        block.append(String.format("%n   ─── %s ───%n", monthName));

        block.append(String.format("   Cutoff 1: %s 1–15%n", monthName));
        block.append(String.format("      Hours Worked : %s%n", firstCutoffHours));
        block.append(String.format("      Gross Pay    : %s%n", payrollValues[PAY_FIRST_CUTOFF_GROSS]));
        block.append(String.format("      Net Pay      : %s%n", payrollValues[PAY_FIRST_CUTOFF_NET]));

        block.append(String.format("   Cutoff 2: %s 16–%d%n", monthName, lastDayOfMonth));
        block.append(String.format("      Hours Worked : %s%n", secondCutoffHours));
        block.append(String.format("      Gross Pay    : %s%n", payrollValues[PAY_SECOND_CUTOFF_GROSS]));
        block.append(String.format("      Net Pay      : %s%n", payrollValues[PAY_SECOND_CUTOFF_NET]));

        block.append(PAYSLIP_SEPARATOR_MINOR).append("\n");
        block.append(String.format("   Total Gross    : %s%n", payrollValues[PAY_TOTAL_MONTHLY_GROSS]));
        block.append("   Deductions:\n");
        block.append(String.format("      SSS              : %s%n", payrollValues[PAY_SSS]));
        block.append(String.format("      PhilHealth       : %s%n", payrollValues[PAY_PHILHEALTH]));
        block.append(String.format("      Pag-IBIG         : %s%n", payrollValues[PAY_PAGIBIG]));
        block.append(String.format("      Withholding Tax  : %s%n", payrollValues[PAY_WITHHOLDING_TAX]));
        block.append(String.format("      Total Deductions : %s%n", payrollValues[PAY_TOTAL_DEDUCTIONS]));

        return block.toString();
    }

    /** Populates detailed subheaders context labels for dynamically selected targets inside dropdown */
    static void populateEmployeePanelForUser(String targetEmployeeId) {
        try {
            String[][] employeeRecords = loadEmployeesFromFile(EMPLOYEE_FILE_PATH);

            Optional<String[]> matchedRecord = Arrays.stream(employeeRecords)
                    .filter(row -> row[COL_EMP_ID].trim().equalsIgnoreCase(targetEmployeeId))
                    .findFirst();

            if (matchedRecord.isPresent()) {
                String[] employeeRow = matchedRecord.get();
                empNumberDisplayLabel.setText(safeGetArrayElement(employeeRow, COL_EMP_ID));
                empNameDisplayLabel.setText(
                        safeGetArrayElement(employeeRow, COL_LAST_NAME) + ", "
                                + safeGetArrayElement(employeeRow, COL_FIRST_NAME)
                );
                empBirthdayDisplayLabel.setText(safeGetArrayElement(employeeRow, COL_BIRTHDAY));
            } else {
                empNumberDisplayLabel.setText(targetEmployeeId);
                empNameDisplayLabel.setText("—");
                empBirthdayDisplayLabel.setText("—");
            }

        } catch (Exception fileException) {
            empNumberDisplayLabel.setText(targetEmployeeId);
            empNameDisplayLabel.setText("(File not accessible)");
            empBirthdayDisplayLabel.setText("—");
        }
    }

    static void populateRosterTable(String[][] employeeRecords) {
        staffRosterModel.setRowCount(0);

        Arrays.stream(employeeRecords)
                .filter(row -> row.length >= COL_MINIMUM_REQUIRED_COLUMNS)
                .forEach(row -> {
                    Object[] tableRow = Arrays.stream(TABLE_COLUMN_CSV_INDICES)
                            .mapToObj(colIndex -> (colIndex < row.length) ? row[colIndex] : "")
                            .toArray();
                    staffRosterModel.addRow(tableRow);
                });
    }

    static void updateStaffStatusLabel(String statusMessage) {
        staffStatusLabel.setText(statusMessage);
    }

    static void clearEmployeePanelState() {
        empIdSelectorDropdown.removeAllItems();
        empNumberDisplayLabel.setText("—");
        empNameDisplayLabel.setText("—");
        empBirthdayDisplayLabel.setText("—");
        empPayCoverageField.setText("");
        empPayrollResultsArea.setText("Your payslip will appear here after you click 'View My Payslip'.");
    }

    static void displayLoginError(String errorMessage) {
        loginErrorLabel.setText(errorMessage);
        loginErrorLabel.setVisible(true);
    }

    static JPanel createStyledPanel(LayoutManager layout, Color backgroundColor) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(backgroundColor);
        return panel;
    }

    static JLabel createLabel(String text, String fontName, int fontStyle, int fontSize, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(fontName, fontStyle, fontSize));
        label.setForeground(textColor);
        return label;
    }

    static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    static void styleTextField(JTextComponent field) {
        field.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        field.setBackground(COLOR_INPUT_BACKGROUND);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_TEXT_PRIMARY);
        field.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    static JTextArea createStyledTextArea(String placeholderText) {
        JTextArea area = new JTextArea(placeholderText);
        area.setEditable(false);
        area.setFont(new Font(FONT_MONO, Font.PLAIN, 12));
        area.setBackground(COLOR_CARD);
        area.setForeground(COLOR_TEXT_PRIMARY);
        area.setCaretColor(COLOR_TEXT_PRIMARY);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
        area.setLineWrap(false);
        return area;
    }

    static JScrollPane createStyledScrollPane(Component contentComponent) {
        JScrollPane scrollPane = new JScrollPane(contentComponent);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        scrollPane.getViewport().setBackground(COLOR_CARD);
        scrollPane.setBackground(COLOR_BACKGROUND);
        return scrollPane;
    }

    static JButton createStyledButton(String buttonLabel, Color baseColor) {
        JButton button = new JButton(buttonLabel);
        button.setFont(new Font(FONT_UI, Font.BOLD, 12));
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        final Color originalColor = baseColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) { button.setBackground(originalColor.brighter()); }
            @Override
            public void mouseExited(MouseEvent event)  { button.setBackground(originalColor); }
        });

        return button;
    }

    static JPanel buildApplicationHeader(String screenTitle, String screenSubtitle) {
        JPanel headerPanel = createStyledPanel(new BorderLayout(), COLOR_SURFACE);
        headerPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, COLOR_ACCENT_PRIMARY),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel titleStack = createStyledPanel(new GridLayout(2, 1, 0, 2), COLOR_SURFACE);
        titleStack.add(createLabel("MotorPH Payroll System", FONT_UI, Font.BOLD, 20, COLOR_TEXT_PRIMARY));
        titleStack.add(createLabel(screenSubtitle, FONT_UI, Font.PLAIN, 12, COLOR_TEXT_SECONDARY));

        JLabel roleLabel = createLabel(screenTitle, FONT_UI, Font.BOLD, 13, COLOR_ACCENT_PRIMARY);

        headerPanel.add(titleStack, BorderLayout.WEST);
        headerPanel.add(roleLabel, BorderLayout.EAST);
        return headerPanel;
    }

    static JSeparator createSeparatorLine() {
        JSeparator separator = new JSeparator();
        separator.setForeground(COLOR_BORDER);
        separator.setBackground(COLOR_BORDER);
        return separator;
    }

    static void applyTableStyling(JTable targetTable) {
        targetTable.setBackground(COLOR_TABLE_ROW_ODD);
        targetTable.setForeground(COLOR_TEXT_PRIMARY);
        targetTable.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        targetTable.setRowHeight(28);
        targetTable.setShowGrid(false);
        targetTable.setIntercellSpacing(new Dimension(0, 0));
        targetTable.setSelectionBackground(COLOR_TABLE_ROW_SEL);
        targetTable.setSelectionForeground(COLOR_TEXT_PRIMARY);
        targetTable.setFillsViewportHeight(true);

        JTableHeader tableHeader = targetTable.getTableHeader();
        tableHeader.setBackground(COLOR_SURFACE);
        tableHeader.setForeground(COLOR_TEXT_SECONDARY);
        tableHeader.setFont(new Font(FONT_UI, Font.BOLD, 12));
        tableHeader.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_BORDER));
        tableHeader.setReorderingAllowed(false);

        targetTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int rowIndex, int columnIndex) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, columnIndex);
                setBackground(isSelected
                        ? COLOR_TABLE_ROW_SEL
                        : (rowIndex % 2 == 0 ? COLOR_TABLE_ROW_EVEN : COLOR_TABLE_ROW_ODD));
                setForeground(COLOR_TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    static void showWarningDialog(String warningMessage) {
        JOptionPane.showMessageDialog(
                applicationWindow, warningMessage, "Input Validation", JOptionPane.WARNING_MESSAGE);
    }

    static void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(
                applicationWindow, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
}