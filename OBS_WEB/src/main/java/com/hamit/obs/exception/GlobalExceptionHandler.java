package com.hamit.obs.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<Map<String, String>> handleServiceException(ServiceException ex) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("error", "Bir hata oluştu: " + ex.getMessage());
		log.error("GlobalException  - user={}",
				SecurityContextHolder.getContext().getAuthentication().getName(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("error", "Bir hata oluştu: " + ex.getMessage());
		log.error("ResponseEntity  - user={}",
				SecurityContextHolder.getContext().getAuthentication().getName(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}