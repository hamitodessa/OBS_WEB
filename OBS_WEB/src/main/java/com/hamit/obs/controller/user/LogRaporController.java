package com.hamit.obs.controller.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.custom.enums.modulTipi;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.user.LogRaporService;

@Controller
public class LogRaporController {

	@Autowired
	private LogRaporService logRaporService;

	@GetMapping("/user/lograpor")
	public String lograpor() {
		return "user/lograpor";
	}

	@PostMapping("user/loglistele")
	@ResponseBody
	public Map<String, Object> loglistele(@RequestBody Map<String, Object> requestBody) {
		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> lograpor = new ArrayList<>();
		try {
			String startDate = (String) requestBody.get("startDate");
			String endDate = (String) requestBody.get("endDate");
			String aciklama = (String) requestBody.get("aciklama");
			modulTipi modultip = modulTipi.fromDbValue((String) requestBody.get("modul"));
			int page = (int) requestBody.get("page");
			int pageSize = (int) requestBody.get("pageSize");
			Pageable pageable = PageRequest.of(page, pageSize);
			lograpor = logRaporService.lograpor(startDate,endDate,aciklama,modultip,pageable);
			response.put("success", true);
			response.put("data", (lograpor != null) ? lograpor : new ArrayList<>());
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("user/logsize")
	@ResponseBody
	public Map<String, Object> ekssize(@RequestBody Map<String, Object> params) {
		Map<String, Object> response = new HashMap<>();
		try {
			String startDate = (String) params.get("startDate");
			String endDate = (String) params.get("endDate");
			String aciklama = (String) params.get("aciklama");
			modulTipi modultip = modulTipi.fromDbValue((String) params.get("modul"));
			double logsize = logRaporService.logsize(startDate, endDate,modultip,aciklama);
			response.put("totalRecords", logsize);
			response.put("errorMessage", ""); 
		} catch (ServiceException e) {
			response.put("data", Collections.emptyList());
			response.put("errorMessage", e.getMessage()); 
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

}