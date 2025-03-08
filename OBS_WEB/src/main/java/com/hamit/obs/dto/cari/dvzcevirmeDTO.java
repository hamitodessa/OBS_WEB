package com.hamit.obs.dto.cari;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class dvzcevirmeDTO {

	private String format;
	String hesapKodu ;
	String startDate ;
	String endDate ;
	String dvz_tur ;
	String dvz_cins ;
	
	private int page;
	private int pageSize;
}
