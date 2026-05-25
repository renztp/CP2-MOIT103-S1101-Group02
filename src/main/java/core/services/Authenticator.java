package core.services;

public class Authenticator {

    public static final String ROLE_EMPLOYEE = "employee";
    public static final String ROLE_PAYROLL_STAFF = "payroll_staff";
    public static final String VALID_PASSWORD = "12345";

    public boolean validateUserCredentials(String username, String password) {
        boolean isKnownUsername = username.equalsIgnoreCase(ROLE_EMPLOYEE)
                || username.equalsIgnoreCase(ROLE_PAYROLL_STAFF);
        boolean isCorrectPassword = password.equals(VALID_PASSWORD);
        return isKnownUsername && isCorrectPassword;
    }

    public boolean isEmployeeRole(String username) {
        return username.equalsIgnoreCase(ROLE_EMPLOYEE);
    }
}
