package com.hamit.obs.custom.yardimci;

import java.text.DecimalFormat;

public class Formatlama {

	public static String doub_0(double rakkam)
	{
		DecimalFormat decimalFormat = new DecimalFormat();//"###,###,##0"
		decimalFormat.setMinimumFractionDigits(0);
		decimalFormat.setMaximumFractionDigits(0);
		String numberAsString = decimalFormat.format(rakkam);
		return numberAsString; 
	}
	public static String doub_2(double rakkam)
	{
		DecimalFormat decimalFormat = new DecimalFormat();//"###,###,##0.00"
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(2);
		String numberAsString = decimalFormat.format(rakkam);
		return numberAsString; 
	}
	public static String doub_3(double rakkam)
	{
		DecimalFormat decimalFormat = new DecimalFormat();//"###,###,##0.000"
		decimalFormat.setMinimumFractionDigits(3);
		decimalFormat.setMaximumFractionDigits(3);
		String numberAsString = decimalFormat.format(rakkam);
		return numberAsString; 
	}
	public static String doub_4(double rakkam)
	{
		DecimalFormat decimalFormat = new DecimalFormat();//"###,###,##0.0000"
		decimalFormat.setMinimumFractionDigits(4);
		decimalFormat.setMaximumFractionDigits(4);
		String numberAsString = decimalFormat.format(rakkam);
		return numberAsString; 
	}
}
