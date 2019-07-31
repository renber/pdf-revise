package de.renebergelt.pdfrevise.tasks;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import de.renebergelt.pdfrevise.types.PageFilter;
import de.renebergelt.pdfrevise.types.TaskFailedException;
import de.renebergelt.pdfrevise.types.TaskOptions;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Replaces text in a pdf
 */
public class ReplaceText implements PdfTask<ReplaceText.ReplaceTextOptions> {

    @Override
    public String getDescription() {
        return "Replacing text (experimental!)";
    }

    @Parameters(separators = "=", commandDescription = "Replaces text in a pdf (experimental!)")
    public static class ReplaceTextOptions implements TaskOptions {
        @Override
        public String getTaskVerb() {
            return "replace-text";
        }

        @Parameter(required = true, description = "The text to be replaced")
        public String what = "";

        @Parameter(names = {"--with"}, description = "The text to insert")
        public String with = "";
    }

    @Override
    public ReplaceTextOptions getDefaultOptions() {
        return new ReplaceTextOptions();
    }

    @Override
    public void process(ReplaceTextOptions options, InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            // use pdfbox to replace text
            // based on https://github.com/chadilukito/Apache-PdfBox-2-Examples/blob/master/ReplaceText.java

            PDDocument document = PDDocument.load(inStream);

            int pageCount = document.getNumberOfPages();

            String what = options.what.replace(" ", "");

            for (int p = 1; p <= pageCount; p++  )
            {
                if (filter.isPageInFilter(p, pageCount)) {
                    PDPage page = document.getPage(p - 1);
                    PDFStreamParser parser = new PDFStreamParser(page);
                    parser.parse();
                    List tokens = parser.getTokens();

                    for (int j = 0; j < tokens.size(); j++)
                    {
                        Object next = tokens.get(j);
                        if (next instanceof Operator)
                        {
                            Operator op = (Operator) next;

                            String pstring = "";
                            int prej = 0;

                            //Tj and TJ are the two operators that display strings in a PDF
                            if (op.getName().equals("Tj"))
                            {
                                // Tj takes one operator and that is the string to display so lets update that operator
                                COSString previous = (COSString) tokens.get(j - 1);
                                String string = previous.getString();
                                if (string.contains(what)) {
                                    string = string.replace(what, options.with);
                                    previous.setValue(string.getBytes());
                                }
                            } else
                            if (op.getName().equals("TJ"))
                            {
                                COSArray previous = (COSArray) tokens.get(j - 1);
                                for (int k = 0; k < previous.size(); k++)
                                {
                                    Object arrElement = previous.getObject(k);
                                    if (arrElement instanceof COSString)
                                    {
                                        COSString cosString = (COSString) arrElement;
                                        String string = cosString.getString();

                                        if (j == prej) {
                                            pstring += string;
                                        } else {
                                            prej = j;
                                            pstring = string;
                                        }
                                    }
                                }


                                if (pstring.trim().contains(what))
                                {
                                    COSString cosString2 = (COSString) previous.getObject(0);
                                    String newString = pstring.replace(what, options.with);
                                    cosString2.setValue(newString.getBytes());

                                    int total = previous.size()-1;
                                    for (int k = total; k > 0; k--) {
                                        previous.remove(k);
                                    }
                                }
                            }
                        }
                    }

                    // now that the tokens are updated we will replace the page content stream.
                    PDStream updatedStream = new PDStream(document);
                    OutputStream out = updatedStream.createOutputStream(COSName.FLATE_DECODE);
                    ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
                    tokenWriter.writeTokens(tokens);
                    out.close();
                    page.setContents(updatedStream);
                }

                progressCallback.accept(p / (float)pageCount);
            }

            document.save(outStream);
            document.close();

        } catch (Exception e) {
            throw new TaskFailedException("Error while replacing text: " + e.getMessage(), e);
        }
    }

}
