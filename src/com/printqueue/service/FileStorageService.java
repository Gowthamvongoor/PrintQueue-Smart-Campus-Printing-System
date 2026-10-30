package com.printqueue.service;

import com.printqueue.model.OrderStatus;
import com.printqueue.model.PrintOrder;
import com.printqueue.model.Printer;
import com.printqueue.model.PrinterStatus;
import com.printqueue.model.Student;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent flat-file storage for students, print orders, and printers.
 * Uses core Java I/O (FileReader, FileWriter, BufferedReader, BufferedWriter).
 */
public class FileStorageService {
    private static final String DATA_DIR = "data";
    private static final String STUDENTS_FILE = DATA_DIR + File.separator + "students.txt";
    private static final String ORDERS_FILE = DATA_DIR + File.separator + "orders.txt";
    private static final String PRINTERS_FILE = DATA_DIR + File.separator + "printers.txt";
    private static final String DELIMITER = "\\|";
    private static final String WRITE_DELIMITER = "|";

    public FileStorageService() {
        ensureDataDirectoryAndFiles();
    }

    /**
     * Creates data folder and initial files if they do not exist.
     */
    private void ensureDataDirectoryAndFiles() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File sFile = new File(STUDENTS_FILE);
            if (!sFile.exists()) {
                sFile.createNewFile();
            }

            File oFile = new File(ORDERS_FILE);
            if (!oFile.exists()) {
                oFile.createNewFile();
            }

            File pFile = new File(PRINTERS_FILE);
            if (!pFile.exists()) {
                pFile.createNewFile();
                // Seed default campus printers if new
                List<Printer> defaults = new ArrayList<>();
                defaults.add(new Printer("PRINTER01", "HP LaserJet Pro 400 (Ground Floor Lab)", PrinterStatus.AVAILABLE));
                defaults.add(new Printer("PRINTER02", "Canon ImageRunner Advance (Library Center)", PrinterStatus.AVAILABLE));
                savePrinters(defaults);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to initialize storage files: " + e.getMessage());
        }
    }

    // ==================== STUDENTS ====================

    public List<Student> loadStudents() {
        List<Student> list = new ArrayList<>();
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 4) {
                    list.add(new Student(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading students: " + e.getMessage());
        }
        return list;
    }

    public void saveStudents(List<Student> students) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENTS_FILE))) {
            for (Student s : students) {
                String line = s.getStudentId() + WRITE_DELIMITER
                        + s.getName() + WRITE_DELIMITER
                        + s.getUsername() + WRITE_DELIMITER
                        + s.getPassword();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error saving students: " + e.getMessage());
        }
    }

    public void appendStudent(Student student) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENTS_FILE, true))) {
            String line = student.getStudentId() + WRITE_DELIMITER
                    + student.getName() + WRITE_DELIMITER
                    + student.getUsername() + WRITE_DELIMITER
                    + student.getPassword();
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("[ERROR] Error appending student: " + e.getMessage());
        }
    }

    // ==================== PRINTERS ====================

    public List<Printer> loadPrinters() {
        List<Printer> list = new ArrayList<>();
        File file = new File(PRINTERS_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 3) {
                    PrinterStatus status;
                    try {
                        status = PrinterStatus.valueOf(parts[2].trim().toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        status = PrinterStatus.AVAILABLE;
                    }
                    list.add(new Printer(parts[0].trim(), parts[1].trim(), status));
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error reading printers: " + e.getMessage());
        }

        // If list was empty, provide defaults
        if (list.isEmpty()) {
            list.add(new Printer("PRINTER01", "HP LaserJet Pro 400 (Ground Floor Lab)", PrinterStatus.AVAILABLE));
            list.add(new Printer("PRINTER02", "Canon ImageRunner Advance (Library Center)", PrinterStatus.AVAILABLE));
            savePrinters(list);
        }

        return list;
    }

    public void savePrinters(List<Printer> printers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRINTERS_FILE))) {
            for (Printer p : printers) {
                String line = p.getPrinterId() + WRITE_DELIMITER
                        + p.getPrinterName() + WRITE_DELIMITER
                        + p.getStatus().name();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error saving printers: " + e.getMessage());
        }
    }

    // ==================== ORDERS ====================

    public List<PrintOrder> loadOrders() {
        List<PrintOrder> list = new ArrayList<>();
        File file = new File(ORDERS_FILE);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 10) {
                    String orderId = parts[0];
                    String studentId = parts[1];
                    String docName = parts[2];
                    int pages = Integer.parseInt(parts[3]);
                    int copies = Integer.parseInt(parts[4]);
                    String printType = parts[5];
                    String paperSize = parts[6];
                    String printMode = parts[7];
                    String submittedTime = parts[8];
                    OrderStatus status;
                    try {
                        status = OrderStatus.valueOf(parts[9].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        status = OrderStatus.PENDING;
                    }
                    String assignedPrinter = (parts.length >= 11) ? parts[10] : "N/A";

                    list.add(new PrintOrder(orderId, studentId, docName, pages, copies,
                            printType, paperSize, printMode, submittedTime, status, assignedPrinter));
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error reading orders: " + e.getMessage());
        }
        return list;
    }

    public void saveOrders(List<PrintOrder> orders) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDERS_FILE))) {
            for (PrintOrder o : orders) {
                String line = o.getOrderId() + WRITE_DELIMITER
                        + o.getStudentId() + WRITE_DELIMITER
                        + o.getDocumentName() + WRITE_DELIMITER
                        + o.getPages() + WRITE_DELIMITER
                        + o.getCopies() + WRITE_DELIMITER
                        + o.getPrintType() + WRITE_DELIMITER
                        + o.getPaperSize() + WRITE_DELIMITER
                        + o.getPrintMode() + WRITE_DELIMITER
                        + o.getSubmittedTime() + WRITE_DELIMITER
                        + o.getStatus().name() + WRITE_DELIMITER
                        + o.getAssignedPrinter();
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error saving orders: " + e.getMessage());
        }
    }
}
