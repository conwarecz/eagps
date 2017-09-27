package net.aineuron.eagps.model.database;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 26.09.2017.
 */

public class LoginData {
    private Long userId;
    private String token;
    private Long role;
    private String userName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getRole() {
        return role;
    }

    public void setRole(Long role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
