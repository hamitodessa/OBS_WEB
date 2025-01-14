
package com.hamit.obs.dto.forum;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Commit {
    private Long commitID;
    private String commitText;
    private String createdBy;
    private Timestamp createdAt;
    private Long subjectID;
}
