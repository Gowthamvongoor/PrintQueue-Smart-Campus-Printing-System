package com.printqueue.model;

/**
 * Represents a document print request submitted by a student.
 */
public class PrintOrder {
    private String orderId;
    private String studentId;
    private String documentName;
    private int pages;
    private int copies;
    private String printType;   // "Black & White" or "Color"
    private String paperSize;   // "A4" or "A3"
    private String printMode;   // "Single-sided" or "Double-sided"
    private String submittedTime;
    private OrderStatus status;
    private String assignedPrinter; // e.g. "PRINTER01" or "N/A"

    public PrintOrder(String orderId, String studentId, String documentName, int pages, int copies,
                      String printType, String paperSize, String printMode, String submittedTime,
                      OrderStatus status, String assignedPrinter) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.documentName = documentName;
        this.pages = pages;
        this.copies = copies;
        this.printType = printType;
        this.paperSize = paperSize;
        this.printMode = printMode;
        this.submittedTime = submittedTime;
        this.status = status;
        this.assignedPrinter = (assignedPrinter == null || assignedPrinter.trim().isEmpty()) ? "N/A" : assignedPrinter;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }

    public String getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
    }

    public String getPrintMode() {
        return printMode;
    }

    public void setPrintMode(String printMode) {
        this.printMode = printMode;
    }

    public String getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(String submittedTime) {
        this.submittedTime = submittedTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getAssignedPrinter() {
        return assignedPrinter;
    }

    public void setAssignedPrinter(String assignedPrinter) {
        this.assignedPrinter = assignedPrinter;
    }

    public int getTotalSheets() {
        int total = pages * copies;
        if ("Double-sided".equalsIgnoreCase(printMode)) {
            return (int) Math.ceil(total / 2.0);
        }
        return total;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d pages, %d copies, %s) - Status: %s",
                orderId, documentName, pages, copies, printType, status);
    }
}
