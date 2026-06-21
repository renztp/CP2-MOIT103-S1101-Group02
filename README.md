# MotorPH Payroll System

A desktop payroll management application for MotorPH employees and payroll staff.

## Abstract

MotorPH Payroll System is a local desktop application that helps manage employee records and compute payroll from attendance data. Payroll staff use it to maintain the employee roster, run payroll reports, and batch-compute salaries; individual employees use it to view their profile and generate a monthly payslip. The app supports searching and filtering the roster, adding, editing, and deleting employee records, and producing on-screen payroll reports for June through December. Salary computation draws on bundled CSV data that is copied to a writable folder in the user's home directory on first run. The system is built with Java 17, Java Swing, and Apache Maven.

## Key Features

- **Employee roster** — Staff loads the roster table via **Load Roster**; double-click a row to view the full employee profile.
- **Search** — Filter employees by employee number, first or last name, or position (case-insensitive).
- **Add / Edit / Delete** — Create, update, or remove employee records through a form dialog; changes persist to the employee CSV.
- **Salary & payslip computation** — Employees view a single-month payslip; staff generate per-employee or all-employee payroll reports; **Compute Salaries** runs batch computation and saves results to CSV.
- **Role-based login** — Payroll staff and employees sign in with different credentials and are routed to separate portals.
- **Payroll reports** — On-screen text reports in the staff portal for a selected employee or all employees.

## Tech Stack

| Item     | Value          |
|----------|----------------|
| Language | Java 17        |
| UI       | Java Swing     |
| Build    | Apache Maven   |
| Data     | CSV files      |

## Project Structure

```
src/main/java/
├── Main.java              # Entry point; wires UI events to services
├── core/models/           # Employee, Attendance, PayrollBreakdown
├── core/services/         # FileHandler, Authenticator, PayrollProcessor,
│                          # SalaryComputationModule, DeductionCalculator
└── ui/                    # MainFrame, LoginPanel, StaffPortalPanel,
                           # EmployeePortalPanel, EmployeeFormDialog

src/main/resources/
├── Employee Details.csv
└── Attendance Record.csv
```

- **`core/models`** — Data classes representing employees, attendance rows, and payroll breakdowns.
- **`core/services`** — Business logic for file I/O, authentication, payroll processing, salary computation, and deductions.
- **`ui`** — Swing panels and dialogs for login, staff and employee portals, and the employee form.
- **`Main.java`** — Application entry point that connects UI actions to service methods.

## How to Run

**Prerequisites:** JDK 17 or later, Maven 3.x, and a graphical display (Swing desktop app).

```bash
cd /path/to/CP2-MOIT103-S1101-Group02-MS2
mvn compile
java -cp target/classes Main
```

`Main` is in the default package, so no package prefix is needed on the command line.

## Login Credentials

| Role          | Username                                | Password |
|---------------|-----------------------------------------|----------|
| Payroll staff | `payroll_staff`                         | `12345`  |
| Employee      | Employee Number (e.g. `10001` from CSV) | `12345`  |

Employees log in using their **Employee Number** in the username field. The password is shared for all employees in this version.

## Data Files Note

Bundled CSV templates live in `src/main/resources/`:

- `Employee Details.csv`
- `Attendance Record.csv`

On first run, the application creates `~/MotorPH-Data/` in the user's home directory and copies the CSV files there if they are missing or if the employee file header is outdated. All runtime reads and writes use the copies in `~/MotorPH-Data/`.

## Limitations

- The roster is not auto-loaded on staff login — click **Load Roster** manually.
- No auto-refresh if CSV files are edited externally while the app is running; the employee list is cached in memory.
- Attendance data is read-only in the UI; deleting an employee does not remove their attendance rows.
- Payroll coverage is limited to months 6–12 (June through December).
- Payslip reports and **Compute Salaries** use different calculation models and may produce different figures.
- Payroll reports display on-screen only; only **Compute Salaries** writes salary fields back to the employee CSV.
- A shared password (`12345`) is used for staff and all employees — not suitable for production use.
- The window size is fixed; there are no automated tests in the repository.

## Team Members

- Lance Tucker — Lead Developer
- Ashley Mackenzie Ramos — Developer
- Louise Isabelle Lopez — Developer
- Ma. Irene Andrea Esquerra — Developer
- Renz Pulvira — Developer
