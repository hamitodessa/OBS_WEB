package com.hamit.obs.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hamit.obs.model.user.User_Shortcut;

public interface UserShortcutRepository extends JpaRepository<User_Shortcut, Long> {

    Optional<User_Shortcut> findByUserIdAndActionCode(Long userId, String actionCode);

    List<User_Shortcut> findAllByUserId(Long userId);
}
