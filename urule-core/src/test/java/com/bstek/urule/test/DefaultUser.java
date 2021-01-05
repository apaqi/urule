package com.bstek.urule.test;

/**
 *
 * @author wpx
 * @Description 类注释
 * @date 2021/1/5
 */
public class DefaultUser{
    private String username;
    private String companyId;
    private boolean isAdmin;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getCompanyId() {
        return companyId;
    }
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public boolean isAdmin() {
        return isAdmin;
    }
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
