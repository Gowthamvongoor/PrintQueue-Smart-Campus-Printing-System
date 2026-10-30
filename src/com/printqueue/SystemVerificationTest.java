package com.printqueue;

import com.printqueue.model.OrderStatus;
import com.printqueue.model.PrintOrder;
import com.printqueue.model.Printer;
import com.printqueue.model.PrinterStatus;
import com.printqueue.model.Student;
import com.printqueue.service.PrintQueueService;
import com.printqueue.util.ConsoleUI;

import java.util.List;

/**
 * Automated end-to-end verification test ensuring all core requirements
 * function correctly and reliably.
 */
public class SystemVerificationTest {
    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  RUNNING PRINTQUEUE VERIFICATION SUITE");
        System.out.println("====================================================");

        PrintQueueService service = new PrintQueueService();

        // 1. Student Registration & Validation
        System.out.println("\n[TEST 1] Student Registration & Duplicate Prevention");
        boolean reg1 = service.registerStudent("23BCE1001", "Alice Sharma", "alice", "alice123");
        assertCondition(reg1, "First registration should succeed.");

        boolean regDup = service.registerStudent("23BCE1001", "Alice Clone", "alice_clone", "pass");
        assertCondition(!regDup, "Duplicate student ID should be rejected.");

        boolean regUserDup = service.registerStudent("23BCE1002", "Bob Vance", "alice", "pass");
        assertCondition(!regUserDup, "Duplicate username should be rejected.");

        // 2. Student Login
        System.out.println("\n[TEST 2] Student Authentication");
        Student loggedStudent = service.loginStudent("alice", "alice123");
        assertCondition(loggedStudent != null && loggedStudent.getName().equals("Alice Sharma"), "Student login with valid credentials.");

        Student badLogin = service.loginStudent("alice", "wrongpass");
        assertCondition(badLogin == null, "Student login with invalid credentials should fail.");

        // 3. Print Request Submission & Order ID generation
        System.out.println("\n[TEST 3] Order Submission & FIFO Enqueue");
        PrintOrder o1 = service.submitOrder(loggedStudent.getStudentId(), "Data_Structures_Lab.pdf", 10, 1, "Black & White", "A4", "Single-sided");
        PrintOrder o2 = service.submitOrder(loggedStudent.getStudentId(), "AI_Research_Paper.pdf", 25, 2, "Color", "A4", "Double-sided");
        PrintOrder o3 = service.submitOrder(loggedStudent.getStudentId(), "Campus_Map.pdf", 1, 3, "Color", "A3", "Single-sided");

        assertCondition(o1 != null && o1.getOrderId().startsWith("PQ"), "Order 1 ID generated properly: " + o1.getOrderId());
        assertCondition(o2 != null && o2.getOrderId().startsWith("PQ"), "Order 2 ID generated properly: " + o2.getOrderId());
        assertCondition(o3 != null && o3.getOrderId().startsWith("PQ"), "Order 3 ID generated properly: " + o3.getOrderId());

        // 4. Queue Positions
        System.out.println("\n[TEST 4] Queue Position Tracking");
        int pos1 = service.getQueuePosition(o1.getOrderId());
        int pos2 = service.getQueuePosition(o2.getOrderId());
        int pos3 = service.getQueuePosition(o3.getOrderId());
        assertCondition(pos1 == 1, "Order 1 should be #1 in queue (Actual: " + pos1 + ")");
        assertCondition(pos2 == 2, "Order 2 should be #2 in queue (Actual: " + pos2 + ")");
        assertCondition(pos3 == 3, "Order 3 should be #3 in queue (Actual: " + pos3 + ")");

        // 5. Visual Displays
        System.out.println("\n[TEST 5] Live Queue Display Visualization");
        ConsoleUI.displayLiveQueue(service.getPrintQueue());

        System.out.println("\n[TEST 6] Printer Monitor Visualization");
        ConsoleUI.displayPrinterMonitor(service.getPrinters());

        // 6. Multithreaded Processing
        System.out.println("\n[TEST 7] Simulated Multithreaded Printing of Order 1");
        service.processNextOrder();
        assertCondition(o1.getStatus() == OrderStatus.READY_FOR_PICKUP, "Order 1 status should be READY_FOR_PICKUP");
        assertCondition(service.getQueuePosition(o1.getOrderId()) == -1, "Order 1 should no longer be in pending queue");

        // 7. Order Timeline
        System.out.println("\n[TEST 8] Visual Timeline Rendering");
        ConsoleUI.displayOrderTimeline(o1, -1);

        // 8. Order Collection
        System.out.println("\n[TEST 9] Order Collection");
        boolean collected = service.collectOrder(o1.getOrderId(), loggedStudent.getStudentId());
        assertCondition(collected && o1.getStatus() == OrderStatus.COLLECTED, "Order 1 collected successfully");

        // 9. Order Cancellation
        System.out.println("\n[TEST 10] Order Cancellation of Order 3");
        boolean cancelled = service.cancelOrder(o3.getOrderId(), loggedStudent.getStudentId());
        assertCondition(cancelled && o3.getStatus() == OrderStatus.CANCELLED, "Order 3 cancelled successfully");
        assertCondition(service.getQueuePosition(o3.getOrderId()) == -1, "Cancelled order 3 removed from queue");

        // 10. Admin Statistics
        System.out.println("\n[TEST 11] Admin Statistics");
        service.displayStatistics();

        System.out.println("\n====================================================");
        System.out.println("  ALL TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("====================================================");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            System.err.println("[FAIL] " + message);
            System.exit(1);
        } else {
            System.out.println("  [PASS] " + message);
        }
    }
}
