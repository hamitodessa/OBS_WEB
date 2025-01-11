
package com.hamit.obs.model.forum;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subjects {

	private Long subjectID;
	private String subjectTitle;
	private String subjectDescription;
	private Timestamp createdAt;
	private String createdBy;
	private List<Commit> commits;

}