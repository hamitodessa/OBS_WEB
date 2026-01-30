package com.hamit.obs.service.user;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hamit.obs.dto.user.UserRowDTO;
import com.hamit.obs.model.user.User;
import com.hamit.obs.repository.user.UserRepository;

@Service
public class UserListService {
	private final UserRepository userRepository;

    public UserListService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserRowDTO> listUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toRowDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) return;
        userRepository.deleteById(id);
    }
    
    
    private UserRowDTO toRowDTO(User u) {
        return new UserRowDTO(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName()
        );
    }
}
