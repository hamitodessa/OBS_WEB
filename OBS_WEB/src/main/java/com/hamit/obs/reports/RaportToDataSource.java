package com.hamit.obs.reports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.util.ByteArrayDataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;

public class RaportToDataSource {

	public ByteArrayDataSource export_pdf(JasperPrint jp) throws Exception
	{
		jp.setLocaleCode("UTF-8");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JasperExportManager.exportReportToPdfStream(jp, baos);
		ByteArrayDataSource ds =  new ByteArrayDataSource(baos.toByteArray(), "application/pdf");
		return ds;
	}
	public ByteArrayDataSource export_xls(JasperPrint jp) throws JRException, IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setExporterInput(new SimpleExporterInput(jp));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
		exporter.exportReport();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream .toByteArray());
		ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, "application/x-any");
		return ds;
	}
	public ByteArrayDataSource export_docx(JasperPrint jp) throws IOException, JRException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRDocxExporter exporter = new JRDocxExporter();   
		exporter.setExporterInput(new SimpleExporterInput(jp));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
		exporter.exportReport();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream .toByteArray());
		ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, "application/x-any");
		return ds;
	}
	public ByteArrayDataSource export_xml(JasperPrint jp) throws IOException, JRException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXmlExporter exporter = new JRXmlExporter();   
		exporter.setExporterInput(new SimpleExporterInput(jp));
		exporter.setExporterOutput(new SimpleXmlExporterOutput(byteArrayOutputStream));
		exporter.exportReport();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream .toByteArray());
		ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, "application/x-any");
		return ds;
	}
	public ByteArrayDataSource export_html(JasperPrint jp) throws IOException, JRException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HtmlExporter exporter = new HtmlExporter();
		exporter.setExporterInput(new SimpleExporterInput(jp));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(byteArrayOutputStream));
		exporter.exportReport();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream .toByteArray());
		ByteArrayDataSource ds = new ByteArrayDataSource(inputStream, "application/x-any");
		return ds;
	}
}