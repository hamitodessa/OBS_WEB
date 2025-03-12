package com.hamit.obs.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.util.ByteArrayDataSource;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hamit.obs.custom.yardimci.Tarih_Cevir;
import com.hamit.obs.dto.kereste.cikisbilgiDTO;
import com.hamit.obs.dto.kereste.keresteDTO;
import com.hamit.obs.dto.kereste.kerestedetayDTO;
import com.hamit.obs.dto.kereste.keresteyazdirDTO;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;
import com.hamit.obs.service.kereste.KeresteService;

@Component
public class ExcellToDataSource {

	@Autowired
	private CariService cariservice;
	
	@Autowired
	private KeresteService keresteService;
	
	public ByteArrayDataSource export_excell(List<Map<String, String>> tableData) {
		ByteArrayDataSource ds = null ;
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Excell_Rapor");
			if (!tableData.isEmpty()) {
				List<String> headers = new ArrayList<>(tableData.get(0).keySet());
				List<String> rightAlignedColumns = List.of("MIKTAR", "TUTAR", "ISK. TUTAR","TOPLAM TUTAR","KDV TUTAR","AGIRLIK",
						"GIRIS MIKTARI", "GIRIS TUTARI", "CIKIS MIKTARI", "CIKIS TUTARI", "CIKIS MALIYET", "STOK MIKTARI","MALIYET",
						"GIRIS AGIRLIK",  "CIKIS AGIRLIK", "STOK AGIRLIK","ONCEKI BAKIYE", "PERY. GIRIS AGIRLIK", "PERY. CIKIS AGIRLIK",
						"PERY. STOK AGIRLIK", "BAKIYE","FIAT","MIKTAR_BAKIYE","TUTAR_BAKIYE","M3","KDV","FIAT","KUR","ISKONTO","TEVKIFAT",
						"CKDV","CFIAT","CKUR","CISKONTO","CTEVKIFAT","GIRIS M3", "CIKIS M3", "STOK M3", "ORT FIAT", "STOK TUTAR",
						"M3_ORT_FIAT");
	           
				Map<String, Double> columnSums = new HashMap<>();
				for (String col : rightAlignedColumns) {
					columnSums.put(col, 0.0);
				}
				XSSFCellStyle rightAlignStyle = workbook.createCellStyle();
				rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);
				XSSFCellStyle leftAlignStyle = workbook.createCellStyle();
				leftAlignStyle.setAlignment(HorizontalAlignment.LEFT);
				XSSFCellStyle toplamStyle = workbook.createCellStyle();
				toplamStyle.setAlignment(HorizontalAlignment.RIGHT);
				toplamStyle.setBorderTop(BorderStyle.MEDIUM);
				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < headers.size(); i++) {
					Cell headerCell = headerRow.createCell(i);
					headerCell.setCellValue(headers.get(i));
					for (String header : headers) {
						for (String column : rightAlignedColumns) {
							if (header.contains(column)) { 
								headerCell.setCellStyle(rightAlignStyle);
								break;
							}
							else {
								headerCell.setCellStyle(leftAlignStyle);
								break;
							}
						}
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
						if (rightAlignedColumns.contains(columnName) || columnName.endsWith("_TUTAR") ||
                                columnName.startsWith("M3_ORT_FIAT_")) {
							cell.setCellStyle(rightAlignStyle);
							try {
								if (cellValue == null || cellValue.toString().trim().isEmpty())
									cellValue = "0";
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
				for (int i = 0; i < headers.size(); i++)
					sheet.autoSizeColumn(i);
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
					if (i > sabitkolon)
						headerCell.setCellStyle(rightAlignStyle);
				}
				int rowNum = 1;
				for (Map<String, String> row : tableData) {
					Row excelRow = sheet.createRow(rowNum);
					for (int i = 0; i < headers.size(); i++) {
						String columnName = headers.get(i);
						Cell cell = excelRow.createCell(i);
						String cellValue = row.get(columnName) == null ? "" : row.get(columnName).trim();
						cell.setCellValue(cellValue);
						if (i >= sabitkolon)
							cell.setCellStyle(rightAlignStyle);
						if(tableData.size() == rowNum)
							cell.setCellStyle(toplamStyle);
					}
					rowNum++;
				}
				for (int i = 0; i < headers.size(); i++)
					sheet.autoSizeColumn(i);
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

	@SuppressWarnings({ "resource", "unused" })
	public ByteArrayDataSource export_excell_kercikis(keresteyazdirDTO keresteyazdirDTO) {
		ByteArrayDataSource ds = null ;
		keresteDTO dto = keresteyazdirDTO.getKeresteDTO();
		List<kerestedetayDTO> tableData = keresteyazdirDTO.getTableData();
		cikisbilgiDTO cikisbilgiDTO = keresteyazdirDTO.getCikisbilgiDTO();
		
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Evrak_" + dto.getFisno());
			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex()); 
			XSSFCellStyle headerStyle = workbook.createCellStyle();
			XSSFCellStyle headerSolaStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.RIGHT);

			XSSFFont solaFont = workbook.createFont();
			solaFont.setFontName("Arial Narrow");
			solaFont. setFontHeight((short)(10*20));
			XSSFCellStyle solaStyle = workbook.createCellStyle();
			solaStyle.setFont(solaFont);
			solaStyle.setAlignment(HorizontalAlignment.LEFT);

			XSSFFont headerSolaFont = workbook.createFont();
			headerSolaFont.setBold(true);
			headerSolaFont.setColor(IndexedColors.BLUE.getIndex()); 
			headerSolaStyle.setFont(headerSolaFont);
			headerSolaStyle.setAlignment(HorizontalAlignment.LEFT);

			XSSFCellStyle satirStyle = workbook.createCellStyle();
			XSSFCellStyle satirStylemik = workbook.createCellStyle();
			XSSFCellStyle satirStyle3 = workbook.createCellStyle();
			XSSFCellStyle satirStyle2 = workbook.createCellStyle();
			XSSFFont satirFont = workbook.createFont();
			satirFont.setFontName("Arial Narrow");
			satirFont. setFontHeight((short)(10*20));
			satirStyle.setFont(satirFont);
			satirStyle.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle3.setFont(satirFont);
			satirStyle2.setFont(satirFont);
			satirStylemik.setFont(satirFont);
			satirStyle3.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.000"));
			satirStyle2.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.00"));
			satirStylemik.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0"));
			satirStyle3.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle2.setAlignment(HorizontalAlignment.RIGHT);
			satirStylemik.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle acikStyle = workbook.createCellStyle();
			XSSFFont acikFont = workbook.createFont();
			acikFont.setColor(IndexedColors.RED.getIndex()); 
			acikFont.setBold(true);
			acikFont.setFontName("Arial");
			acikFont. setFontHeight((short)(22*20));
			acikStyle.setFont(acikFont);
			acikStyle.setAlignment(HorizontalAlignment.CENTER);

			XSSFCellStyle satirStyle2_ARA = workbook.createCellStyle();
			satirStyle2_ARA.setFont(satirFont);
			satirStyle2_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.00"));
			satirStyle2_ARA.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle2_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStyle2_ARA.setBorderBottom(BorderStyle.MEDIUM);
			XSSFCellStyle satirStyle3_ARA = workbook.createCellStyle();
			satirStyle3_ARA.setFont(satirFont);
			satirStyle3_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.000"));
			satirStyle3_ARA.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle3_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStyle3_ARA.setBorderBottom(BorderStyle.MEDIUM);
			XSSFCellStyle satirStylemik_ARA = workbook.createCellStyle();
			satirStylemik_ARA.setFont(satirFont);
			satirStylemik_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0"));
			satirStylemik_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStylemik_ARA.setBorderBottom(BorderStyle.MEDIUM);
			satirStylemik_ARA.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle satirStyleBASLIK = workbook.createCellStyle();
			satirStyleBASLIK.setFont(satirFont);
			satirStyleBASLIK.setBorderTop(BorderStyle.MEDIUM);
			satirStyleBASLIK.setBorderBottom(BorderStyle.MEDIUM);
			satirStyleBASLIK.setAlignment(HorizontalAlignment.LEFT);
			XSSFCellStyle satirStyleBASLIK2 = workbook.createCellStyle();
			satirStyleBASLIK2.setFont(satirFont);
			satirStyleBASLIK2.setBorderTop(BorderStyle.MEDIUM);
			satirStyleBASLIK2.setBorderBottom(BorderStyle.MEDIUM);
			satirStyleBASLIK2.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle satirStyleTOPTUT = workbook.createCellStyle();
			satirStyleTOPTUT.setFont(satirFont);
			satirStyleTOPTUT.setBorderTop(BorderStyle.MEDIUM);
			satirStyleTOPTUT.setAlignment(HorizontalAlignment.RIGHT);

			Cell cell ;
			Row bosRow = sheet.createRow(1);

			Row satir1 = sheet.createRow(2);
			cell = satir1.createCell(0);
			cell.setCellStyle(solaStyle);
			cell.setCellValue("Evrak No :");

			cell = satir1.createCell(1);
			cell.setCellStyle(solaStyle);
			cell.setCellValue(dto.getFisno());

			cell = satir1.createCell(9);
			cell.setCellValue(Tarih_Cevir.tarihTers(dto.getTarih()));
			cell.setCellStyle(satirStyle);


			Row satir2 = sheet.createRow(3);
			cell = satir2.createCell(0);
			cell.setCellStyle(solaStyle);
			cell.setCellValue("Musteri Kodu:");

			cell = satir2.createCell(1);
			cell.setCellStyle(solaStyle);
			cell.setCellValue(dto.getCarikod());

			Row satir3 = sheet.createRow(4);
			sheet.addMergedRegion(new CellRangeAddress(4,4,1,3));


			cell = satir3.createCell(1);
			cell.setCellStyle(solaStyle);
			
			String[] hesadi = cariservice.hesap_adi_oku(dto.getCarikod());
			
			cell.setCellValue(hesadi[0]);

			Row bosRow5 = sheet.createRow(5);

			Row aCIKLAMA = sheet.createRow(6);

			cell = aCIKLAMA.createCell(0);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Paket No");

			cell = aCIKLAMA.createCell(1);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Barkod");

			cell = aCIKLAMA.createCell(2);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Urun Kodu");

			cell = aCIKLAMA.createCell(3);
			cell.setCellValue("Miktar");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(4);
			cell.setCellValue("m3");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(5);
			cell.setCellValue("Paket m3");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(6);
			cell.setCellValue("Fiat");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(7);
			cell.setCellValue("Iskonto");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(8);
			cell.setCellValue("KDV");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(9);
			cell.setCellValue("Tutar");
			cell.setCellStyle(satirStyleBASLIK2);

			//******************SATIRLAR ***********************************************	
			int satir = 0 ;
			//List<kerestedetayDTO> tableData = kerestekayitDTO.getTableData();
			for (int i =0;i< tableData.size() ;i++)
			{
				if (!  tableData.get(i).getPaketno().equals("") )
				{
					Row satirRow = sheet.createRow(i+7);
					for (int s =0;s<= 10 ;s++)
					{

						if (s == 0 || s == 1 || s == 2)
						{
							cell = satirRow.createCell(s);
							if(s==0)
								cell.setCellValue(tableData.get(i).getPaketno());
							else if(s==1)
								cell.setCellValue(tableData.get(i).getBarkod());
							else if(s==2)
								cell.setCellValue(tableData.get(i).getUkodu());
							cell.setCellStyle(solaStyle); 
						}
						else if (s == 3)
						{
								cell = satirRow.createCell(s);
								cell.setCellValue(tableData.get(i).getMiktar());
								cell.setCellStyle(satirStylemik); 
						}
						else if (s == 4 || s == 5)
						{
								cell = satirRow.createCell(s);
								if(s==4)
									cell.setCellValue(tableData.get(i).getM3());
								else if(s==5) {
									if(tableData.get(i).getPakm3() == 0)
										cell.setCellValue("");
									else
										cell.setCellValue(tableData.get(i).getPakm3());
								}
								cell.setCellStyle(satirStyle3); 
						}
						else if (s == 7 || s == 8 || s == 9 ||  s == 10 )
						{
								cell = satirRow.createCell(s-1);
								cell.setCellStyle(satirStyle2);
								if(s==7)
									cell.setCellValue(tableData.get(i).getCfiat());
								else if(s==8)
									cell.setCellValue(tableData.get(i).getCiskonto());
								else if(s==9)
									cell.setCellValue(tableData.get(i).getCkdv());
								else if(s==10)
									cell.setCellValue(tableData.get(i).getCtutar());
						}
					}
					satir += 1 ;
				}
			}
			Row toplam1  = sheet.createRow(satir + 7);
			cell = toplam1.createCell(3);
			cell.setCellValue(cikisbilgiDTO.getTotalmiktar());//top miktar
			cell.setCellStyle(satirStylemik_ARA); 

			cell = toplam1.createCell(4);
			cell.setCellValue(cikisbilgiDTO.getTotalm3());//top m3
			cell.setCellStyle(satirStyle3_ARA); 

			cell = toplam1.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTotaltutar()); // top tutar
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(8);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(7);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(6);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(5);
			cell.setCellValue(cikisbilgiDTO.getPaketsayi());//paket sayi
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(2);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(1);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(0);
			cell.setCellStyle(satirStyleTOPTUT); 

			//***********************************************************
			Row toplam2  = sheet.createRow(satir + 8);
			sheet.addMergedRegion(new CellRangeAddress(satir + 8,satir + 8,6,8));
			cell = toplam2.createCell(6);
			cell.setCellValue( "Iskonto");
			cell.setCellStyle(satirStyle);

			cell = toplam2.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getIskonto());// alt  isk toplam
			cell.setCellStyle(satirStyle2);

			Row toplam3  = sheet.createRow(satir + 9);
			sheet.addMergedRegion(new CellRangeAddress(satir + 9,satir + 9,6,8));
			cell = toplam3.createCell(6);
			cell.setCellValue( "Iskonto Tutar");
			cell.setCellStyle(satirStyle);

			cell = toplam3.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getBakiye());  // alt bakiye
			cell.setCellStyle(satirStyle2);

			Row toplam4  = sheet.createRow(satir + 10);
			sheet.addMergedRegion(new CellRangeAddress(satir + 10,satir + 10,6,8));
			cell = toplam4.createCell(6);
			cell.setCellValue( "Kdv");
			cell.setCellStyle(satirStyle);

			cell = toplam4.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getKdv()); // alt kdv topl
			cell.setCellStyle(satirStyle2);

			Row toplam5  = sheet.createRow(satir + 11);
			sheet.addMergedRegion(new CellRangeAddress(satir + 11,satir + 11,6,8));
			cell = toplam5.createCell(4);
			cell.setCellValue( "Tevkifat");
			cell.setCellStyle(satirStyle);

			cell = toplam5.createCell(5);
			cell.setCellValue(dto.getTevoran());
			cell.setCellStyle(satirStyle2);

			cell = toplam5.createCell(6);
			cell.setCellValue( "Tev.Edilen KDV");
			cell.setCellStyle(satirStyle);

			cell = toplam5.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevedkdv());// alt tev edilen kdv
			cell.setCellStyle(satirStyle2);

			Row toplam6  = sheet.createRow(satir + 12);
			sheet.addMergedRegion(new CellRangeAddress(satir + 12,satir + 12,6,8));
			cell = toplam6.createCell(6);
			cell.setCellValue( "Tev.Dah.Top.Tut");
			cell.setCellStyle(satirStyle);

			cell = toplam6.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevdahtoptut());// alt tev dah top tut
			cell.setCellStyle(satirStyle2);

			Row toplam7  = sheet.createRow(satir + 13);
			sheet.addMergedRegion(new CellRangeAddress(satir + 13,satir + 13,6,8));
			cell = toplam7.createCell(6);
			cell.setCellValue( "Beyan Edilen KDV");
			cell.setCellStyle(satirStyle);

			cell = toplam7.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getBeyedikdv());// alt beyan edilen kdv
			cell.setCellStyle(satirStyle2);

			Row toplam8  = sheet.createRow(satir + 14);
			sheet.addMergedRegion(new CellRangeAddress(satir + 14,satir + 14,6,8));
			cell = toplam8.createCell(6);
			cell.setCellValue( "Tev.Har.Top.Tut");
			cell.setCellStyle(satirStyle);

			cell = toplam8.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevhartoptut());//alt tev har top tur
			cell.setCellStyle(satirStyle2_ARA); 
			//
			List<String> uniqueDataList = u_kod_ogren(tableData) ;
			int ssatir = satir + 8 ;
			for (int iterator = 0;iterator <= uniqueDataList.size()-1;iterator ++) {
				Row row = sheet.getRow(ssatir);
				if (row == null)
					row = sheet.createRow(ssatir);
				cell = row.createCell(0);
				sheet.addMergedRegion(new CellRangeAddress(ssatir,ssatir,0,1));
				cell.setCellValue(uniqueDataList.get(iterator) + " -" + keresteService.kod_adi(uniqueDataList.get(iterator)));
				cell.setCellStyle(solaStyle);
				ssatir +=1 ;
			}
			for (int i=0; i<=  11; i++)
				sheet.autoSizeColumn(i);
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
	
	@SuppressWarnings({ "resource", "unused" })
	public ByteArrayDataSource export_excell_kergiris(keresteyazdirDTO keresteyazdirDTO) {
		ByteArrayDataSource ds = null ;
		keresteDTO dto = keresteyazdirDTO.getKeresteDTO();
		List<kerestedetayDTO> tableData = keresteyazdirDTO.getTableData();
		cikisbilgiDTO cikisbilgiDTO = keresteyazdirDTO.getCikisbilgiDTO();
		
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Evrak_" + dto.getFisno());
			XSSFFont headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex()); 
			XSSFCellStyle headerStyle = workbook.createCellStyle();
			XSSFCellStyle headerSolaStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.RIGHT);

			XSSFFont solaFont = workbook.createFont();
			solaFont.setFontName("Arial Narrow");
			solaFont. setFontHeight((short)(10*20));
			XSSFCellStyle solaStyle = workbook.createCellStyle();
			solaStyle.setFont(solaFont);
			solaStyle.setAlignment(HorizontalAlignment.LEFT);

			XSSFFont headerSolaFont = workbook.createFont();
			headerSolaFont.setBold(true);
			headerSolaFont.setColor(IndexedColors.BLUE.getIndex()); 
			headerSolaStyle.setFont(headerSolaFont);
			headerSolaStyle.setAlignment(HorizontalAlignment.LEFT);

			XSSFCellStyle satirStyle = workbook.createCellStyle();
			XSSFCellStyle satirStylemik = workbook.createCellStyle();
			XSSFCellStyle satirStyle3 = workbook.createCellStyle();
			XSSFCellStyle satirStyle2 = workbook.createCellStyle();
			XSSFFont satirFont = workbook.createFont();
			satirFont.setFontName("Arial Narrow");
			satirFont. setFontHeight((short)(10*20));
			satirStyle.setFont(satirFont);
			satirStyle.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle3.setFont(satirFont);
			satirStyle2.setFont(satirFont);
			satirStylemik.setFont(satirFont);
			satirStyle3.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.000"));
			satirStyle2.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.00"));
			satirStylemik.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0"));
			satirStyle3.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle2.setAlignment(HorizontalAlignment.RIGHT);
			satirStylemik.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle acikStyle = workbook.createCellStyle();
			XSSFFont acikFont = workbook.createFont();
			acikFont.setColor(IndexedColors.RED.getIndex()); 
			acikFont.setBold(true);
			acikFont.setFontName("Arial");
			acikFont. setFontHeight((short)(22*20));
			acikStyle.setFont(acikFont);
			acikStyle.setAlignment(HorizontalAlignment.CENTER);

			XSSFCellStyle satirStyle2_ARA = workbook.createCellStyle();
			satirStyle2_ARA.setFont(satirFont);
			satirStyle2_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.00"));
			satirStyle2_ARA.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle2_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStyle2_ARA.setBorderBottom(BorderStyle.MEDIUM);
			XSSFCellStyle satirStyle3_ARA = workbook.createCellStyle();
			satirStyle3_ARA.setFont(satirFont);
			satirStyle3_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0.000"));
			satirStyle3_ARA.setAlignment(HorizontalAlignment.RIGHT);
			satirStyle3_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStyle3_ARA.setBorderBottom(BorderStyle.MEDIUM);
			XSSFCellStyle satirStylemik_ARA = workbook.createCellStyle();
			satirStylemik_ARA.setFont(satirFont);
			satirStylemik_ARA.setDataFormat( workbook.createDataFormat().getFormat("###,###,##0"));
			satirStylemik_ARA.setBorderTop(BorderStyle.MEDIUM);
			satirStylemik_ARA.setBorderBottom(BorderStyle.MEDIUM);
			satirStylemik_ARA.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle satirStyleBASLIK = workbook.createCellStyle();
			satirStyleBASLIK.setFont(satirFont);
			satirStyleBASLIK.setBorderTop(BorderStyle.MEDIUM);
			satirStyleBASLIK.setBorderBottom(BorderStyle.MEDIUM);
			satirStyleBASLIK.setAlignment(HorizontalAlignment.LEFT);
			XSSFCellStyle satirStyleBASLIK2 = workbook.createCellStyle();
			satirStyleBASLIK2.setFont(satirFont);
			satirStyleBASLIK2.setBorderTop(BorderStyle.MEDIUM);
			satirStyleBASLIK2.setBorderBottom(BorderStyle.MEDIUM);
			satirStyleBASLIK2.setAlignment(HorizontalAlignment.RIGHT);
			XSSFCellStyle satirStyleTOPTUT = workbook.createCellStyle();
			satirStyleTOPTUT.setFont(satirFont);
			satirStyleTOPTUT.setBorderTop(BorderStyle.MEDIUM);
			satirStyleTOPTUT.setAlignment(HorizontalAlignment.RIGHT);

			Cell cell ;
			Row bosRow = sheet.createRow(1);

			Row satir1 = sheet.createRow(2);
			cell = satir1.createCell(0);
			cell.setCellStyle(solaStyle);
			cell.setCellValue("Evrak No :");

			cell = satir1.createCell(1);
			cell.setCellStyle(solaStyle);
			cell.setCellValue(dto.getFisno());

			cell = satir1.createCell(9);
			cell.setCellValue(Tarih_Cevir.tarihTers(dto.getTarih()));
			cell.setCellStyle(satirStyle);


			Row satir2 = sheet.createRow(3);
			cell = satir2.createCell(0);
			cell.setCellStyle(solaStyle);
			cell.setCellValue("Musteri Kodu:");

			cell = satir2.createCell(1);
			cell.setCellStyle(solaStyle);
			cell.setCellValue(dto.getCarikod());

			Row satir3 = sheet.createRow(4);
			sheet.addMergedRegion(new CellRangeAddress(4,4,1,3));


			cell = satir3.createCell(1);
			cell.setCellStyle(solaStyle);
			
			String[] hesadi = cariservice.hesap_adi_oku(dto.getCarikod());
			
			cell.setCellValue(hesadi[0]);

			Row bosRow5 = sheet.createRow(5);

			Row aCIKLAMA = sheet.createRow(6);

			cell = aCIKLAMA.createCell(0);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Paket No");

			cell = aCIKLAMA.createCell(1);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Barkod");

			cell = aCIKLAMA.createCell(2);
			cell.setCellStyle(satirStyleBASLIK);
			cell.setCellValue("Urun Kodu");

			cell = aCIKLAMA.createCell(3);
			cell.setCellValue("Miktar");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(4);
			cell.setCellValue("m3");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(5);
			cell.setCellValue("Paket m3");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(6);
			cell.setCellValue("Fiat");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(7);
			cell.setCellValue("Iskonto");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(8);
			cell.setCellValue("KDV");
			cell.setCellStyle(satirStyleBASLIK2);

			cell = aCIKLAMA.createCell(9);
			cell.setCellValue("Tutar");
			cell.setCellStyle(satirStyleBASLIK2);

			//******************SATIRLAR ***********************************************	
			int satir = 0 ;
			//List<kerestedetayDTO> tableData = kerestekayitDTO.getTableData();
			for (int i =0;i< tableData.size() ;i++)
			{
				if (!  tableData.get(i).getPaketno().equals("") )
				{
					Row satirRow = sheet.createRow(i+7);
					for (int s =0;s<= 10 ;s++)
					{

						if (s == 0 || s == 1 || s == 2)
						{
							cell = satirRow.createCell(s);
							if(s==0)
								cell.setCellValue(tableData.get(i).getPaketno());
							else if(s==1)
								cell.setCellValue(tableData.get(i).getBarkod());
							else if(s==2)
								cell.setCellValue(tableData.get(i).getUkodu());
							cell.setCellStyle(solaStyle); 
						}
						else if (s == 3)
						{
								cell = satirRow.createCell(s);
								cell.setCellValue(tableData.get(i).getMiktar());
								cell.setCellStyle(satirStylemik); 
						}
						else if (s == 4 || s == 5)
						{
								cell = satirRow.createCell(s);
								if(s==4)
									cell.setCellValue(tableData.get(i).getM3());
								else if(s==5) {
									if(tableData.get(i).getPakm3() == 0)
										cell.setCellValue("");
									else
										cell.setCellValue(tableData.get(i).getPakm3());
								}
								cell.setCellStyle(satirStyle3); 
						}
						else if (s == 7 || s == 8 || s == 9 ||  s == 10 )
						{
								cell = satirRow.createCell(s-1);
								cell.setCellStyle(satirStyle2);
								if(s==7)
									cell.setCellValue(tableData.get(i).getFiat());
								else if(s==8)
									cell.setCellValue(tableData.get(i).getIskonto());
								else if(s==9)
									cell.setCellValue(tableData.get(i).getKdv());
								else if(s==10)
									cell.setCellValue(tableData.get(i).getTutar());
						}
					}
					satir += 1 ;
				}
			}
			Row toplam1  = sheet.createRow(satir + 7);
			cell = toplam1.createCell(3);
			cell.setCellValue(cikisbilgiDTO.getTotalmiktar());//top miktar
			cell.setCellStyle(satirStylemik_ARA); 

			cell = toplam1.createCell(4);
			cell.setCellValue(cikisbilgiDTO.getTotalm3());//top m3
			cell.setCellStyle(satirStyle3_ARA); 

			cell = toplam1.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTotaltutar()); // top tutar
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(8);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(7);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(6);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(5);
			cell.setCellValue(cikisbilgiDTO.getPaketsayi());//paket sayi
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(2);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(1);
			cell.setCellStyle(satirStyleTOPTUT); 

			cell = toplam1.createCell(0);
			cell.setCellStyle(satirStyleTOPTUT); 

			//***********************************************************
			Row toplam2  = sheet.createRow(satir + 8);
			sheet.addMergedRegion(new CellRangeAddress(satir + 8,satir + 8,6,8));
			cell = toplam2.createCell(6);
			cell.setCellValue( "Iskonto");
			cell.setCellStyle(satirStyle);

			cell = toplam2.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getIskonto());// alt  isk toplam
			cell.setCellStyle(satirStyle2);

			Row toplam3  = sheet.createRow(satir + 9);
			sheet.addMergedRegion(new CellRangeAddress(satir + 9,satir + 9,6,8));
			cell = toplam3.createCell(6);
			cell.setCellValue( "Iskonto Tutar");
			cell.setCellStyle(satirStyle);

			cell = toplam3.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getBakiye());  // alt bakiye
			cell.setCellStyle(satirStyle2);

			Row toplam4  = sheet.createRow(satir + 10);
			sheet.addMergedRegion(new CellRangeAddress(satir + 10,satir + 10,6,8));
			cell = toplam4.createCell(6);
			cell.setCellValue( "Kdv");
			cell.setCellStyle(satirStyle);

			cell = toplam4.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getKdv()); // alt kdv topl
			cell.setCellStyle(satirStyle2);

			Row toplam5  = sheet.createRow(satir + 11);
			sheet.addMergedRegion(new CellRangeAddress(satir + 11,satir + 11,6,8));
			cell = toplam5.createCell(4);
			cell.setCellValue( "Tevkifat");
			cell.setCellStyle(satirStyle);

			cell = toplam5.createCell(5);
			cell.setCellValue(dto.getTevoran());
			cell.setCellStyle(satirStyle2);

			cell = toplam5.createCell(6);
			cell.setCellValue( "Tev.Edilen KDV");
			cell.setCellStyle(satirStyle);

			cell = toplam5.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevedkdv());// alt tev edilen kdv
			cell.setCellStyle(satirStyle2);

			Row toplam6  = sheet.createRow(satir + 12);
			sheet.addMergedRegion(new CellRangeAddress(satir + 12,satir + 12,6,8));
			cell = toplam6.createCell(6);
			cell.setCellValue( "Tev.Dah.Top.Tut");
			cell.setCellStyle(satirStyle);

			cell = toplam6.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevdahtoptut());// alt tev dah top tut
			cell.setCellStyle(satirStyle2);

			Row toplam7  = sheet.createRow(satir + 13);
			sheet.addMergedRegion(new CellRangeAddress(satir + 13,satir + 13,6,8));
			cell = toplam7.createCell(6);
			cell.setCellValue( "Beyan Edilen KDV");
			cell.setCellStyle(satirStyle);

			cell = toplam7.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getBeyedikdv());// alt beyan edilen kdv
			cell.setCellStyle(satirStyle2);

			Row toplam8  = sheet.createRow(satir + 14);
			sheet.addMergedRegion(new CellRangeAddress(satir + 14,satir + 14,6,8));
			cell = toplam8.createCell(6);
			cell.setCellValue( "Tev.Har.Top.Tut");
			cell.setCellStyle(satirStyle);

			cell = toplam8.createCell(9);
			cell.setCellValue(cikisbilgiDTO.getTevhartoptut());//alt tev har top tur
			cell.setCellStyle(satirStyle2_ARA); 
			//
			List<String> uniqueDataList = u_kod_ogren(tableData) ;
			int ssatir = satir + 8 ;
			for (int iterator = 0;iterator <= uniqueDataList.size()-1;iterator ++) {
				Row row = sheet.getRow(ssatir);
				if (row == null)
					row = sheet.createRow(ssatir);
				cell = row.createCell(0);
				sheet.addMergedRegion(new CellRangeAddress(ssatir,ssatir,0,1));
				cell.setCellValue(uniqueDataList.get(iterator) + " -" + keresteService.kod_adi(uniqueDataList.get(iterator)));
				cell.setCellStyle(solaStyle);
				ssatir +=1 ;
			}
			for (int i=0; i<=  11; i++)
				sheet.autoSizeColumn(i);
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
	private List<String> u_kod_ogren(List<kerestedetayDTO> tableData)
	{
		List<String> list = new ArrayList<String>();  
		for (int i =0;i< tableData.size() ;i++)
		{
			if (! tableData.get(i).getUkodu().equals(""))
				list.add(tableData.get(i).getUkodu().toString().substring(0, 2));
		}
		List<String> uniqueDataList = list.stream().distinct().collect(Collectors.toList());
		return uniqueDataList ;
	}

}