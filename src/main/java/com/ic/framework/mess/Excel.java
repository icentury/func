package com.ic.framework.mess;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class Excel {

    private static final String xls = "e:\\mess\\1.xlsx";
    private static final String xls2 = "e:\\mess\\2.xlsx";

    public static void main(String[] args) {
        read(xls);
    }

    public static void read(String file) {
        try (Workbook workbook = WorkbookFactory.create(new File(xls));
             FileOutputStream fos = new FileOutputStream(xls2)) {
            Sheet sheet = workbook.getSheetAt(1);
            Row row;
            int count = sheet.getLastRowNum();
            for (int i = 1; i <= count; i++) {
                row = sheet.getRow(i);
                String ipRange = row.getCell(1).getStringCellValue();
                String ips = handlerIPRange(ipRange.replace(" ", "").replace("，", ","));
                System.out.println(ips);
                Cell cell = row.createCell(4, CellType.STRING);
                cell.setCellValue(ips);
            }
            workbook.write(fos);
        } catch (EncryptedDocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String handlerIPRange(String ipRange) {
        if (!Pattern.compile("^[0-9.\\-,/]+$").matcher(ipRange).find()) {
            return "";
        }
        String ret = "";
        if (ipRange.contains("-")) {
            String[] arr = ipRange.replace("/24", "").split("-");
            String[] start = arr[0].split("\\.");
            String[] end = arr[1].split("\\.");
            StringBuilder sb = new StringBuilder();
            if (start.length == end.length && end.length > 2) {
                if (start[2].equals(end[2])) {
                    int s = Integer.parseInt(start[3]);
                    int e = Integer.parseInt(end[3]);
                    if (s <= 1 && e == 254) {
                        sb.append(start[0]).append(".").append(start[1]).append(".").append(start[2]).append(".0").append("/24,");
                    } else {
                        for (int i = 0; i <= e - s; i++) {
                            sb.append(start[0]).append(".").append(start[1]).append(".").append(start[2]).append(".").append(s + i).append("/32,");
                        }
                    }
                } else {
                    int s = Integer.parseInt(start[2]);
                    int e = Integer.parseInt(end[2]);
                    for (int i = 0; i <= e - s; i++) {
                        sb.append(start[0]).append(".").append(start[1]).append(".").append(s + i).append(".0").append("/24,");
                    }
                }
            } else {
                int s = Integer.parseInt(start[2]);
                int e = Integer.parseInt(end[0]);
                for (int i = 0; i <= e - s; i++) {
                    sb.append(start[0]).append(".").append(start[1]).append(".").append(s + i).append(".0").append("/24,");
                }
            }
            ret = sb.toString();
        } else if (ipRange.contains(",")) {
            ret = String.join("/32,", ipRange.split(",")) + "/32";
        } else if (Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$").matcher(ipRange).find()) {
            ret = ipRange + "/32";
        } else {
            ret = ipRange;
        }
        return ret.endsWith(",") ? ret.substring(0, ret.length() - 1) : ret;
    }
}
