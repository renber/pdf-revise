package de.renebergelt.pdfrevise.tasks;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.function.Consumer;

public class DisableCopyPaste implements PdfTask {

    @Override
    public String getDescription() {
        return "Disabling copy&paste";
    }

    @Override
    public void process(InputStream inStream, OutputStream outStream, PageFilter filter, Consumer<Float> progressCallback) throws TaskFailedException {
        try {
            PdfReader reader = new PdfReader(inStream);
            PdfStamper stamper = new PdfStamper(reader, outStream);

            // the idea is to encrypt the pdf with a random OWNER password so that nobody can enabled the required permissions
            stamper.setEncryption(null, randomString(32).getBytes(),   ~(PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_MODIFY_CONTENTS /*| PdfWriter.ALLOW_PRINTING*/ ), PdfWriter.STANDARD_ENCRYPTION_128);

            stamper.close();
        } catch (IOException | DocumentException e) {
            throw new TaskFailedException("Setting pdf permissions failed: " + e.getMessage(), e);
        }

        progressCallback.accept(Float.valueOf(1));
    }

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random rand = new Random();

        for(int i = 0; i < length; i++) {
            sb.append((char) 32 + rand.nextInt(127 - 32));
        }

        return sb.toString();
    }
}
