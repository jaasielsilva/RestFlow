package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractForm;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractListViewModel;
import com.jaasielsilva.erpcorporativo.app.dto.web.admin.contract.ContractViewModel;
import com.jaasielsilva.erpcorporativo.app.model.ContractStatus;
import com.jaasielsilva.erpcorporativo.app.usecase.web.admin.ContractUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/contratos")
@RequiredArgsConstructor
public class ContractAdminApiController {

    private final ContractUseCase contractUseCase;

    @GetMapping
    public ApiResponse<ContractListViewModel> list(
            @RequestParam(name = "tenantNome", required = false) String tenantNome,
            @RequestParam(name = "status", required = false) ContractStatus status,
            @RequestParam(name = "planoCodigo", required = false) String planoCodigo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ApiResponse.success(contractUseCase.list(tenantNome, status, planoCodigo, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ContractViewModel> getById(@PathVariable("id") Long id) {
        return ApiResponse.success(contractUseCase.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ContractViewModel> create(
            Authentication authentication,
            @Valid @RequestBody ContractForm form
    ) {
        return ApiResponse.success(contractUseCase.create(form, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContractViewModel> update(
            Authentication authentication,
            @PathVariable("id") Long id,
            @Valid @RequestBody ContractForm form
    ) {
        return ApiResponse.success(contractUseCase.update(id, form, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable("id") Long id) {
        contractUseCase.delete(id, authentication.getName());
    }
}
