package com.hamit.obs.service.user;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hamit.obs.dto.user.UserRowDTO;
import com.hamit.obs.model.user.Role;
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
    	System.out.println("25");
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
        List<String> roles = (u.getRoles() == null) ? List.of()
                : u.getRoles().stream()
                .map(Role::getName)              // RolEnum
                .filter(r -> r != null)
                .map(Enum::name)                 // "ADMIN", "USER" vs
                .sorted()
                .collect(Collectors.toList());

        return new UserRowDTO(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getAdmin_hesap(),
                u.getCalisandvzcins(),
                roles
        );
    }
}
