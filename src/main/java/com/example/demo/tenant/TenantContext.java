package com.example.demo.tenant;

public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_ENTERPRISE = new ThreadLocal<>();

    public static void setEnterpriseId(Long enterpriseId) {
        CURRENT_ENTERPRISE.set(enterpriseId);
    }

    public static Long getEnterpriseId() {
        return CURRENT_ENTERPRISE.get();
    }

    public static void clear() {
        CURRENT_ENTERPRISE.remove();
    }
}
