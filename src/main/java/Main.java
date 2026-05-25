import core.models.Attendance;
import core.models.Employee;
import core.services.Authenticator;
import core.services.FileHandler;
import core.services.PayrollProcessor;
import ui.EmployeePortalPanel;
import ui.LoginPanel;
import ui.MainFrame;
import ui.StaffPortalPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static final String SCREEN_LOGIN = "LOGIN";
    private static final String SCREEN_EMPLOYEE = "EMPLOYEE";
    private static final String SCREEN_STAFF = "STAFF";
    private static final String EMPTY_DISPLAY_VALUE = "-";

    private static MainFrame applicationWindow;
    private static LoginPanel loginPanel;
    private static EmployeePortalPanel employeePanel;
    private static StaffPortalPanel staffPanel;

    private static Authenticator authenticator;
    private static FileHandler fileHandler;
    private static PayrollProcessor payrollProcessor;

    public void main(String[] args) {
        SwingUtilities.invokeLater(Main::buildAndLaunchApplication);
    }

    private static void buildAndLaunchApplication() {
        authenticator = new Authenticator();
        fileHandler = new FileHandler();
        payrollProcessor = new PayrollProcessor();

        applicationWindow = new MainFrame();
        loginPanel = new LoginPanel();
        employeePanel = new EmployeePortalPanel();
        staffPanel = new StaffPortalPanel();

        applicationWindow.registerScreen(loginPanel, SCREEN_LOGIN);
        applicationWindow.registerScreen(employeePanel, SCREEN_EMPLOYEE);
        applicationWindow.registerScreen(staffPanel, SCREEN_STAFF);

        loginPanel.getLoginButton().addActionListener(e -> handleLoginAttempt());
        loginPanel.getLoginPasswordField().addActionListener(e -> handleLoginAttempt());

        employeePanel.getEmpIdSelectorDropdown().addActionListener(e -> handleEmployeeSelectionChange());
        employeePanel.getComputePayslipButton().addActionListener(e -> handleEmployeePayslipRequest());
        employeePanel.getLogoutButton().addActionListener(e -> handleLogout());

        staffPanel.getLoadRosterButton().addActionListener(e -> handleLoadEmployeeRoster());
        staffPanel.getSinglePayslipButton().addActionListener(e -> handleSingleEmployeePayrollRequest());
        staffPanel.getAllEmployeesPayrollButton().addActionListener(e -> handleAllEmployeesPayrollRequest());
        staffPanel.getLogoutButton().addActionListener(e -> handleLogout());
        staffPanel.getStaffRosterTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    handleViewEmployeeRecord();
                }
            }
        });

        applicationWindow.setVisible(true);
        applicationWindow.navigateTo(SCREEN_LOGIN);
    }

    private static void handleLoginAttempt() {
        String enteredUsername = loginPanel.getLoginUsernameField().getText().trim();
        String enteredPassword = new String(loginPanel.getLoginPasswordField().getPassword()).trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            displayLoginError("Username and password are required.");
            return;
        }

        if (!authenticator.validateUserCredentials(enteredUsername, enteredPassword)) {
            displayLoginError("Incorrect username or password.");
            loginPanel.getLoginPasswordField().setText("");
            return;
        }

        loginPanel.getLoginErrorLabel().setVisible(false);
        loginPanel.getLoginUsernameField().setText("");
        loginPanel.getLoginPasswordField().setText("");

        if (authenticator.isEmployeeRole(enteredUsername)) {
            initializeEmployeeIdSelector();
            applicationWindow.navigateTo(SCREEN_EMPLOYEE);
        } else {
            applicationWindow.navigateTo(SCREEN_STAFF);
        }
    }

    private static void handleLogout() {
        clearEmployeePanelState();
        applicationWindow.navigateTo(SCREEN_LOGIN);
    }

    private static void initializeEmployeeIdSelector() {
        employeePanel.getEmpIdSelectorDropdown().removeAllItems();

        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            for (Employee employee : employeeRecords) {
                employeePanel.getEmpIdSelectorDropdown().addItem(employee.getId());
            }

            if (employeePanel.getEmpIdSelectorDropdown().getItemCount() > 0) {
                employeePanel.getEmpIdSelectorDropdown().setSelectedIndex(0);
                handleEmployeeSelectionChange();
            }
        } catch (Exception ex) {
            showErrorDialog("Could not index employee profile lists: " + ex.getMessage());
        }
    }

    private static void handleEmployeeSelectionChange() {
        Object selected = employeePanel.getEmpIdSelectorDropdown().getSelectedItem();
        if (selected == null) {
            return;
        }

        String targetId = selected.toString();
        populateEmployeePanelForUser(targetId);
    }

    private static void handleEmployeePayslipRequest() {
        String coverageInput = employeePanel.getEmpPayCoverageField().getText().trim();

        if (!payrollProcessor.validateMonthInput(coverageInput)) {
            showWarningDialog("Pay Coverage must be a month number between 6 (June) and 12 (December).");
            return;
        }

        int selectedMonth = Integer.parseInt(coverageInput);
        String employeeId = employeePanel.getEmpNumberDisplayLabel().getText();

        if (employeeId.equals(EMPTY_DISPLAY_VALUE)) {
            showWarningDialog("Please select a valid Employee ID from the dropdown.");
            return;
        }

        try {
            List<Attendance> attendanceRecords = fileHandler.loadAttendance();
            List<Employee> employeeRecords = fileHandler.loadEmployees();

            Employee matchedEmployee = fileHandler.findEmployeeById(employeeRecords, employeeId);
            if (matchedEmployee == null) {
                showErrorDialog("Employee record not found in the file.");
                return;
            }

            int payrollYear = payrollProcessor.resolveYearFromAttendance(attendanceRecords, employeeId);
            String payslipText = payrollProcessor.buildPayslipForEmployee(
                    matchedEmployee, attendanceRecords, selectedMonth, payrollYear);

            employeePanel.getEmpPayrollResultsArea().setText(payslipText);
            employeePanel.getEmpPayrollResultsArea().setCaretPosition(0);
        } catch (Exception loadException) {
            showErrorDialog("Failed to load data: " + loadException.getMessage());
        }
    }

    private static void handleLoadEmployeeRoster() {
        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            populateRosterTable(employeeRecords);
            updateStaffStatusLabel(employeeRecords.size() + " employees loaded. Double click rows to view profiles.");
        } catch (Exception loadException) {
            showErrorDialog("Failed to load employee file: " + loadException.getMessage());
        }
    }

    private static void handleSingleEmployeePayrollRequest() {
        int selectedTableRow = staffPanel.getStaffRosterTable().getSelectedRow();

        if (selectedTableRow == -1) {
            showWarningDialog("Please select an employee from the roster table first.");
            return;
        }

        String selectedEmployeeId = staffPanel.getStaffRosterModel().getValueAt(selectedTableRow, 0).toString();

        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            List<Attendance> attendanceRecords = fileHandler.loadAttendance();

            Employee matchedEmployee = fileHandler.findEmployeeById(employeeRecords, selectedEmployeeId);
            if (matchedEmployee == null) {
                showErrorDialog("Employee record not found.");
                return;
            }

            String payrollReport = payrollProcessor.buildFullPayrollReportForEmployee(
                    matchedEmployee, attendanceRecords);
            staffPanel.getStaffPayrollResultsArea().setText(payrollReport);
            staffPanel.getStaffPayrollResultsArea().setCaretPosition(0);
            updateStaffStatusLabel("Payslip generated for employee " + selectedEmployeeId + ".");
        } catch (Exception loadException) {
            showErrorDialog("Failed to generate payslip: " + loadException.getMessage());
        }
    }

    private static void handleAllEmployeesPayrollRequest() {
        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            List<Attendance> attendanceRecords = fileHandler.loadAttendance();

            if (employeeRecords.isEmpty()) {
                showWarningDialog("No employee records found. Please load the roster first.");
                return;
            }

            String combinedReport = employeeRecords.stream()
                    .map(employee -> payrollProcessor.buildFullPayrollReportForEmployee(employee, attendanceRecords))
                    .collect(Collectors.joining("\n\n" + "=".repeat(55) + "\n\n"));

            staffPanel.getStaffPayrollResultsArea().setText(combinedReport);
            staffPanel.getStaffPayrollResultsArea().setCaretPosition(0);
            updateStaffStatusLabel("All-employee payroll generated (" + employeeRecords.size() + " employees).");
        } catch (Exception loadException) {
            showErrorDialog("Failed to generate payroll: " + loadException.getMessage());
        }
    }

    private static void handleViewEmployeeRecord() {
        int selectedTableRow = staffPanel.getStaffRosterTable().getSelectedRow();
        if (selectedTableRow == -1) {
            return;
        }

        String selectedEmployeeId = staffPanel.getStaffRosterModel().getValueAt(selectedTableRow, 0).toString();

        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            Employee employee = fileHandler.findEmployeeById(employeeRecords, selectedEmployeeId);

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
                String value = employee.getValueAt(i);
                if (!value.isEmpty()) {
                    profileBuilder.append(String.format("%-25s: %s%n", labels[i], value));
                }
            }

            JTextArea recordTextArea = MainFrame.createStyledTextArea(profileBuilder.toString());
            recordTextArea.setFont(new Font(MainFrame.FONT_MONO, Font.PLAIN, 13));
            JScrollPane scrollPane = MainFrame.createStyledScrollPane(recordTextArea);
            scrollPane.setPreferredSize(new Dimension(500, 450));

            JOptionPane.showMessageDialog(
                    applicationWindow,
                    scrollPane,
                    "Employee Record - ID " + selectedEmployeeId,
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            showErrorDialog("Could not query record: " + ex.getMessage());
        }
    }

    private static void populateEmployeePanelForUser(String targetEmployeeId) {
        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            Employee matchedEmployee = fileHandler.findEmployeeById(employeeRecords, targetEmployeeId);

            if (matchedEmployee != null) {
                employeePanel.getEmpNumberDisplayLabel().setText(matchedEmployee.getId());
                employeePanel.getEmpNameDisplayLabel().setText(
                        matchedEmployee.getLastName() + ", " + matchedEmployee.getFirstName());
                employeePanel.getEmpBirthdayDisplayLabel().setText(matchedEmployee.getBirthday());
            } else {
                employeePanel.getEmpNumberDisplayLabel().setText(targetEmployeeId);
                employeePanel.getEmpNameDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
                employeePanel.getEmpBirthdayDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
            }
        } catch (Exception fileException) {
            employeePanel.getEmpNumberDisplayLabel().setText(targetEmployeeId);
            employeePanel.getEmpNameDisplayLabel().setText("(File not accessible)");
            employeePanel.getEmpBirthdayDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        }
    }

    private static void populateRosterTable(List<Employee> employeeRecords) {
        staffPanel.getStaffRosterModel().setRowCount(0);

        for (Employee employee : employeeRecords) {
            Object[] tableRow = {
                    employee.getId(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getBirthday()
            };
            staffPanel.getStaffRosterModel().addRow(tableRow);
        }
    }

    private static void updateStaffStatusLabel(String statusMessage) {
        staffPanel.getStaffStatusLabel().setText(statusMessage);
    }

    private static void clearEmployeePanelState() {
        employeePanel.getEmpIdSelectorDropdown().removeAllItems();
        employeePanel.getEmpNumberDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        employeePanel.getEmpNameDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        employeePanel.getEmpBirthdayDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        employeePanel.getEmpPayCoverageField().setText("");
        employeePanel.getEmpPayrollResultsArea().setText(
                "Your payslip will appear here after you click 'View My Payslip'.");
    }

    private static void displayLoginError(String errorMessage) {
        loginPanel.getLoginErrorLabel().setText(errorMessage);
        loginPanel.getLoginErrorLabel().setVisible(true);
    }

    private static void showWarningDialog(String warningMessage) {
        MainFrame.showWarningDialog(applicationWindow, warningMessage);
    }

    private static void showErrorDialog(String errorMessage) {
        MainFrame.showErrorDialog(applicationWindow, errorMessage);
    }
}
