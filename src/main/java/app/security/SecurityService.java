package app.security;

public interface SecurityService {
    String getLoggedInUsername();
    void autologin(String login, String pass);
}
