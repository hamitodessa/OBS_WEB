package com.hamit.obs.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.util.ByteArrayDataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hamit.obs.exception.ServiceException;

public class ExcellToDataSource {

	public ByteArrayDataSource export_excell(List<Map<String, String>> tableData) {
		ByteArrayDataSource ds = null ;
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Excell_Rapor");
			if (!tableData.isEmpty()) {
				List<String> headers = new ArrayList<>(tableData.get(0).keySet());
				List<String> rightAlignedColumns = List.of("MIKTAR", "TUTAR", "ISK. TUTAR","TOPLAM TUTAR","KDV TUTAR","AGIRLIK",
						"GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI","MALIYET",
						"GIRIS AGIRLIK",  "CIKIS AGIRLIK", "STOK AGIRLIK","ONCEKI BAKIYE", "PERY. GIRIS AGIRLIK", "PERY. CIKIS AGIRLIK",
						"PERY. STOK AGIRLIK", "BAKIYE");
				Map<String, Double> columnSums = new HashMap<>();
				for (String col : rightAlignedColumns) {
					columnSums.put(col, 0.0);
				}
				XSSFCellStyle rightAlignStyle = workbook.createCellStyle();
				rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);

				XSSFCellStyle toplamStyle = workbook.createCellStyle();
				toplamStyle.setAlignment(HorizontalAlignment.RIGHT);
				toplamStyle.setBorderTop(BorderStyle.MEDIUM);

				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < headers.size(); i++) {
					Cell headerCell = headerRow.createCell(i);
					headerCell.setCellValue(headers.get(i));
					if (rightAlignedColumns.contains(headers.get(i))) {
						headerCell.setCellStyle(rightAlignStyle);
					}
				}
				int rowNum = 1;
				for (Map<String, String> row : tableData) {
					Row excelRow = sheet.createRow(rowNum);
					for (int i = 0; i < headers.size(); i++) {
						String columnName = headers.get(i);
						Cell cell = excelRow.createCell(i);
						String cellValue = row.get(columnName) == null ? "" : row.get(columnName).trim();
						cell.setCellValue(cellValue);
						if (rightAlignedColumns.contains(columnName)) {
							cell.setCellStyle(rightAlignStyle);
							try {
								if (cellValue == null || cellValue.toString().trim().isEmpty()) {
									cellValue = "0";
								}
								NumberFormat formatter = NumberFormat.getInstance();
								Number number = formatter.parse(cellValue.toString());
								double value = number.doubleValue();
								columnSums.put(columnName, columnSums.getOrDefault(columnName, 0.0) + value);
							} catch (Exception e) {
								throw new ServiceException(e.getMessage());
							}
						}
					}
					rowNum++;
				}
				/*				
				Row totalRow = sheet.createRow(rowNum++);
				for (int i = 0; i < headers.size(); i++) {
					String columnName = headers.get(i);
					Cell cell = totalRow.createCell(i);
					if (columnSums.containsKey(columnName)) {
						if(columnName.equals("MIKTAR") || columnName.equals("AGIRLIK"))
						{
							cell.setCellValue(Formatlama.doub_3(columnSums.get(columnName)));
						}
						else {
							cell.setCellValue(Formatlama.doub_2(columnSums.get(columnName)));
						}
						cell.setCellStyle(toplamStyle);
					} else {
						cell.setCellValue("");
					}
				}
				 */
				for (int i = 0; i < headers.size(); i++) {
					sheet.autoSizeColumn(i);
				}
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			byte[] byteArray = bos.toByteArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			ds = new ByteArrayDataSource(in, "application/x-any");
			bos.close();
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
		return ds;
	}

	public ByteArrayDataSource export_excell_grp(List<Map<String, String>> tableData,int sabitkolon) {
		ByteArrayDataSource ds = null ;
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Excell_Rapor");
			if (!tableData.isEmpty()) {
				List<String> headers = new ArrayList<>(tableData.get(0).keySet());
				XSSFCellStyle rightAlignStyle = workbook.createCellStyle();
				rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);
				XSSFCellStyle toplamStyle = workbook.createCellStyle();
				toplamStyle.setAlignment(HorizontalAlignment.RIGHT);
				toplamStyle.setBorderTop(BorderStyle.MEDIUM);
				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < headers.size(); i++) {
					Cell headerCell = headerRow.createCell(i);
					headerCell.setCellValue(headers.get(i));
					if (i > sabitkolon) {
						headerCell.setCellStyle(rightAlignStyle);
					}
				}
				int rowNum = 1;
				for (Map<String, String> row : tableData) {
					Row excelRow = sheet.createRow(rowNum);
					for (int i = 0; i < headers.size(); i++) {
						String columnName = headers.get(i);
						Cell cell = excelRow.createCell(i);
						String cellValue = row.get(columnName) == null ? "" : row.get(columnName).trim();
						cell.setCellValue(cellValue);
						if (i > sabitkolon) {
							cell.setCellStyle(rightAlignStyle);
						}
						if(tableData.size() == rowNum) {
							cell.setCellStyle(toplamStyle);
						}
					}
					rowNum++;
				}
				for (int i = 0; i < headers.size(); i++) {
					sheet.autoSizeColumn(i);
				}
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			byte[] byteArray = bos.toByteArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			ds = new ByteArrayDataSource(in, "application/x-any");
			bos.close();
		} catch (Exception ex) {
			throw new ServiceException(ex.getMessage());
		}
		return ds;
	}
}
