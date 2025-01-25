package com.hamit.obs.dto.stok;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class faturakayitDTO {
	
	private faturaDTO faturaDTO;
	private List<faturadetayDTO> tableData; 

}
