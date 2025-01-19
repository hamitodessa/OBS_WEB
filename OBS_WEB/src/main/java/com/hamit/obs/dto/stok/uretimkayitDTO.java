package com.hamit.obs.dto.stok;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class uretimkayitDTO {
	
	private uretimDTO uretimDTO;
	private List<uretimdetayDTO> tableData; 
}
