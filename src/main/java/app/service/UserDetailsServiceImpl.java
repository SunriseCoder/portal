package app.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import app.entity.UserEntity;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        UserEntity userEntity = userService.findByLogin(login);

        if (userEntity == null) {
            throw new UsernameNotFoundException(login);
        }

        // TODO Replace new ArrayList with List of Roles after implementation
        User user = new User(userEntity.getLogin(), userEntity.getPass(), new ArrayList<>());
        return user;
    }
}
