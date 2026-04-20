package com.jaasielsilva.erpcorporativo.app.controller.api.v1.admin;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.erpcorporativo.app.dto.api.ApiResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.OnboardingActivationResponse;
import com.jaasielsilva.erpcorporativo.app.dto.api.admin.OnboardingSubscriptionSummaryResponse;
import com.jaasielsilva.erpcorporativo.app.repository.onboarding.OnboardingSubscriptionRepository;
import com.jaasielsilva.erpcorporativo.app.service.shared.PublicOnboardingActivationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/onboarding")
@RequiredArgsConstructor
public class PublicOnboardingAdminApiController {

    private final PublicOnboardingActivationService publicOnboardingActivationService;
    private final OnboardingSubscriptionRepository onboardingSubscriptionRepository;

    @GetMapping("/recent")
    public ApiResponse<java.util.List<OnboardingSubscriptionSummaryResponse>> recent() {
        var items = onboardingSubscriptionRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(OnboardingSubscriptionSummaryResponse::from)
                .toList();
        return ApiResponse.success(items);
    }

    @PostMapping("/{onboardingId}/resend-credentials")
    public ApiResponse<OnboardingActivationResponse> resendCredentials(
            Authentication authentication,
            @PathVariable("onboardingId") Long onboardingId
    ) {
        var result = publicOnboardingActivationService.resendCredentials(onboardingId, authentication.getName());
        return ApiResponse.success(OnboardingActivationResponse.from(result));
    }
}
