package dev.haymon.jwtsecurity.controller;

import dev.haymon.jwtsecurity.controller.dto.UserResponse;
import dev.haymon.jwtsecurity.model.User;
import dev.haymon.jwtsecurity.service.AdminService;
import dev.haymon.jwtsecurity.service.validation.UserDeletionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;
    private final UserResponseMapper userMapper;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listOfUsers(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<User> users = service.getUsers(pageNumber, pageSize);
        Page<UserResponse> userResponses = users.map(userMapper::toDTO);
        return ResponseEntity.ok(userResponses);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
