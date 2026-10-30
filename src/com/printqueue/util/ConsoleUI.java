package com.printqueue.util;

import com.printqueue.model.OrderStatus;
import com.printqueue.model.PrintOrder;
import com.printqueue.model.Printer;
import com.printqueue.model.PrinterStatus;

import java.util.List;
import java.util.Queue;

/**
 * Utility class for formatted console output, ASCII diagrams,
 * ANSI colored text, and animated terminal screens.
 */
public class ConsoleUI {

    // ANSI Color Codes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Standard width for UI boxes
    private static final int BOX_WIDTH = 54;

    /**
     * Prints a centered title inside a single-line or double-line box.
     */
    public static void printHeader(String title) {
        System.out.println();
        System.out.println(CYAN + "╔" + "═".repeat(BOX_WIDTH - 2) + "╗" + RESET);
        printBoxLine(title, CYAN, true);
        System.out.println(CYAN + "╚" + "═".repeat(BOX_WIDTH - 2) + "╝" + RESET);
    }

    /**
     * Formats and prints a single text line inside the standard box borders.
     */
    public static void printBoxLine(String content, String color, boolean center) {
        int maxLen = BOX_WIDTH - 4;
        if (content.length() > maxLen) {
            content = content.substring(0, maxLen - 3) + "...";
        }
        int padding = maxLen - content.length();
        int leftPad = center ? (padding / 2) : 1;
        int rightPad = padding - leftPad;

        System.out.print(color + "║ " + RESET);
        System.out.print(" ".repeat(Math.max(0, leftPad)));
        System.out.print(content);
        System.out.print(" ".repeat(Math.max(0, rightPad)));
        System.out.println(color + " ║" + RESET);
    }

    // ==================== NOTIFICATIONS ====================

    public static void success(String message) {
        System.out.println(GREEN + BOLD + "[SUCCESS] " + RESET + GREEN + message + RESET);
    }

    public static void info(String message) {
        System.out.println(CYAN + BOLD + "[INFO] " + RESET + CYAN + message + RESET);
    }

    public static void warning(String message) {
        System.out.println(YELLOW + BOLD + "[WARNING] " + RESET + YELLOW + message + RESET);
    }

    public static void error(String message) {
        System.out.println(RED + BOLD + "[ERROR] " + RESET + RED + message + RESET);
    }

    public static void processing(String message) {
        System.out.println(PURPLE + BOLD + "[PROCESSING] " + RESET + PURPLE + message + RESET);
    }

    // ==================== VISUAL LIVE QUEUE DISPLAY ====================

    public static void displayLiveQueue(Queue<PrintOrder> queue) {
        System.out.println();
        System.out.println(CYAN + "╔" + "═".repeat(BOX_WIDTH - 2) + "╗" + RESET);
        printBoxLine("LIVE PRINT QUEUE", CYAN, true);
        System.out.println(CYAN + "╠" + "═".repeat(BOX_WIDTH - 2) + "╣" + RESET);
        printBoxLine("", CYAN, false);

        if (queue.isEmpty()) {
            printBoxLine("Queue is currently empty.", YELLOW, true);
            printBoxLine("No pending print requests waiting.", YELLOW, true);
        } else {
            printBoxLine("NEXT", GREEN, false);
            printBoxLine(" |", GREEN, false);
            printBoxLine(" v", GREEN, false);

            // Build queue chain
            StringBuilder chain = new StringBuilder();
            StringBuilder statuses = new StringBuilder();
            int count = 0;
            int maxDisplay = 3;

            for (PrintOrder order : queue) {
                if (count < maxDisplay) {
                    if (count > 0) {
                        chain.append(" ---> ");
                        statuses.append("      ");
                    }
                    chain.append("[").append(order.getOrderId()).append("]");
                    statuses.append(" PENDING");
                }
                count++;
            }

            if (count > maxDisplay) {
                chain.append(" ---> ... (+").append(count - maxDisplay).append(" more)");
            }

            printBoxLine(chain.toString(), WHITE, false);
            printBoxLine(statuses.toString(), YELLOW, false);
        }

        printBoxLine("", CYAN, false);
        printBoxLine("Total Pending Orders: " + queue.size(), CYAN, false);
        System.out.println(CYAN + "╚" + "═".repeat(BOX_WIDTH - 2) + "╝" + RESET);
    }

    // ==================== VISUAL ORDER TIMELINE ====================

    public static void displayOrderTimeline(PrintOrder order, int queuePosition) {
        System.out.println();
        System.out.println(CYAN + "╔" + "═".repeat(BOX_WIDTH - 2) + "╗" + RESET);
        printBoxLine("ORDER TRACKING", CYAN, true);
        System.out.println(CYAN + "╠" + "═".repeat(BOX_WIDTH - 2) + "╣" + RESET);
        printBoxLine("", CYAN, false);
        printBoxLine("Order ID : " + order.getOrderId(), WHITE, false);
        printBoxLine("Document : " + order.getDocumentName(), WHITE, false);
        printBoxLine("Submitted: " + order.getSubmittedTime(), WHITE, false);
        if (queuePosition > 0) {
            printBoxLine("Queue Pos: #" + queuePosition + " in pending queue", YELLOW, false);
        }
        printBoxLine("", CYAN, false);

        OrderStatus s = order.getStatus();

        if (s == OrderStatus.CANCELLED) {
            printBoxLine("[✓] Submitted", GREEN, false);
            printBoxLine(" |", CYAN, false);
            printBoxLine("[✗] Order Cancelled by Student", RED, false);
        } else {
            // Stage 1: Submitted
            printBoxLine("[✓] Submitted", GREEN, false);
            printBoxLine(" |", CYAN, false);

            // Stage 2: Pending
            if (s == OrderStatus.SUBMITTED) {
                printBoxLine("[>] Pending", YELLOW, false);
            } else {
                printBoxLine("[✓] Pending", GREEN, false);
            }
            printBoxLine(" |", CYAN, false);

            // Stage 3: In Progress
            if (s == OrderStatus.IN_PROGRESS) {
                printBoxLine("[>] In Progress (Printing on " + order.getAssignedPrinter() + ")", PURPLE, false);
            } else if (s == OrderStatus.READY_FOR_PICKUP || s == OrderStatus.COLLECTED) {
                printBoxLine("[✓] In Progress", GREEN, false);
            } else {
                printBoxLine("[ ] In Progress", WHITE, false);
            }
            printBoxLine(" |", CYAN, false);

            // Stage 4: Ready for Pickup
            if (s == OrderStatus.READY_FOR_PICKUP) {
                printBoxLine("[>] Ready for Pickup (Go to Counter)", YELLOW, false);
            } else if (s == OrderStatus.COLLECTED) {
                printBoxLine("[✓] Ready for Pickup", GREEN, false);
            } else {
                printBoxLine("[ ] Ready for Pickup", WHITE, false);
            }
            printBoxLine(" |", CYAN, false);

            // Stage 5: Collected
            if (s == OrderStatus.COLLECTED) {
                printBoxLine("[✓] Collected", GREEN, false);
            } else {
                printBoxLine("[ ] Collected", WHITE, false);
            }
        }

        printBoxLine("", CYAN, false);
        System.out.println(CYAN + "╚" + "═".repeat(BOX_WIDTH - 2) + "╝" + RESET);
    }

    // ==================== VISUAL PRINTER MONITOR ====================

    public static void displayPrinterMonitor(List<Printer> printers) {
        System.out.println();
        System.out.println(CYAN + "╔" + "═".repeat(BOX_WIDTH - 2) + "╗" + RESET);
        printBoxLine("PRINTER MONITOR", CYAN, true);
        System.out.println(CYAN + "╠" + "═".repeat(BOX_WIDTH - 2) + "╣" + RESET);
        printBoxLine("", CYAN, false);

        int availableCount = 0;
        int busyCount = 0;
        int offlineCount = 0;

        for (Printer p : printers) {
            String statusTag;
            if (p.getStatus() == PrinterStatus.AVAILABLE) {
                statusTag = GREEN + "[ AVAILABLE ]" + RESET;
                availableCount++;
            } else if (p.getStatus() == PrinterStatus.BUSY) {
                statusTag = YELLOW + "[ BUSY ]" + RESET;
                busyCount++;
            } else {
                statusTag = RED + "[ OFFLINE ]" + RESET;
                offlineCount++;
            }

            // Align printer name and status
            String pName = p.getPrinterId() + " (" + p.getPrinterName() + ")";
            if (pName.length() > 32) {
                pName = pName.substring(0, 29) + "...";
            }
            String rawLine = String.format("%-33s %s", pName, statusTag);
            System.out.print(CYAN + "║ " + RESET);
            System.out.print(rawLine);
            // Calculate padding based on uncolored length
            int plainLen = pName.length() + 1 + ("[" + p.getStatus() + "]").length();
            int pad = Math.max(0, BOX_WIDTH - 4 - plainLen);
            System.out.print(" ".repeat(pad));
            System.out.println(CYAN + " ║" + RESET);
        }

        printBoxLine("", CYAN, false);
        printBoxLine("Total Printers : " + printers.size(), CYAN, false);
        printBoxLine("Available      : " + availableCount, GREEN, false);
        printBoxLine("Busy           : " + busyCount, YELLOW, false);
        if (offlineCount > 0) {
            printBoxLine("Offline        : " + offlineCount, RED, false);
        }
        printBoxLine("", CYAN, false);
        System.out.println(CYAN + "╚" + "═".repeat(BOX_WIDTH - 2) + "╝" + RESET);
    }

    // ==================== ANIMATED PRINTING SCREEN ====================

    public static void displayPrintingAnimation(PrintOrder order, Printer printer) {
        System.out.println();
        System.out.println(PURPLE + "╔" + "═".repeat(BOX_WIDTH - 2) + "╗" + RESET);
        printBoxLine("PRINTING IN PROGRESS", PURPLE, true);
        System.out.println(PURPLE + "╠" + "═".repeat(BOX_WIDTH - 2) + "╣" + RESET);
        printBoxLine("", PURPLE, false);
        printBoxLine("Order ID : " + order.getOrderId(), WHITE, false);
        printBoxLine("File     : " + order.getDocumentName(), WHITE, false);
        printBoxLine("Pages    : " + order.getPages() + " pages x " + order.getCopies() + " copies", WHITE, false);
        printBoxLine("Type     : " + order.getPrintType() + " (" + order.getPaperSize() + ", " + order.getPrintMode() + ")", WHITE, false);
        printBoxLine("Printer  : " + printer.getPrinterId(), WHITE, false);
        printBoxLine("", PURPLE, false);
        printBoxLine("Printing Document...", YELLOW, false);
        printBoxLine("", PURPLE, false);

        int totalSteps = 20;
        int barLength = 20;

        for (int i = 0; i <= totalSteps; i++) {
            int percent = (i * 100) / totalSteps;
            int filled = (i * barLength) / totalSteps;
            int unfilled = barLength - filled;

            StringBuilder bar = new StringBuilder();
            bar.append("[");
            bar.append("█".repeat(filled));
            bar.append("░".repeat(unfilled));
            bar.append(String.format("] %3d%%", percent));

            // Carriage return to animate smoothly
            System.out.print("\r" + PURPLE + "║ " + RESET + GREEN + String.format("%-48s", "  " + bar.toString()) + PURPLE + " ║" + RESET);

            try {
                Thread.sleep(120); // ~2.4 seconds total animation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println();
        printBoxLine("", PURPLE, false);
        printBoxLine("Finishing and ejecting paper...", CYAN, false);
        printBoxLine("Print job completed successfully!", GREEN, true);
        printBoxLine("", PURPLE, false);
        System.out.println(PURPLE + "╚" + "═".repeat(BOX_WIDTH - 2) + "╝" + RESET);
    }
}
