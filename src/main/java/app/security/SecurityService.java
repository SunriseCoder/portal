package app.security;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface SecurityService {
    List<String> getIps();
    String getLoggedInUsername();
    void autologin(String login, String pass);
}
