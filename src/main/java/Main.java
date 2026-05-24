import core.FileHandler;
import models.Employee;

import java.util.LinkedHashMap;

public class Main {
    public void main() throws Exception {
        System.out.println("Hello World!");
        FileHandler fileHandler = new FileHandler();

        LinkedHashMap<Integer, Employee> employees = fileHandler.getEmployeesRecords();
    }
}
