package com.hamit.obs.dto.user;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class User_DetailsDTO {
	private Long id;
    private String user_modul;
    private String user_prog_kodu;
    private String user_server;
    private String user_ip;
    private boolean izinlimi;
    private boolean calisanmi;
    private boolean log;
 	private String email;
	private String user_pwd_server;
	private String hangi_sql;
	private String superviser;
}