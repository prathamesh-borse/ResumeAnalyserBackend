package com.analyse.resume.ResumeAnalyser.config;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class FooterPageEvent extends PdfPageEventHelper {

    private String username;
    private Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

    public FooterPageEvent(String username) {
        this.username = username;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle pageSize = document.getPageSize();

        // Create footer content with 2 columns (not 3)
        PdfPTable footer = new PdfPTable(2);
        try {
            footer.setWidths(new int[]{3, 1}); // Left wider, right narrower
            footer.setTotalWidth(pageSize.getWidth() - document.leftMargin() - document.rightMargin());
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(20);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

            // Left: Company/App name
            PdfPCell leftCell = new PdfPCell(new Phrase("SkillMatch AI | Generated for: " + username, footerFont));
            leftCell.setBorder(Rectangle.TOP);
            leftCell.setBorderColor(BaseColor.LIGHT_GRAY);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            leftCell.setPaddingTop(5);
            leftCell.setPaddingBottom(5);
            footer.addCell(leftCell);

            // Right: Page number
            PdfPCell rightCell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
            rightCell.setBorder(Rectangle.TOP);
            rightCell.setBorderColor(BaseColor.LIGHT_GRAY);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            rightCell.setPaddingTop(5);
            rightCell.setPaddingBottom(5);
            footer.addCell(rightCell);

            // Position the footer at the bottom - CORRECTED positioning
            float footerYPosition = document.bottom() - 10; // 10 points above bottom margin
            footer.writeSelectedRows(0, -1, document.leftMargin(),
                    footerYPosition, writer.getDirectContent());

        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
}