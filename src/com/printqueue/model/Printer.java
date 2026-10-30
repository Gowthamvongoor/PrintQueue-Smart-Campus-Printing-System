package com.printqueue.model;

/**
 * Represents a physical campus printer simulated in the system.
 */
public class Printer {
    private String printerId;
    private String printerName;
    private PrinterStatus status;

    public Printer(String printerId, String printerName, PrinterStatus status) {
        this.printerId = printerId;
        this.printerName = printerName;
        this.status = status;
    }

    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public PrinterStatus getStatus() {
        return status;
    }

    public void setStatus(PrinterStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == PrinterStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return printerId + " (" + printerName + ") [" + status + "]";
    }
}
