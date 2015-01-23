package com.digital.bills;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by vedhavyas.singareddi on 1/23/2015.
 */
public class PDFGenerator {

    private List<Bill> bills;
    private String title;
    private Context context;
    private ParseUser user;

    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    public PDFGenerator(Context context, List<Bill> bills, String title) {
        this.context = context;
        this.bills = bills;
        this.title = title;
        this.user = ParseUser.getCurrentUser();
    }

    public String generatePdf(){
        try {
            String fileName =  title+"_bill_summary.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(new File(Environment.getExternalStorageDirectory(), fileName)));
            document.open();
            addMetaData(document);
            addContent(document);
            document.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addMetaData(Document document) {
        document.addTitle(title.toUpperCase() + " - Bill Summary");
        document.addSubject("Bill Summary");
        document.addKeywords("PDF");
        document.addAuthor(user.getEmail());
        document.addCreator("My Bills Lite");
    }


    private void insertCell(PdfPTable table, String text, int align, int colspan, Font font){

        //create a new cell with the specified Text and Font
        PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
        //set the cell alignment
        cell.setHorizontalAlignment(align);
        //set the cell column span in case you want to merge two or more cells
        cell.setColspan(colspan);
        //in case there is no text and you wan to create an empty row
        if(text.trim().equalsIgnoreCase("")){
            cell.setMinimumHeight(10f);
        }
        //add the call to the table
        table.addCell(cell);

    }

    private void addContent(Document document) throws DocumentException {

        Paragraph paragraph = new Paragraph(title+" - Bill Summary");
        float[] columnWidths = {2f, 2f, 5f, 2f, 2f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(90f);

        //insert column headings
        insertCell(table, "S.No", Element.ALIGN_RIGHT, 1, smallBold);
        insertCell(table, "Bill No", Element.ALIGN_LEFT, 1, smallBold);
        insertCell(table, "Bill Description", Element.ALIGN_LEFT, 1, smallBold);
        insertCell(table, "Bill Category", Element.ALIGN_LEFT, 1, smallBold);
        insertCell(table, "Bill Amount", Element.ALIGN_RIGHT, 1, smallBold);
        table.setHeaderRows(1);

        float total = 0;
        for(int x=0; x<bills.size(); x++){
            Bill bill = bills.get(x);

            insertCell(table, String.valueOf(x+1), Element.ALIGN_RIGHT, 1, smallBold);
            if(bill.getBillID() != null) {
                insertCell(table, bill.getBillID(), Element.ALIGN_LEFT, 1, smallBold);
            }

            if(bill.getDescription() != null){
                insertCell(table, bill.getDescription(), Element.ALIGN_LEFT, 1, smallBold);
            }

            if(bill.getCategory() != null){
                insertCell(table, bill.getCategory(), Element.ALIGN_LEFT, 1, smallBold);
            }

            if (bill.getAmount() > 0){
                total += bill.getAmount();
                insertCell(table, String.valueOf(bill.getAmount()), Element.ALIGN_RIGHT, 1, smallBold);
            }

        }

        insertCell(table, "Total amount", Element.ALIGN_RIGHT, 4, smallBold);
        insertCell(table, String.valueOf(total), Element.ALIGN_RIGHT, 1, smallBold);

        paragraph.add(table);
        document.add(paragraph);

        for(Bill bill : bills){
            if(bill.getBill() != null){
                document.newPage();
                addImage(document, Utilities.getBitmapFromBlob(bill.getBill()));
            }
        }
    }

    public void addImage(Document document,Bitmap bitmap) throws DocumentException {
        try {

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            Image image = Image.getInstance(imageInByte);
            image.setAlignment(Element.ALIGN_MIDDLE);
            document.add(image);
        }
        catch(IOException ex)
        {
            return;
        }
    }

}
