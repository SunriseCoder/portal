package app.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        List<GrantedAuthority> grantedAuthorities = userEntity.getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p)).collect(Collectors.toList());
        User user = new User(userEntity.getLogin(), userEntity.getPass(), grantedAuthorities);
        return user;
    }
}
