package core;

import models.Employee;

import java.util.HashMap;

public class DatabaseService {
    private FileHandler fileHandler;
    private HashMap<Integer, Employee> cachedEmployees;

    public DatabaseService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.cachedEmployees = this.fileHandler.getEmployeesRecords();
    }

    public HashMap<Integer, Employee> getCachedEmployees() {
        return cachedEmployees;
    }
}
