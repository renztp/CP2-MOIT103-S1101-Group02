package core.services;

import core.models.Employee;

import java.util.List;

// Handles login credential validation for both staff and employee roles.
// Employees log in using their employee number instead of a shared username.
public class Authenticator {

    public static final String ROLE_PAYROLL_STAFF = "payroll_staff";
    public static final String VALID_PASSWORD = "12345";

    // Checks if the given username/password matches the payroll staff account.
    public boolean validateStaffCredentials(String username, String password) {
        return username.equalsIgnoreCase(ROLE_PAYROLL_STAFF) && password.equals(VALID_PASSWORD);
    }

    // Checks if the given employee number and password match any employee record.
    // Returns the matched Employee, or null if credentials are invalid.
    public Employee validateEmployeeCredentials(String employeeNumber, String password, List<Employee> employees) {
        if (!password.equals(VALID_PASSWORD)) {
            return null;
        }
        return employees.stream()
                .filter(emp -> emp.getId().equals(employeeNumber.trim()))
                .findFirst()
                .orElse(null);
    }

    // Returns true if the username corresponds to the payroll staff role.
    public boolean isStaffUsername(String username) {
        return username.equalsIgnoreCase(ROLE_PAYROLL_STAFF);
    }
}
