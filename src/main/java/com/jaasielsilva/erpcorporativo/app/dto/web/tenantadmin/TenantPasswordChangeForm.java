package com.jaasielsilva.erpcorporativo.app.dto.web.tenantadmin;

import lombok.Data;

@Data
public class TenantPasswordChangeForm {

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
