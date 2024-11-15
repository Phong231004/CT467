package ui;

class InvoiceItem {

    private String customerName;
    private String customerPhone;
    private String employeeName;
    private String invoiceId;
    private String deviceId;
    private String deviceName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    // Constructor
    public InvoiceItem(String customerName, String customerPhone, String employeeName, String invoiceId,
                       String deviceId, String deviceName, int quantity, double unitPrice, double totalPrice) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.employeeName = employeeName;
        this.invoiceId = invoiceId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getter methods
    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
