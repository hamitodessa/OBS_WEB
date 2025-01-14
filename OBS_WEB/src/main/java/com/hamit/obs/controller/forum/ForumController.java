package com.hamit.obs.controller.forum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.dto.forum.Subjects;
import com.hamit.obs.exception.ServiceException;
import com.hamit.obs.service.forum.ForumService;

@Controller
public class ForumController {

	@Autowired
	private ForumService forumService;


	@GetMapping("/forum")
	public String mizan() {
		return "forum/forum";
	}

	@GetMapping("/getSubjects")
	@ResponseBody
	public Map<String, Object> getSubjects()  {
		Map<String, Object> response = new HashMap<>();
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			forumService.mesajsayiUpdateUser(email);
			List<Subjects> listsubject =  forumService.findAllWithCommits();
			response.put("success", true);
			response.put("subjects", listsubject);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("subjects", Collections.emptyList());
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}

	@PostMapping("addSubject")
	@ResponseBody
	public Map<String, Object> addSubject(@RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			String title = request.get("title");
			String description = request.get("description");
			String createdBy = email;
			forumService.subjectSave(title, description, createdBy);
			response.put("success", true);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("success", false);
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
	
	@PostMapping("addComment/{subjectId}")
	@ResponseBody
	public Map<String, Object> addComment(@PathVariable Long subjectId, @RequestBody Map<String, String> request) {
		Map<String, Object> response = new HashMap<>();
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			String commitText = request.get("text");
			String createdBy = email;
			forumService.commitSave(subjectId, commitText, createdBy);
			response.put("success", true);
			response.put("errorMessage", "");
		} catch (ServiceException e) {
			response.put("success", false);
			response.put("errorMessage", e.getMessage());
		} catch (Exception e) {
			response.put("success", false);
			response.put("errorMessage", "Hata: " + e.getMessage());
		}
		return response;
	}
}
