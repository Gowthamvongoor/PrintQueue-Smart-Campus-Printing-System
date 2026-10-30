package com.printqueue;

import com.printqueue.model.OrderStatus;
import com.printqueue.model.PrintOrder;
import com.printqueue.model.Printer;
import com.printqueue.model.PrinterStatus;
import com.printqueue.model.Student;
import com.printqueue.service.PrintQueueService;
import com.printqueue.util.ConsoleUI;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class providing the interactive console user interface,
 * menus, student and admin workflows, and viva-friendly logic.
 */
public class Main {
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private final PrintQueueService service;
    private final Scanner scanner;

    public Main() {
        this.service = new PrintQueueService();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        boolean running = true;
        while (running) {
            printWelcomeBanner();
            System.out.println("  1. Student Login");
            System.out.println("  2. Student Registration");
            System.out.println("  3. Administrator Login");
            System.out.println("  4. View Live Print Queue (Public)");
            System.out.println("  5. Exit System");
            System.out.print("\nEnter your choice (1-5): ");

            int choice = readIntChoice(1, 5);
            switch (choice) {
                case 1:
                    handleStudentLogin();
                    break;
                case 2:
                    handleStudentRegistration();
                    break;
                case 3:
                    handleAdminLogin();
                    break;
                case 4:
                    ConsoleUI.displayLiveQueue(service.getPrintQueue());
                    pauseForEnter();
                    break;
                case 5:
                    running = false;
                    System.out.println();
                    ConsoleUI.info("Thank you for using PrintQueue - Smart Campus Printing System. Goodbye!");
                    break;
            }
        }
    }

    private void printWelcomeBanner() {
        System.out.println();
        System.out.println(ConsoleUI.CYAN + "╔════════════════════════════════════════════════════╗" + ConsoleUI.RESET);
        System.out.println(ConsoleUI.CYAN + "║ " + ConsoleUI.BOLD + ConsoleUI.WHITE + "      SMART CAMPUS PRINTING QUEUE SYSTEM          " + ConsoleUI.RESET + ConsoleUI.CYAN + "║" + ConsoleUI.RESET);
        System.out.println(ConsoleUI.CYAN + "║ " + ConsoleUI.YELLOW + "           FIFO Queue & Multithreading            " + ConsoleUI.RESET + ConsoleUI.CYAN + "║" + ConsoleUI.RESET);
        System.out.println(ConsoleUI.CYAN + "╚════════════════════════════════════════════════════╝" + ConsoleUI.RESET);
    }

    // ==================== STUDENT WORKFLOW ====================

    private void handleStudentRegistration() {
        ConsoleUI.printHeader("STUDENT REGISTRATION");
        System.out.print("Enter Student ID (e.g. 23BCE1001): ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Choose Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Choose Password: ");
        String password = scanner.nextLine().trim();

        service.registerStudent(studentId, name, username, password);
        pauseForEnter();
    }

    private void handleStudentLogin() {
        ConsoleUI.printHeader("STUDENT LOGIN");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Student student = service.loginStudent(username, password);
        if (student == null) {
            ConsoleUI.error("Invalid username or password! Please check your credentials.");
            pauseForEnter();
            return;
        }

        ConsoleUI.success("Welcome back, " + student.getName() + " (" + student.getStudentId() + ")!");
        runStudentDashboard(student);
    }

    private void runStudentDashboard(Student student) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            ConsoleUI.printHeader("STUDENT DASHBOARD - " + student.getName().toUpperCase());
            System.out.println("  1. Submit New Print Request");
            System.out.println("  2. View My Print Orders");
            System.out.println("  3. Track Order Position & Visual Timeline");
            System.out.println("  4. Collect Printed Document");
            System.out.println("  5. Cancel Pending Order");
            System.out.println("  6. View Live Campus Queue");
            System.out.println("  7. Logout to Main Menu");
            System.out.print("\nSelect option (1-7): ");

            int choice = readIntChoice(1, 7);
            switch (choice) {
                case 1:
                    handleSubmitPrintRequest(student);
                    break;
                case 2:
                    handleViewStudentOrders(student);
                    pauseForEnter();
                    break;
                case 3:
                    handleTrackOrderTimeline(student);
                    pauseForEnter();
                    break;
                case 4:
                    handleCollectDocument(student);
                    pauseForEnter();
                    break;
                case 5:
                    handleCancelOrder(student);
                    pauseForEnter();
                    break;
                case 6:
                    ConsoleUI.displayLiveQueue(service.getPrintQueue());
                    pauseForEnter();
                    break;
                case 7:
                    loggedIn = false;
                    ConsoleUI.info("Logged out successfully.");
                    break;
            }
        }
    }

    private void handleSubmitPrintRequest(Student student) {
        ConsoleUI.printHeader("SUBMIT PRINT REQUEST");

        System.out.print("Enter Document Name (e.g., Mini_Project_Report.pdf): ");
        String docName = scanner.nextLine().trim();
        while (docName.isEmpty()) {
            ConsoleUI.warning("Document name cannot be empty. Please enter a valid name:");
            docName = scanner.nextLine().trim();
        }

        System.out.print("Enter Number of Pages: ");
        int pages = readPositiveInt();

        System.out.print("Enter Number of Copies: ");
        int copies = readPositiveInt();

        System.out.println("\nSelect Print Type:");
        System.out.println("  1. Black & White");
        System.out.println("  2. Color");
        System.out.print("Choice (1-2): ");
        int typeChoice = readIntChoice(1, 2);
        String printType = (typeChoice == 1) ? "Black & White" : "Color";

        System.out.println("\nSelect Paper Size:");
        System.out.println("  1. A4");
        System.out.println("  2. A3");
        System.out.print("Choice (1-2): ");
        int sizeChoice = readIntChoice(1, 2);
        String paperSize = (sizeChoice == 1) ? "A4" : "A3";

        System.out.println("\nSelect Print Mode:");
        System.out.println("  1. Single-sided");
        System.out.println("  2. Double-sided");
        System.out.print("Choice (1-2): ");
        int modeChoice = readIntChoice(1, 2);
        String printMode = (modeChoice == 1) ? "Single-sided" : "Double-sided";

        PrintOrder order = service.submitOrder(
                student.getStudentId(),
                docName,
                pages,
                copies,
                printType,
                paperSize,
                printMode
        );

        int pos = service.getQueuePosition(order.getOrderId());

        System.out.println();
        ConsoleUI.success("Print request submitted successfully!");
        ConsoleUI.info("Your assigned Order ID is: " + ConsoleUI.BOLD + order.getOrderId() + ConsoleUI.RESET);
        ConsoleUI.info("Your current queue position is: #" + pos);

        ConsoleUI.displayLiveQueue(service.getPrintQueue());
        pauseForEnter();
    }

    private void handleViewStudentOrders(Student student) {
        List<PrintOrder> myOrders = service.getOrdersByStudent(student.getStudentId());
        ConsoleUI.printHeader("MY PRINT ORDERS (" + student.getStudentId() + ")");

        if (myOrders.isEmpty()) {
            System.out.println(ConsoleUI.YELLOW + "  You have not placed any print orders yet." + ConsoleUI.RESET);
            return;
        }

        printOrderTable(myOrders);
    }

    private void handleTrackOrderTimeline(Student student) {
        List<PrintOrder> myOrders = service.getOrdersByStudent(student.getStudentId());
        if (myOrders.isEmpty()) {
            ConsoleUI.warning("You have no orders to track.");
            return;
        }

        System.out.println("\nYour orders:");
        for (PrintOrder o : myOrders) {
            System.out.println(" - " + o.getOrderId() + ": " + o.getDocumentName() + " [" + o.getStatus() + "]");
        }

        System.out.print("\nEnter Order ID to track (e.g. PQ1001): ");
        String orderId = scanner.nextLine().trim();

        PrintOrder order = service.findOrderById(orderId);
        if (order == null || !order.getStudentId().equalsIgnoreCase(student.getStudentId())) {
            ConsoleUI.error("Order ID not found in your orders.");
            return;
        }

        int pos = service.getQueuePosition(order.getOrderId());
        ConsoleUI.displayOrderTimeline(order, pos);
    }

    private void handleCollectDocument(Student student) {
        ConsoleUI.printHeader("COLLECT PRINTED DOCUMENT");
        List<PrintOrder> myOrders = service.getOrdersByStudent(student.getStudentId());

        boolean hasReady = false;
        for (PrintOrder o : myOrders) {
            if (o.getStatus() == OrderStatus.READY_FOR_PICKUP) {
                if (!hasReady) {
                    System.out.println("Orders ready for collection:");
                    hasReady = true;
                }
                System.out.println("  -> [" + o.getOrderId() + "] " + o.getDocumentName() + " (Printed on: " + o.getAssignedPrinter() + ")");
            }
        }

        if (!hasReady) {
            ConsoleUI.warning("None of your orders are currently READY FOR PICKUP.");
            return;
        }

        System.out.print("\nEnter Order ID to collect: ");
        String orderId = scanner.nextLine().trim();

        service.collectOrder(orderId, student.getStudentId());
    }

    private void handleCancelOrder(Student student) {
        ConsoleUI.printHeader("CANCEL PRINT ORDER");
        List<PrintOrder> myOrders = service.getOrdersByStudent(student.getStudentId());

        boolean hasCancellable = false;
        for (PrintOrder o : myOrders) {
            if (o.getStatus() == OrderStatus.SUBMITTED || o.getStatus() == OrderStatus.PENDING) {
                if (!hasCancellable) {
                    System.out.println("Orders eligible for cancellation:");
                    hasCancellable = true;
                }
                int pos = service.getQueuePosition(o.getOrderId());
                System.out.println("  -> [" + o.getOrderId() + "] " + o.getDocumentName() + " (Queue Pos: #" + pos + ")");
            }
        }

        if (!hasCancellable) {
            ConsoleUI.warning("You have no pending orders that can be cancelled.");
            return;
        }

        System.out.print("\nEnter Order ID to cancel: ");
        String orderId = scanner.nextLine().trim();

        service.cancelOrder(orderId, student.getStudentId());
    }

    // ==================== ADMINISTRATOR WORKFLOW ====================

    private void handleAdminLogin() {
        ConsoleUI.printHeader("ADMINISTRATOR LOGIN");
        System.out.print("Admin Username (default: admin): ");
        String user = scanner.nextLine().trim();

        System.out.print("Admin Password (default: admin123): ");
        String pass = scanner.nextLine().trim();

        if (ADMIN_USER.equals(user) && ADMIN_PASS.equals(pass)) {
            ConsoleUI.success("Admin authenticated successfully!");
            runAdminDashboard();
        } else {
            ConsoleUI.error("Invalid administrator credentials!");
            pauseForEnter();
        }
    }

    private void runAdminDashboard() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println();
            ConsoleUI.printHeader("ADMINISTRATOR CONTROL PANEL");
            System.out.println("  1. View All Campus Print Orders");
            System.out.println("  2. View Live Print Queue (ASCII Diagram)");
            System.out.println("  3. Process Next Order in Queue (Simulate Printing)");
            System.out.println("  4. Monitor & Manage Campus Printers");
            System.out.println("  5. View System Statistics");
            System.out.println("  6. Logout to Main Menu");
            System.out.print("\nSelect option (1-6): ");

            int choice = readIntChoice(1, 6);
            switch (choice) {
                case 1:
                    printOrderTable(service.getAllOrders());
                    pauseForEnter();
                    break;
                case 2:
                    ConsoleUI.displayLiveQueue(service.getPrintQueue());
                    pauseForEnter();
                    break;
                case 3:
                    handleProcessNextOrder();
                    pauseForEnter();
                    break;
                case 4:
                    handleManagePrinters();
                    break;
                case 5:
                    service.displayStatistics();
                    pauseForEnter();
                    break;
                case 6:
                    loggedIn = false;
                    ConsoleUI.info("Administrator logged out.");
                    break;
            }
        }
    }

    private void handleProcessNextOrder() {
        ConsoleUI.printHeader("PROCESS NEXT QUEUE JOB");
        if (service.getPrintQueue().isEmpty()) {
            ConsoleUI.warning("Queue is empty. No pending print orders to process.");
            return;
        }

        PrintOrder next = service.getPrintQueue().peek();
        System.out.println("Next order in FIFO queue:");
        System.out.println("  Order ID : " + next.getOrderId());
        System.out.println("  Document : " + next.getDocumentName());
        System.out.println("  Student  : " + next.getStudentId());
        System.out.println("  Sheets   : " + next.getPages() + " pages x " + next.getCopies() + " copies (" + next.getPrintMode() + ")");

        System.out.print("\nProceed to process this job? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y") || confirm.equalsIgnoreCase("YES")) {
            service.processNextOrder();
        } else {
            ConsoleUI.info("Processing cancelled by administrator.");
        }
    }

    private void handleManagePrinters() {
        boolean managing = true;
        while (managing) {
            ConsoleUI.displayPrinterMonitor(service.getPrinters());
            System.out.println("Printer Actions:");
            System.out.println("  1. Toggle Printer Status (Available / Offline)");
            System.out.println("  2. Return to Admin Panel");
            System.out.print("Select action (1-2): ");

            int action = readIntChoice(1, 2);
            if (action == 1) {
                System.out.print("Enter Printer ID (e.g. PRINTER01, PRINTER02): ");
                String pid = scanner.nextLine().trim().toUpperCase();

                Printer found = null;
                for (Printer p : service.getPrinters()) {
                    if (p.getPrinterId().equalsIgnoreCase(pid)) {
                        found = p;
                        break;
                    }
                }

                if (found == null) {
                    ConsoleUI.error("Invalid Printer ID.");
                } else {
                    System.out.println("Current status of " + pid + ": " + found.getStatus());
                    System.out.println("Select new status:");
                    System.out.println("  1. AVAILABLE");
                    System.out.println("  2. OFFLINE (Maintenance / Out of Paper)");
                    System.out.print("Choice (1-2): ");
                    int statusChoice = readIntChoice(1, 2);
                    PrinterStatus newStatus = (statusChoice == 1) ? PrinterStatus.AVAILABLE : PrinterStatus.OFFLINE;
                    service.updatePrinterStatus(pid, newStatus);
                }
                pauseForEnter();
            } else {
                managing = false;
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private void printOrderTable(List<PrintOrder> orders) {
        if (orders.isEmpty()) {
            ConsoleUI.info("No orders found.");
            return;
        }

        System.out.println();
        String format = "%-8s | %-10s | %-20s | %-5s | %-6s | %-13s | %-16s | %-10s%n";
        System.out.printf(format, "Order ID", "Student", "Document Name", "Pages", "Copies", "Print Type", "Status", "Printer");
        System.out.println("-".repeat(98));

        for (PrintOrder o : orders) {
            String doc = o.getDocumentName();
            if (doc.length() > 19) {
                doc = doc.substring(0, 16) + "...";
            }
            System.out.printf(format,
                    o.getOrderId(),
                    o.getStudentId(),
                    doc,
                    o.getPages(),
                    o.getCopies(),
                    o.getPrintType(),
                    o.getStatus(),
                    o.getAssignedPrinter()
            );
        }
    }

    private int readIntChoice(int min, int max) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("Please enter a valid number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input. Please enter a number (%d-%d): ", min, max);
            }
        }
    }

    private int readPositiveInt() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val > 0) {
                    return val;
                }
                System.out.print("Value must be greater than zero. Try again: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid integer. Please enter a valid number: ");
            }
        }
    }

    private void pauseForEnter() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
