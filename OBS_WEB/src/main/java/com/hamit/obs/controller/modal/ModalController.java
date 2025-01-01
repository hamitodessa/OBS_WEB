package com.hamit.obs.controller.modal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.cari.CariService;

@Controller
public class ModalController {

	@Autowired
	private CariService cariservice;

	@GetMapping("/modal/hsppln")
	@ResponseBody
	public List<Map<String, Object>> getModalData() {
		try {
			List<Map<String, Object>> modalData = cariservice.hp_pln();
			return (modalData != null) ? modalData : new ArrayList<>();
		} catch (ServiceException e) {
			throw e; 
		} catch (Exception e) {
			throw new ServiceException("Modal verileri alınırken bir hata oluştu.", e);
		}
	}
}