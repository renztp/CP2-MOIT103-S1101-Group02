import core.FileHandler;

public class Main {
    public void main() throws Exception {
        System.out.println("Hello World!");

        FileHandler fileHandler = new FileHandler();
        String[][] employeeRecords = fileHandler.getEmployeesRecords();
//        System.out.println(employeeRecords);
    }
}
