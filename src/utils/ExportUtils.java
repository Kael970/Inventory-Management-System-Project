package utils;

import models.Product;
import models.Sale;
import models.Request;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Export utilities for CSV outputs
 */
public class ExportUtils {

    public static void exportProductsToCSV(List<Product> products, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("product_id,product_name,buying_price,selling_price,stock_quantity,threshold_value,expiry_date\n");
            for (Product p : products) {
                fw.write(String.format("%d,%s,%.2f,%.2f,%d,%d,%s\n",
                        p.getProductId(),
                        escape(p.getProductName()),
                        p.getBuyingPrice(),
                        p.getSellingPrice(),
                        p.getStockQuantity(),
                        p.getThresholdValue(),
                        p.getExpiryDate() != null ? p.getExpiryDate().toString() : ""));
            }
        }
    }

    public static void exportSalesToCSV(List<Sale> sales, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("sale_id,product_id,product_name,quantity,unit_price,total_price,sale_date,user_id\n");
            for (Sale s : sales) {
                fw.write(String.format("%d,%d,%s,%d,%.2f,%.2f,%s,%d\n",
                        s.getSaleId(),
                        s.getProductId(),
                        escape(s.getProductName()),
                        s.getQuantity(),
                        s.getUnitPrice(),
                        s.getTotalPrice(),
                        s.getSaleDate() != null ? s.getSaleDate().toString() : "",
                        s.getUserId()));
            }
        }
    }

    // Add Excel export for sales
    public static void exportSalesToExcel(List<Sale> sales, File file) throws IOException {
        ExcelExportUtils.exportSalesToExcel(sales, file);
    }

    // Add Excel export for requests
    public static void exportRequestsToExcel(List<Request> requests, File file) throws IOException {
        ExcelExportUtils.exportRequestsToExcel(requests, file);
    }

    public static void exportRequestsToCSV(List<Request> requests, File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("request_id,product_id,product_name,requested_quantity,requested_by,status,request_date\n");
            for (Request r : requests) {
                fw.write(String.format("%d,%d,%s,%d,%s,%s,%s\n",
                        r.getRequestId(),
                        r.getProductId(),
                        escape(r.getProductName()),
                        r.getRequestedQuantity(),
                        escape(r.getRequestedBy()),
                        r.getStatus(),
                        r.getRequestDate() != null ? r.getRequestDate().toString() : ""
                ));
            }
        }
    }

    private static String escape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
