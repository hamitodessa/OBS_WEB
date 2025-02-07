package com.hamit.obs.custom.yardimci;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultSetConverter {
	public static List<Map<String, Object>> convertToList(ResultSet resultSet) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		while (resultSet.next()) {
			Map<String, Object> rowMap = new HashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				Object columnValue = resultSet.getObject(i);
				rowMap.put(columnName, columnValue);
			}
			resultList.add(rowMap);
		}
		return resultList;
	}

	public static List<Map<String, Object>> convertToListPIVOT(ResultSet resultSet) throws SQLException {
		List<Map<String, Object>> resultList = new ArrayList<>();
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		while (resultSet.next()) {
			Map<String, Object> rowMap = new LinkedHashMap<>(); 
			double toplam = 0.0;
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				Object columnValue = resultSet.getObject(i);
				rowMap.put(columnName, columnValue);
				if (columnValue instanceof Number) {
					toplam += ((Number) columnValue).doubleValue();
				}
			}
			rowMap.put("TOPLAM", toplam);
			resultList.add(rowMap);
		}
		return resultList;
	}

	public static List<Map<String, String>> parseTableData(String data, List<String> headers) {
		List<Map<String, String>> resultList = new ArrayList<>();
		String[] rows = data.split("\n"); 
		for (String row : rows) {
			String[] values = row.split("\\|\\|");
			Map<String, String> rowData = new LinkedHashMap<>();
			for (int i = 0; i < headers.size(); i++) {
				rowData.put(headers.get(i), values[i]);
			}
			resultList.add(rowData);
		}
		return resultList;
	}
}