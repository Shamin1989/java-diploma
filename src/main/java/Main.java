import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        LinksSuggester linksSuggester = new LinksSuggester(new File("data/config"));
        var pathIn = new File("data/pdfs/");
        var pathOut = new File("data/converted/");
        convertAllPdf(linksSuggester, pathIn, pathOut);

    }
    static void convertAllPdf(LinksSuggester linksSuggester, File pathIn, File pathOut) throws IOException {
        List<Suggest> allSuggests = linksSuggester.getAllSuggests();
        for (File fileIn : (Objects.requireNonNull(pathIn.listFiles()))) {
            Map<String, Boolean> existSuggestInPdf = new HashMap<>();
            for (Suggest sug : allSuggests) {
                existSuggestInPdf.put(sug.getKeyWord(), false);
            }
            File fileOut = new File(pathOut, fileIn.getName());
            var oldDoc = new PdfDocument(new PdfReader(fileIn));
            var newDoc = new PdfDocument(new PdfWriter(fileOut));
            int countOfPages = oldDoc.getNumberOfPages();
            for (int i = 1; i <= countOfPages; i++) {
                oldDoc.copyPagesTo(i, i, newDoc);
                PdfPage page = oldDoc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(page);
                List<Suggest> findSuggests = linksSuggester.suggest(text);
                List<Suggest> pageSuggests = new ArrayList<>();
                boolean isFind = false;
                for (Suggest sug : findSuggests) {
                    if (!existSuggestInPdf.get(sug.getKeyWord())) {
                        pageSuggests.add(sug);
                        existSuggestInPdf.put(sug.getKeyWord(), true);
                        isFind = true;
                    }
                }
                if (isFind) {
                    var newPage = newDoc.addNewPage();
                    var rect = new Rectangle(newPage.getPageSize()).moveRight(10).moveDown(10);
                    Canvas canvas = new Canvas(newPage, rect);
                    Paragraph paragraph = new Paragraph("Suggestions:\n");
                    paragraph.setFontSize(25);

                    for (Suggest suggest : pageSuggests) {
                        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
                        PdfAction action = PdfAction.createURI(suggest.getUrl());
                        annotation.setAction(action);
                        Link link = new Link(suggest.getTitle(), annotation);
                        paragraph.add(link.setUnderline());
                        paragraph.add("\n");
                    }

                    canvas.add(paragraph);
                }
            }
            oldDoc.close();
            newDoc.close();
        }
    }
}
