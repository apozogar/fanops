package com.softwells.fanops.controller;

import com.softwells.fanops.controller.dto.ApiResponse;
import com.softwells.fanops.model.RoleEntity;
import com.softwells.fanops.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RolesController {

  private final RoleRepository roleRepository;

  @GetMapping
  public ResponseEntity<ApiResponse<List<RoleEntity>>> getAll() {
    return ResponseEntity.ok(new ApiResponse<>(true, "Roles recuperados", roleRepository.findAll()));
  }
}