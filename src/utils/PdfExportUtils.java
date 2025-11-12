package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; // Added for PDFBox 3.x
import models.Request;
import java.io.File;
import java.util.List;

/**
 * Minimal PDF export utilizing JTable's print mechanism. On systems with a PDF printer, this allows saving as PDF.
 * For rich PDFs, integrate iText or PDFBox; kept minimal to avoid external dependencies.
 */
public class PdfExportUtils {
    public static void exportRequestsToPDF(List<Request> requests, File file) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream content = new PDPageContentStream(doc, page);
            content.beginText();
            // Use built-in font for PDFBox 3.x (Standard14Fonts)
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            content.newLineAtOffset(50, 750);
            content.showText("Requests Report");
            content.newLineAtOffset(0, -30);
            String header = "ID | Product ID | Product Name | Qty | By | Status | Date";
            content.showText(header);
            content.newLineAtOffset(0, -20);
            for (Request r : requests) {
                String line = String.format("%d | %d | %s | %d | %s | %s | %s",
                        r.getRequestId(), r.getProductId(), r.getProductName(), r.getRequestedQuantity(),
                        r.getRequestedBy(), r.getStatus(), r.getRequestDate() != null ? r.getRequestDate().toString() : "");
                content.showText(line);
                content.newLineAtOffset(0, -15);
            }
            content.endText();
            content.close();
            doc.save(file);
        }
    }
}
