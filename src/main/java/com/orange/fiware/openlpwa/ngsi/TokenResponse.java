package com.orange.fiware.openlpwa.ngsi;

import java.util.List;

public class TokenResponse {
    private String access_token;
    private String token_type;
    private String expires_in;
    private String refresh_token;
    private List<String> scope;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }
}
