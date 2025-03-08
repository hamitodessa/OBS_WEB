package com.hamit.obs.dto.kereste;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class keresteyazdirDTO {
	
	private keresteDTO keresteDTO;
	private List<kerestedetayDTO> tableData;
	private cikisbilgiDTO cikisbilgiDTO;

}
