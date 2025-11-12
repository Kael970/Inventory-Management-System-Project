package utils;

import models.Product;
import models.Sale;
import models.Request;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports data to Excel 2003 XML Spreadsheet format (single worksheet).
 * Avoids external library dependencies (Apache POI) while remaining importable by Excel.
 */
public class ExcelExportUtils {

    private static final String XML_HEADER = "<?xml version=\"1.0\"?>" +
            "\n<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"" +
            " xmlns:o=\"urn:schemas-microsoft-com:office:office\"" +
            " xmlns:x=\"urn:schemas-microsoft-com:office:excel\"" +
            " xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"" +
            " xmlns:html=\"http://www.w3.org/TR/REC-html40\">";

    public static void exportProductsToExcel(List<Product> products, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(XML_HEADER);
            fw.write("\n<Worksheet ss:Name=\"Products\">\n<Table>\n");
            // Header row
            writeRow(fw, "Product ID", "Name", "Buying Price", "Selling Price", "Quantity", "Threshold", "Expiry Date", "Availability");
            for (Product p : products) {
                writeRow(fw,
                        String.valueOf(p.getProductId()),
                        safe(p.getProductName()),
                        formatPrice(p.getBuyingPrice()),
                        formatPrice(p.getSellingPrice()),
                        String.valueOf(p.getStockQuantity()),
                        String.valueOf(p.getThresholdValue()),
                        p.getExpiryDate() != null ? p.getExpiryDate().toString() : "",
                        safe(p.getAvailabilityStatus())
                );
            }
            fw.write("</Table>\n</Worksheet>\n</Workbook>");
        }
    }

    public static void exportSalesToExcel(List<Sale> sales, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(XML_HEADER);
            fw.write("\n<Worksheet ss:Name=\"Sales\">\n<Table>\n");
            writeRow(fw, "Sale ID", "Product", "Quantity", "Unit Price", "Total Price", "Date", "User ID");
            for (Sale s : sales) {
                writeRow(fw,
                        String.valueOf(s.getSaleId()),
                        safe(s.getProductName()),
                        String.valueOf(s.getQuantity()),
                        formatPrice(s.getUnitPrice()),
                        formatPrice(s.getTotalPrice()),
                        s.getSaleDate() != null ? s.getSaleDate().toString() : "",
                        String.valueOf(s.getUserId())
                );
            }
            fw.write("</Table>\n</Worksheet>\n</Workbook>");
        }
    }

    public static void exportRequestsToExcel(List<Request> requests, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(XML_HEADER);
            fw.write("\n<Worksheet ss:Name=\"Requests\">\n<Table>\n");
            writeRow(fw, "Request ID", "Product ID", "Product Name", "Requested Qty", "Requested By", "Status", "Request Date");
            for (Request r : requests) {
                writeRow(fw,
                        String.valueOf(r.getRequestId()),
                        String.valueOf(r.getProductId()),
                        safe(r.getProductName()),
                        String.valueOf(r.getRequestedQuantity()),
                        safe(r.getRequestedBy()),
                        safe(r.getStatus()),
                        r.getRequestDate() != null ? r.getRequestDate().toString() : ""
                );
            }
            fw.write("</Table>\n</Worksheet>\n</Workbook>");
        }
    }

    private static void writeRow(FileWriter fw, String... cells) throws IOException {
        fw.write("<Row>");
        for (String cell : cells) {
            fw.write("<Cell><Data ss:Type=\"String\">" + safe(cell) + "</Data></Cell>");
        }
        fw.write("</Row>\n");
    }

    private static String formatPrice(double v) { return String.format("%.2f", v); }

    private static String safe(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}
