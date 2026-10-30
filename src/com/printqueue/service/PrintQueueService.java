package com.printqueue.service;

import com.printqueue.model.OrderStatus;
import com.printqueue.model.PrintOrder;
import com.printqueue.model.Printer;
import com.printqueue.model.PrinterStatus;
import com.printqueue.model.Student;
import com.printqueue.util.ConsoleUI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Core business service managing students, print queue operations,
 * printer allocations, and multithreaded print simulations.
 */
public class PrintQueueService {
    private final FileStorageService storageService;
    private final List<Student> students;
    private final List<PrintOrder> allOrders;
    private final Queue<PrintOrder> printQueue;
    private final List<Printer> printers;
    private int nextOrderIdNumber = 1001;

    public PrintQueueService() {
        this.storageService = new FileStorageService();
        this.students = new ArrayList<>(storageService.loadStudents());
        this.allOrders = new ArrayList<>(storageService.loadOrders());
        this.printers = new ArrayList<>(storageService.loadPrinters());
        this.printQueue = new LinkedList<>();

        // Reconstruct FIFO queue from pending/submitted orders in file
        initializeQueue();
        initializeOrderIdCounter();
    }

    /**
     * Initializes the FIFO queue with pending orders from persistent storage.
     */
    private void initializeQueue() {
        for (PrintOrder order : allOrders) {
            if (order.getStatus() == OrderStatus.SUBMITTED || order.getStatus() == OrderStatus.PENDING) {
                // Ensure status is marked as PENDING in queue
                order.setStatus(OrderStatus.PENDING);
                printQueue.offer(order);
            }
        }
    }

    /**
     * Finds the maximum existing order ID number to prevent duplicates.
     */
    private void initializeOrderIdCounter() {
        int max = 1000;
        for (PrintOrder o : allOrders) {
            String id = o.getOrderId();
            if (id != null && id.startsWith("PQ")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        this.nextOrderIdNumber = max + 1;
    }

    // ==================== STUDENT AUTHENTICATION ====================

    public synchronized boolean registerStudent(String studentId, String name, String username, String password) {
        if (studentId == null || studentId.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            ConsoleUI.error("All registration fields are required!");
            return false;
        }

        // Check for duplicate Student ID
        for (Student s : students) {
            if (s.getStudentId().equalsIgnoreCase(studentId.trim())) {
                ConsoleUI.error("A student with ID '" + studentId + "' already exists!");
                return false;
            }
        }

        // Check for duplicate Username
        for (Student s : students) {
            if (s.getUsername().equalsIgnoreCase(username.trim())) {
                ConsoleUI.error("Username '" + username + "' is already taken! Please choose another.");
                return false;
            }
        }

        Student newStudent = new Student(studentId.trim(), name.trim(), username.trim(), password.trim());
        students.add(newStudent);
        storageService.saveStudents(students);
        ConsoleUI.success("Student registered successfully! Please log in.");
        return true;
    }

    public Student loginStudent(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        for (Student s : students) {
            if (s.getUsername().equalsIgnoreCase(username.trim()) && s.getPassword().equals(password)) {
                return s;
            }
        }
        return null;
    }

    // ==================== ORDER SUBMISSION & QUEUE ====================

    /**
     * Submits a new print order and enqueues it into the FIFO queue.
     */
    public synchronized PrintOrder submitOrder(String studentId, String documentName, int pages, int copies,
                                               String printType, String paperSize, String printMode) {
        String orderId = "PQ" + (nextOrderIdNumber++);
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        PrintOrder order = new PrintOrder(
                orderId,
                studentId,
                documentName.trim(),
                pages,
                copies,
                printType,
                paperSize,
                printMode,
                currentTime,
                OrderStatus.PENDING,
                "N/A"
        );

        allOrders.add(order);
        printQueue.offer(order);
        storageService.saveOrders(allOrders);

        return order;
    }

    /**
     * Returns the 1-based queue position of an order, or -1 if not currently in queue.
     */
    public synchronized int getQueuePosition(String orderId) {
        int pos = 1;
        for (PrintOrder o : printQueue) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    /**
     * Retrieves all orders for a specific student.
     */
    public synchronized List<PrintOrder> getOrdersByStudent(String studentId) {
        List<PrintOrder> list = new ArrayList<>();
        for (PrintOrder o : allOrders) {
            if (o.getStudentId().equalsIgnoreCase(studentId)) {
                list.add(o);
            }
        }
        return list;
    }

    /**
     * Cancels an order if it has not yet started printing.
     */
    public synchronized boolean cancelOrder(String orderId, String studentId) {
        PrintOrder target = null;
        for (PrintOrder o : allOrders) {
            if (o.getOrderId().equalsIgnoreCase(orderId) && o.getStudentId().equalsIgnoreCase(studentId)) {
                target = o;
                break;
            }
        }

        if (target == null) {
            ConsoleUI.error("Order ID " + orderId + " not found for your account.");
            return false;
        }

        if (target.getStatus() != OrderStatus.SUBMITTED && target.getStatus() != OrderStatus.PENDING) {
            ConsoleUI.error("Cannot cancel order " + orderId + ". Only PENDING orders can be cancelled (Current: " + target.getStatus() + ").");
            return false;
        }

        target.setStatus(OrderStatus.CANCELLED);
        printQueue.remove(target);
        storageService.saveOrders(allOrders);
        ConsoleUI.success("Order " + orderId + " has been cancelled and removed from the queue.");
        return true;
    }

    /**
     * Collects a printed document if it is ready for pickup.
     */
    public synchronized boolean collectOrder(String orderId, String studentId) {
        PrintOrder target = null;
        for (PrintOrder o : allOrders) {
            if (o.getOrderId().equalsIgnoreCase(orderId) && o.getStudentId().equalsIgnoreCase(studentId)) {
                target = o;
                break;
            }
        }

        if (target == null) {
            ConsoleUI.error("Order ID " + orderId + " not found for your account.");
            return false;
        }

        if (target.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            ConsoleUI.error("Order " + orderId + " cannot be collected yet! Status: " + target.getStatus());
            return false;
        }

        target.setStatus(OrderStatus.COLLECTED);
        storageService.saveOrders(allOrders);
        ConsoleUI.success("Document collected successfully! Thank you for using PrintQueue.");
        return true;
    }

    // ==================== PRINTER MANAGEMENT ====================

    public synchronized List<Printer> getPrinters() {
        return printers;
    }

    public synchronized Printer findAvailablePrinter() {
        for (Printer p : printers) {
            if (p.isAvailable()) {
                return p;
            }
        }
        return null;
    }

    public synchronized boolean updatePrinterStatus(String printerId, PrinterStatus newStatus) {
        for (Printer p : printers) {
            if (p.getPrinterId().equalsIgnoreCase(printerId)) {
                p.setStatus(newStatus);
                storageService.savePrinters(printers);
                ConsoleUI.success("Printer " + printerId + " status updated to " + newStatus);
                return true;
            }
        }
        ConsoleUI.error("Printer " + printerId + " not found.");
        return false;
    }

    // ==================== ADMIN: PROCESS NEXT ORDER WITH THREADING ====================

    /**
     * Processes the oldest eligible order in the FIFO queue using a dedicated background Thread.
     */
    public synchronized void processNextOrder() {
        if (printQueue.isEmpty()) {
            ConsoleUI.warning("The print queue is empty! No pending orders to process.");
            return;
        }

        Printer printer = findAvailablePrinter();
        if (printer == null) {
            ConsoleUI.warning("No printer is currently available. All printers are either BUSY or OFFLINE.");
            return;
        }

        // FIFO: Dequeue the first order
        PrintOrder order = printQueue.poll();
        if (order == null) {
            return;
        }

        // Update states
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setAssignedPrinter(printer.getPrinterId());
        printer.setStatus(PrinterStatus.BUSY);
        storageService.saveOrders(allOrders);
        storageService.savePrinters(printers);

        ConsoleUI.processing("Allocated Order " + order.getOrderId() + " to " + printer.getPrinterId() + ". Starting print job...");

        // Run the print simulation in a Java Thread
        Thread printThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Render live animation screen
                ConsoleUI.displayPrintingAnimation(order, printer);

                // On completion: update statuses
                synchronized (PrintQueueService.this) {
                    order.setStatus(OrderStatus.READY_FOR_PICKUP);
                    printer.setStatus(PrinterStatus.AVAILABLE);
                    storageService.saveOrders(allOrders);
                    storageService.savePrinters(printers);
                }

                ConsoleUI.success("Order " + order.getOrderId() + " is now READY FOR PICKUP at " + printer.getPrinterName() + "!");
            }
        }, "PrintWorker-" + order.getOrderId());

        printThread.start();

        try {
            // Wait for print simulation to complete before returning control to menu
            printThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ConsoleUI.error("Print execution was interrupted.");
        }
    }

    // ==================== GETTERS & STATS ====================

    public synchronized Queue<PrintOrder> getPrintQueue() {
        return printQueue;
    }

    public synchronized List<PrintOrder> getAllOrders() {
        return allOrders;
    }

    public synchronized PrintOrder findOrderById(String orderId) {
        for (PrintOrder o : allOrders) {
            if (o.getOrderId().equalsIgnoreCase(orderId)) {
                return o;
            }
        }
        return null;
    }

    public synchronized void displayStatistics() {
        int total = allOrders.size();
        int pending = 0;
        int inProgress = 0;
        int ready = 0;
        int collected = 0;
        int cancelled = 0;
        int totalPages = 0;

        for (PrintOrder o : allOrders) {
            switch (o.getStatus()) {
                case PENDING:
                case SUBMITTED:
                    pending++;
                    break;
                case IN_PROGRESS:
                    inProgress++;
                    break;
                case READY_FOR_PICKUP:
                    ready++;
                    break;
                case COLLECTED:
                    collected++;
                    totalPages += o.getPages() * o.getCopies();
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
            }
        }

        ConsoleUI.printHeader("CAMPUS PRINTING SYSTEM STATISTICS");
        ConsoleUI.printBoxLine("Total Print Orders   : " + total, ConsoleUI.WHITE, false);
        ConsoleUI.printBoxLine("Pending in Queue     : " + pending, ConsoleUI.YELLOW, false);
        ConsoleUI.printBoxLine("In Progress          : " + inProgress, ConsoleUI.PURPLE, false);
        ConsoleUI.printBoxLine("Ready for Pickup     : " + ready, ConsoleUI.CYAN, false);
        ConsoleUI.printBoxLine("Collected / Done     : " + collected, ConsoleUI.GREEN, false);
        ConsoleUI.printBoxLine("Cancelled Orders     : " + cancelled, ConsoleUI.RED, false);
        ConsoleUI.printBoxLine("Total Pages Printed  : " + totalPages + " pages", ConsoleUI.GREEN, false);
        ConsoleUI.printBoxLine("Registered Students  : " + students.size(), ConsoleUI.WHITE, false);
        ConsoleUI.printBoxLine("Total Printers       : " + printers.size(), ConsoleUI.WHITE, false);
        System.out.println(ConsoleUI.CYAN + "╚" + "═".repeat(52) + "╝" + ConsoleUI.RESET);
    }
}
