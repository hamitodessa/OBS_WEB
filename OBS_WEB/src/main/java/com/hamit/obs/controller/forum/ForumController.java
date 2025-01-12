package com.hamit.obs.controller.forum;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hamit.obs.model.forum.Subjects;
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
	public List<Subjects> getSubjects() throws Exception {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		forumService.mesajsayiUpdateUser(email);
		return forumService.findAllWithCommits();
	}

	@PostMapping("addSubject")
	@ResponseBody
	public ResponseEntity<String> addSubject(@RequestBody Map<String, String> request) {
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			String title = request.get("title");
			String description = request.get("description");
			String createdBy = email;
			forumService.subjectSave(title, description, createdBy);
			return ResponseEntity.ok("Konu Eklendi");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to add subject.");
		}
	}
	@PostMapping("addComment/{subjectId}")
	@ResponseBody
	public ResponseEntity<String> addComment(@PathVariable Long subjectId, @RequestBody Map<String, String> request) {
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			String commitText = request.get("text");
			String createdBy = email;
			forumService.commitSave(subjectId, commitText, createdBy);
			return ResponseEntity.ok("Yorum Eklendi.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body("Failed to add comment.");
		}
	}
}
