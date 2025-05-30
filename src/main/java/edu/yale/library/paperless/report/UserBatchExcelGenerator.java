package edu.yale.library.paperless.report;

import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskStatus;
import edu.yale.library.paperless.entities.UserTaskBatch;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import static edu.yale.library.paperless.services.StringHelper.isBlank;

public class UserBatchExcelGenerator {

    public InputStream userBatchToExcel(UserTaskBatch userTaskBatch) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Task Batch");
        sheet.createFreezePane(0, 1);
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(6, 6000);
        sheet.setColumnWidth(7, 6000);

        Row header = sheet.createRow(0);

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        headerStyle.setFont(font);

        int ix = 0;

        Cell headerCell = header.createCell(ix++);
        headerCell.setCellValue("Title");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Call Number");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Enum / Chron / Year");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Barcode");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Patron Name");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Patron Email");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Patron Barcode");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Pickup Location");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Incoming Status");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Set Status");
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(ix++);
        headerCell.setCellValue("Problems");
        headerCell.setCellStyle(headerStyle);

        CellStyle style = workbook.createCellStyle();
        //style.setWrapText(true);

        for (int i = 0; i< userTaskBatch.getTasks().size(); i++) {
            Task cs = userTaskBatch.getTasks().get(i);
            cs.setIncomingStatus(cs.getStatus().equals(TaskStatus.NOS) || cs.getStatus().equals(TaskStatus.FOS) || cs.getStatus().equals(TaskStatus.New) ? TaskStatus.New : TaskStatus.NOS);
            Row row = sheet.createRow(i + 1);
            ix = 0;

            Cell cell = row.createCell(ix++);
            cell.setCellValue(cs.getTitle());
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(cs.getCallNumber());
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(isBlank(cs.getEnumeration())?"none":cs.getEnumeration());
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(cs.getItemBarcode() + (cs.getOriginalItemBarcode()!=null?" (original: " + cs.getOriginalItemBarcode() + ")":""));
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            String patronName = cs.getRequester();
            cell.setCellValue(patronName);
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            String patronEmail = cs.getPatronEmail();
            cell.setCellValue(patronEmail);
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            String patronBarcode = cs.getPatronBarcode();
            cell.setCellValue(patronBarcode);
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(cs.getPickupLocationDisplay()!=null?cs.getPickupLocationDisplay():"unknown");
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(""+cs.getIncomingStatus());
            cell.setCellStyle(style);

            cell = row.createCell(ix++);
            cell.setCellValue(""+cs.getStatus());
            cell.setCellStyle(style);

            String problems = "n/a";
            if ( cs.getTaskFillProblems() != null && cs.getTaskFillProblems().size() > 0 ) {
                problems = cs.getTaskFillProblems().stream().map((p)->p.getName()).collect(Collectors.joining(", "));
            }

            cell = row.createCell(ix++);
            cell.setCellValue(problems);
            cell.setCellStyle(style);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
