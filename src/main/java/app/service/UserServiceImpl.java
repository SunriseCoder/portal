package app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import app.dao.UserLockRepository;
import app.dao.UserRepository;
import app.entity.UserEntity;
import app.entity.UserLockEntity;
import app.enums.Permissions;
import app.enums.Users;
import app.util.StringUtils;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserRepository repository;
    @Autowired
    private UserLockRepository userLockRepository;

    @Override
    public List<UserEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public UserEntity findById(Long id) {
        return repository.findOne(id);
    }

    @Override
    public UserEntity findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Override
    public UserEntity findByDisplayName(String displayName) {
        return repository.findByDisplayName(displayName);
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
    public boolean hasPermission(Permissions permission) {
        UserEntity user = getLoggedInUser();
        return user != null && user.hasPermission(permission.name());
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

    @Override
    public void encryptPass(UserEntity user) {
        String encodedPass = bCryptPasswordEncoder.encode(user.getPass());
        user.setPass(encodedPass);
        user.setConfirm(encodedPass);
    }

    @Override
    public void lockUser(Long id, String reason) {
        UserEntity currentUser = getLoggedInUser();
        UserEntity user = findById(id);

        UserLockEntity lock = new UserLockEntity();
        lock.setUser(user);
        lock.setReason(reason);
        lock.setLockedBy(currentUser);

        userLockRepository.save(lock);
    }
}
