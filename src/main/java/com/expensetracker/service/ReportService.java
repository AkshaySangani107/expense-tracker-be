package com.expensetracker.service;

import com.expensetracker.model.Transaction;
import com.expensetracker.repository.TransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public byte[] generatePdfReport(Long userId, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getCategory().getType().name())) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("Expense Tracker Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date Range
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Report Period: " + startDateStr + " to " + endDateStr, normalFont));
            document.add(new Paragraph(" ", normalFont));

            // Summary Table
            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            addCellToTable(summaryTable, "Total Income", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            addCellToTable(summaryTable, "Total Expense", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            addCellToTable(summaryTable, "Net Balance", FontFactory.getFont(FontFactory.HELVETICA_BOLD));

            addCellToTable(summaryTable, "₹" + totalIncome.toString(), normalFont);
            addCellToTable(summaryTable, "₹" + totalExpense.toString(), normalFont);
            addCellToTable(summaryTable, "₹" + netBalance.toString(), normalFont);

            document.add(summaryTable);

            // Transactions Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 2f, 3f, 2f, 2f });

            addCellToTable(table, "Date", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            addCellToTable(table, "Note", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            addCellToTable(table, "Category", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            addCellToTable(table, "Amount", FontFactory.getFont(FontFactory.HELVETICA_BOLD));

            for (Transaction t : transactions) {
                addCellToTable(table, t.getTransactionDate().toString(), normalFont);
                addCellToTable(table, t.getNote() != null ? t.getNote() : "", normalFont);
                addCellToTable(table, t.getCategory().getName(), normalFont);
                String amountStr = "$" + t.getAmount().toString();
                if ("EXPENSE".equals(t.getCategory().getType().name())) {
                    amountStr = "-" + amountStr;
                }
                addCellToTable(table, amountStr, normalFont);
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return baos.toByteArray();
    }

    private void addCellToTable(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        table.addCell(cell);
    }
}
