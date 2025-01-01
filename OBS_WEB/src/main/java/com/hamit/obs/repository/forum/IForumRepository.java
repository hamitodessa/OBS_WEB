package com.hamit.obs.repository.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hamit.obs.model.forum.Comment;
@Repository
public interface IForumRepository extends JpaRepository<Comment,Long> {

}
