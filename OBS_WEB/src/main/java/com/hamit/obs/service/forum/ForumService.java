package com.hamit.obs.service.forum;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hamit.obs.model.forum.Commit;
import com.hamit.obs.model.forum.Subjects;
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
	
	public List<Subjects> getAllSubjects(){
		return forumRepository.getAllSubjects();
	}
	
	public List<Commit> getCommitsBySubjectID(Long subjectID){
		return forumRepository.getCommitsBySubjectID(subjectID);
	}
	
	 public List<Subjects> getAllSubjectsWithCommits() throws Exception {
	        return forumRepository.findAllWithCommits();
	    }
}
