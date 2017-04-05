package app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import app.dao.UserRepository;
import app.entity.UserEntity;
import app.enums.Users;
import app.util.StringUtils;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository repository;

    @Override
    public UserEntity findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Boolean isAuthenticated() {
        boolean result = !Users.anonymousUser.name().equals(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return Boolean.valueOf(result);
    }

    @Override
    public UserEntity getLoggedInUser() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = findByLogin(name);
        return user;
    }

    @Override
    public void save(UserEntity user) {
        if (user.getId() == null) {
            // For new user encrypting password
            encryptPass(user);
        } else {
            UserEntity storedUser = repository.getOne(user.getId());
            if (!StringUtils.safeEquals(user.getPass(), storedUser.getPass())) {
                // For existing user if password was changed, updating it
                encryptPass(user);
            }
        }
        repository.saveAndFlush(user);
    }

    private void encryptPass(UserEntity user) {
        String encodedPass = bCryptPasswordEncoder.encode(user.getPass());
        user.setPass(encodedPass);
        user.setConfirm(encodedPass);
    }
}
