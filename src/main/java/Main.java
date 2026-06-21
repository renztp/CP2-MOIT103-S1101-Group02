import core.models.Attendance;
import core.models.Employee;
import core.services.Authenticator;
import core.services.FileHandler;
import core.services.PayrollProcessor;
import core.services.SalaryComputationModule;
import ui.EmployeeFormDialog;
import ui.EmployeePortalPanel;
import ui.LoginPanel;
import ui.MainFrame;
import ui.StaffPortalPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    // Holds the currently logged-in employee so the portal only shows their own data.
    private static Employee loggedInEmployee;

    public static void main(String[] args) {
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

        // Login actions
        loginPanel.getLoginButton().addActionListener(e -> handleLoginAttempt());
        loginPanel.getLoginPasswordField().addActionListener(e -> handleLoginAttempt());

        // Employee portal actions
        employeePanel.getComputePayslipButton().addActionListener(e -> handleEmployeePayslipRequest());
        employeePanel.getLogoutButton().addActionListener(e -> handleLogout());

        // Staff portal - roster and payroll actions
        staffPanel.getLoadRosterButton().addActionListener(e -> handleLoadEmployeeRoster());
        staffPanel.getSinglePayslipButton().addActionListener(e -> handleSingleEmployeePayrollRequest());
        staffPanel.getAllEmployeesPayrollButton().addActionListener(e -> handleAllEmployeesPayrollRequest());
        staffPanel.getComputeSalariesButton().addActionListener(e -> handleComputeSalaries());
        staffPanel.getLogoutButton().addActionListener(e -> handleLogout());

        // Staff portal - CRUD actions
        staffPanel.getAddEmployeeButton().addActionListener(e -> handleAddEmployee());
        staffPanel.getEditEmployeeButton().addActionListener(e -> handleEditEmployee());
        staffPanel.getDeleteEmployeeButton().addActionListener(e -> handleDeleteEmployee());

        // Staff portal - search actions
        staffPanel.getSearchButton().addActionListener(e -> handleSearch());
        staffPanel.getClearSearchButton().addActionListener(e -> handleClearSearch());
        staffPanel.getSearchField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSearch();
                }
            }
        });

        // Double-click row to view full profile
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

    // Handles login for both staff (username) and employees (employee number).
    private static void handleLoginAttempt() {
        String enteredUsername = loginPanel.getLoginUsernameField().getText().trim();
        String enteredPassword = new String(loginPanel.getLoginPasswordField().getPassword()).trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            displayLoginError("Username/Employee Number and password are required.");
            return;
        }

        // Check staff login first
        if (authenticator.isStaffUsername(enteredUsername)) {
            if (authenticator.validateStaffCredentials(enteredUsername, enteredPassword)) {
                clearLoginFields();
                applicationWindow.navigateTo(SCREEN_STAFF);
            } else {
                displayLoginError("Incorrect username or password.");
                loginPanel.getLoginPasswordField().setText("");
            }
            return;
        }

        // Attempt employee login using employee number
        try {
            List<Employee> employees = fileHandler.loadEmployees();
            Employee matched = authenticator.validateEmployeeCredentials(enteredUsername, enteredPassword, employees);
            if (matched != null) {
                loggedInEmployee = matched;
                clearLoginFields();
                populateEmployeePanelForUser(matched);
                applicationWindow.navigateTo(SCREEN_EMPLOYEE);
            } else {
                displayLoginError("Incorrect employee number or password.");
                loginPanel.getLoginPasswordField().setText("");
            }
        } catch (Exception ex) {
            displayLoginError("Could not load employee records: " + ex.getMessage());
        }
    }

    private static void handleLogout() {
        loggedInEmployee = null;
        clearEmployeePanelState();
        applicationWindow.navigateTo(SCREEN_LOGIN);
    }

    // Fills the employee portal with the logged-in employee's profile fields.
    private static void populateEmployeePanelForUser(Employee employee) {
        employeePanel.getEmpNumberDisplayLabel().setText(employee.getId());
        employeePanel.getEmpNameDisplayLabel().setText(
                employee.getLastName() + ", " + employee.getFirstName());
        employeePanel.getEmpPositionDisplayLabel().setText(employee.getPosition());
        employeePanel.getEmpBirthdayDisplayLabel().setText(employee.getBirthday());
        employeePanel.getEmpPayCoverageField().setText("");
        employeePanel.getEmpPayrollResultsArea().setText(
                "Your payslip will appear here after you click 'View My Payslip'.");
    }

    // Generates and displays the logged-in employee's payslip for the selected month.
    private static void handleEmployeePayslipRequest() {
        if (loggedInEmployee == null) {
            showWarningDialog("No employee is currently logged in.");
            return;
        }

        String coverageInput = employeePanel.getEmpPayCoverageField().getText().trim();
        if (!payrollProcessor.validateMonthInput(coverageInput)) {
            showWarningDialog("Pay Coverage must be a month number between 6 (June) and 12 (December).");
            return;
        }

        int selectedMonth = Integer.parseInt(coverageInput);

        try {
            List<Attendance> attendanceRecords = fileHandler.loadAttendance();
            int payrollYear = payrollProcessor.resolveYearFromAttendance(attendanceRecords, loggedInEmployee.getId());
            String payslipText = payrollProcessor.buildPayslipForEmployee(
                    loggedInEmployee, attendanceRecords, selectedMonth, payrollYear);

            employeePanel.getEmpPayrollResultsArea().setText(payslipText);
            employeePanel.getEmpPayrollResultsArea().setCaretPosition(0);
        } catch (Exception ex) {
            showErrorDialog("Failed to load data: " + ex.getMessage());
        }
    }

    // Loads all employees into the roster table.
    private static void handleLoadEmployeeRoster() {
        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            populateRosterTable(employeeRecords);
            staffPanel.getStaffStatusLabel().setForeground(MainFrame.COLOR_TEXT_SECONDARY);
            updateStaffStatusLabel(employeeRecords.size() + " employees loaded. Double-click rows to view profiles.");
        } catch (Exception ex) {
            showErrorDialog("Failed to load employee file: " + ex.getMessage());
        }
    }

    // Filters the roster table to employees matching the search query.
    private static void handleSearch() {
        String query = staffPanel.getSearchField().getText().trim();
        if (query.isEmpty()) {
            handleLoadEmployeeRoster();
            return;
        }
        try {
            List<Employee> results = fileHandler.searchEmployees(query);
            populateRosterTable(results);
            if (results.isEmpty()) {
                staffPanel.getStaffStatusLabel().setForeground(MainFrame.COLOR_ACCENT_WARNING);
                updateStaffStatusLabel("No employees matched \"" + query + "\". Try a different name, number, or position.");
            } else {
                staffPanel.getStaffStatusLabel().setForeground(MainFrame.COLOR_TEXT_SECONDARY);
                updateStaffStatusLabel(results.size() + " result(s) found for \"" + query + "\".");
            }
        } catch (Exception ex) {
            showErrorDialog("Search failed: " + ex.getMessage());
        }
    }

    // Clears the search field and reloads the full roster.
    private static void handleClearSearch() {
        staffPanel.getSearchField().setText("");
        handleLoadEmployeeRoster();
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

            String payrollReport = payrollProcessor.buildFullPayrollReportForEmployee(matchedEmployee, attendanceRecords);
            staffPanel.getStaffPayrollResultsArea().setText(payrollReport);
            staffPanel.getStaffPayrollResultsArea().setCaretPosition(0);
            updateStaffStatusLabel("Payslip generated for employee " + selectedEmployeeId + ".");
        } catch (Exception ex) {
            showErrorDialog("Failed to generate payslip: " + ex.getMessage());
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
        } catch (Exception ex) {
            showErrorDialog("Failed to generate payroll: " + ex.getMessage());
        }
    }

    // Feature 3 - Computes salaries for all employees, displays results, and saves to CSV.
    private static void handleComputeSalaries() {
        try {
            List<Employee> employeeRecords = fileHandler.loadEmployees();
            List<Attendance> attendanceRecords = fileHandler.loadAttendance();

            if (employeeRecords.isEmpty()) {
                showWarningDialog("No employee records found. Please load the employee roster first.");
                return;
            }

            String numericValidationError = SalaryComputationModule.validateEmployeeNumericFields(employeeRecords);
            if (numericValidationError != null) {
                showWarningDialog(numericValidationError);
                return;
            }

            SalaryComputationModule.SalaryResult result =
                    SalaryComputationModule.computeAllSalaries(employeeRecords, attendanceRecords);

            if (!result.success) {
                showWarningDialog(result.errorMessage);
                return;
            }

            String salaryReport = SalaryComputationModule.buildSalaryReport(result, employeeRecords);
            staffPanel.getStaffPayrollResultsArea().setText(salaryReport);
            staffPanel.getStaffPayrollResultsArea().setCaretPosition(0);

            fileHandler.saveSalaryResults(result);

            updateStaffStatusLabel("Salaries computed for " + employeeRecords.size() + " employees.");

            JOptionPane.showMessageDialog(
                    applicationWindow,
                    "Salary results were generated and saved to the employee CSV file.",
                    "Computation Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            showErrorDialog("Failed to compute salaries: " + ex.getMessage());
        }
    }

    // Opens the Add Employee dialog and saves the new record to the in-memory list.
    private static void handleAddEmployee() {
        EmployeeFormDialog dialog = new EmployeeFormDialog(applicationWindow, "Add New Employee", null);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        Employee newEmployee = dialog.buildEmployee();
        try {
            if (fileHandler.employeeIdExists(newEmployee.getId())) {
                showErrorDialog("Employee ID " + newEmployee.getId() + " already exists.");
                return;
            }
            fileHandler.addEmployee(newEmployee);
            handleLoadEmployeeRoster();
            updateStaffStatusLabel("Employee " + newEmployee.getId() + " added successfully.");
        } catch (Exception ex) {
            showErrorDialog("Failed to add employee: " + ex.getMessage());
        }
    }

    // Opens the Edit Employee dialog pre-filled with the selected row's data.
    private static void handleEditEmployee() {
        int selectedRow = staffPanel.getStaffRosterTable().getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select an employee from the roster to edit.");
            return;
        }

        String selectedId = staffPanel.getStaffRosterModel().getValueAt(selectedRow, 0).toString();

        try {
            List<Employee> employees = fileHandler.loadEmployees();
            Employee existing = fileHandler.findEmployeeById(employees, selectedId);
            if (existing == null) {
                showErrorDialog("Employee record not found.");
                return;
            }

            EmployeeFormDialog dialog = new EmployeeFormDialog(applicationWindow, "Edit Employee - " + selectedId, existing);
            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                return;
            }

            fileHandler.updateEmployee(dialog.buildEmployee());
            handleLoadEmployeeRoster();
            updateStaffStatusLabel("Employee " + selectedId + " updated successfully.");
        } catch (Exception ex) {
            showErrorDialog("Failed to update employee: " + ex.getMessage());
        }
    }

    // Prompts for confirmation then removes the selected employee from the in-memory list.
    private static void handleDeleteEmployee() {
        int selectedRow = staffPanel.getStaffRosterTable().getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Please select an employee from the roster to delete.");
            return;
        }

        String selectedId = staffPanel.getStaffRosterModel().getValueAt(selectedRow, 0).toString();
        String selectedName = staffPanel.getStaffRosterModel().getValueAt(selectedRow, 2)
                + " " + staffPanel.getStaffRosterModel().getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(
                applicationWindow,
                "Delete employee " + selectedId + " (" + selectedName + ")?\nThis cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            fileHandler.deleteEmployee(selectedId);
            handleLoadEmployeeRoster();
            updateStaffStatusLabel("Employee " + selectedId + " deleted.");
        } catch (Exception ex) {
            showErrorDialog("Failed to delete employee: " + ex.getMessage());
        }
    }

    // Shows the full profile of the double-clicked employee in a dialog.
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

    // Fills the roster table with a list of employees. Includes position column.
    private static void populateRosterTable(List<Employee> employeeRecords) {
        staffPanel.getStaffRosterModel().setRowCount(0);
        for (Employee employee : employeeRecords) {
            Object[] tableRow = {
                    employee.getId(),
                    employee.getLastName(),
                    employee.getFirstName(),
                    employee.getPosition(),
                    employee.getBirthday()
            };
            staffPanel.getStaffRosterModel().addRow(tableRow);
        }
    }

    private static void updateStaffStatusLabel(String statusMessage) {
        staffPanel.getStaffStatusLabel().setText(statusMessage);
    }

    private static void clearLoginFields() {
        loginPanel.getLoginErrorLabel().setVisible(false);
        loginPanel.getLoginUsernameField().setText("");
        loginPanel.getLoginPasswordField().setText("");
    }

    private static void clearEmployeePanelState() {
        employeePanel.getEmpNumberDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        employeePanel.getEmpNameDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
        employeePanel.getEmpPositionDisplayLabel().setText(EMPTY_DISPLAY_VALUE);
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
