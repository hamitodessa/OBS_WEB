package com.hamit.obs.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ConnectionDetails {
	private String serverIp;
	private String databaseName;
	private String username;
	private String password;
	private String hangisql;
	private boolean loglama;
	private String jdbcUrl;
	private String jdbcUrlLog;
}