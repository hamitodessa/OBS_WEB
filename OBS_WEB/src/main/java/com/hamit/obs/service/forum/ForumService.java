package com.hamit.obs.service.forum;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.dto.forum.Subjects;
import com.hamit.obs.repository.forum.IForumRepository;

@Service
public class ForumService {

	@Autowired
	private IForumRepository forumRepository;

	public void subjectSave(String subjectTitle, String subjectDescription,String createdBy) {
		forumRepository.subjectSave(subjectTitle, subjectDescription,createdBy);
	}

	public void commitSave(Long subjectID, String commitText, String createdBy) {
		forumRepository.commitSave(subjectID, commitText, createdBy);
	}

	public List<Subjects> findAllWithCommits() throws Exception {
		return forumRepository.findAllWithCommits();
	}
	public void mesajsayiSaveUser(String username) {
		forumRepository.mesajsayiSaveUser(username);
	}

	public int getmesajsayi(String username) {
		return forumRepository.getmesajsayi(username);
	}

	public void mesajsayiUpdateUser(String username) {
		forumRepository.mesajsayiUpdateUser(username);
	}
	public void mesajsayiDeleteUser(String username) {
		forumRepository.mesajsayiDeleteUser(username);
	}
}