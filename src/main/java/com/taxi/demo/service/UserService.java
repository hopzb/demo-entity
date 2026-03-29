package com.taxi.demo.service;

import com.taxi.demo.config.JwtService;
import com.taxi.demo.dto.RegisterUserRequest;
import com.taxi.demo.dto.UserResponseDTO;
import com.taxi.demo.dto.auth.LoginRequest;
import com.taxi.demo.dto.auth.LoginResponse;
import com.taxi.demo.entity.User;
import com.taxi.demo.repository.UserRepository;
import com.taxi.demo.util.Md5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;


    public UserResponseDTO createUser(RegisterUserRequest request) {
        return register(request);
    }

    public UserResponseDTO register(RegisterUserRequest request) {
        validateRegisterRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(Md5Util.hash(request.getPassword()))
                .role(normalizeRole(request.getRole()))
                .phone(request.getPhone())
                .build();

        return toDTO(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getPhone() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Thiếu phone hoặc password");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("Sai phone hoặc password"));

        String hashedPassword = Md5Util.hash(request.getPassword());
        if (!hashedPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Sai phone hoặc password");
        }

        String token = jwtService.generateToken(user);
        return LoginResponse.builder()
                .user(toDTO(user))
                .token(token)
                .build();
    }

    public UserResponseDTO getMe(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return toDTO(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user")));
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        return toDTO(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id = " + id)));
    }

    public UserResponseDTO updatePhone(Long id, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone không hợp lệ");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id = " + id));
        user.setPhone(phone);
        return toDTO(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy user với id = " + id);
        }
        userRepository.deleteById(id);
    }

    private void validateRegisterRequest(RegisterUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không hợp lệ");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password không được để trống");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone không được để trống");
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_PASSENGER";
        }
        return role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
    }

    private UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }
}
