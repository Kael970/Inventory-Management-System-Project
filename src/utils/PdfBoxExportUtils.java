package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.io.File;

public class PdfBoxExportUtils {
    public static <T> void exportTableToPDF(TableView<T> table, File file, String title) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        cs.setFont(fontBold, 16);
        cs.beginText();
        cs.newLineAtOffset(50, 750);
        cs.showText(title);
        cs.endText();
        cs.setFont(fontRegular, 10);
        float y = 730;
        int colIdx = 0;
        for (TableColumn<T, ?> col : table.getColumns()) {
            cs.beginText();
            cs.newLineAtOffset(50 + colIdx * 100, y);
            cs.showText(col.getText());
            cs.endText();
            colIdx++;
        }
        y -= 20;
        for (T item : table.getItems()) {
            colIdx = 0;
            for (TableColumn<T, ?> col : table.getColumns()) {
                cs.beginText();
                cs.newLineAtOffset(50 + colIdx * 100, y);
                Object val = col.getCellObservableValue(item).getValue();
                cs.showText(val == null ? "" : val.toString());
                cs.endText();
                colIdx++;
            }
            y -= 15;
            if (y < 50) break;
        }
        cs.close();
        doc.save(file);
        doc.close();
    }

    public static void exportTextToPDF(String text, File file, String title) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.LETTER);
        doc.addPage(page);
        PDPageContentStream cs = new PDPageContentStream(doc, page);
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        cs.setFont(fontBold, 16);
        cs.beginText();
        cs.newLineAtOffset(50, 750);
        cs.showText(title);
        cs.endText();
        cs.setFont(fontRegular, 10);
        float y = 730;
        for (String line : text.split("\n")) {
            cs.beginText();
            cs.newLineAtOffset(50, y);
            cs.showText(line);
            cs.endText();
            y -= 15;
            if (y < 50) break;
        }
        cs.close();
        doc.save(file);
        doc.close();
    }
}
