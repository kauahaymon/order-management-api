package dev.haymon.ordermanagement.service;

import dev.haymon.ordermanagement.application.dto.user.UserUpdateRequest;
import dev.haymon.ordermanagement.domain.model.User;
import dev.haymon.ordermanagement.domain.repository.RoleRepository;
import dev.haymon.ordermanagement.domain.repository.UserRepository;
import dev.haymon.ordermanagement.service.validation.UserDeletionValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserDeletionValidator deletionValidator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public Page<User> getUsers(Integer pageNumber, Integer pageSize) {
        Pageable userPage = PageRequest.of(pageNumber, pageSize);
        return userRepository.findAll(userPage);
    }

    public void deleteUser(Integer id) {
        deletionValidator.validate(id);
        userRepository.deleteById(id);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public void updateUser(Integer id, UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (user.getPassword() != null && !request.getPassword().isEmpty()) {
            String encodedPassword = encoder.encode(request.getPassword());
            user.setPassword(encodedPassword);
        }
        user.setFirstName(request.getFirstname());
        user.setLastName(request.getLastname());
        user.setEmail(request.getEmail());
        user.setAccountLocked(request.getAccountLocked());
        user.setEnabled(request.getEnabled());

        request.getRoles().forEach(roleName -> {
                   var role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role '" + roleName + "' não permitido(a). Usuário só pode ser ADMIN ou USER"));
                   user.addRole(role);
                }
        );
        removeUnusedRoles(request, user);

        userRepository.save(user);
    }

    private static void removeUnusedRoles(UserUpdateRequest request, User user) {
        user.getRoles().forEach(role -> {
            if (!request.getRoles().contains(role.getName())) {
                user.removeRole(role);
            }
        });
    }
}
