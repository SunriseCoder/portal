package app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.UserRepository;
import app.entity.UserEntity;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository repository;

    @Override
    public UserEntity findByLogin(String login) {
        return repository.findByLogin(login);
    }
}
