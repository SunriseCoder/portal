package app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import app.dao.UserRepository;
import app.entity.UserEntity;
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
    public void save(UserEntity user) {
        if (user.getId() == null) {
            encryptPass(user);
        } else {
            UserEntity storedUser = repository.getOne(user.getId());
            if (!StringUtils.safeEquals(user.getPass(), storedUser.getPass())) {
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
