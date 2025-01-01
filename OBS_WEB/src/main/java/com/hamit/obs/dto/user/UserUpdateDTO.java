package com.hamit.obs.dto.user;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {
	private String firstName;
    private String lastName;
    private MultipartFile image;
}
