package app.service;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import app.dao.UserConfirmRepository;
import app.dao.UserLockRepository;
import app.dao.UserRepository;
import app.entity.UserConfirmEntity;
import app.entity.UserEntity;
import app.entity.UserLockEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.enums.Permissions;
import app.enums.Users;
import app.util.StringUtils;

@Component
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class.getName());

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserRepository repository;
    @Autowired
    private UserLockRepository userLockRepository;
    @Autowired
    private UserConfirmRepository userConfirmRepository;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String name = authentication.getName();
        UserEntity user = findByLogin(name);
        return user;
    }

    @Override
    public boolean hasPermission(Permissions permission) {
        UserEntity user = getLoggedInUser();
        return user != null && !user.isLocked() && user.hasPermission(permission.name());
    }

    @Override
    public UserEntity save(UserEntity user) {
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
        return repository.saveAndFlush(user);
    }

    @Override
    public void encryptPass(UserEntity user) {
        String encodedPass = bCryptPasswordEncoder.encode(user.getPass());
        user.setPass(encodedPass);
        user.setConfirm(encodedPass);
    }

    @Override
    @Transactional
    public void confirmUser(Long id, String comment) {
        UserConfirmEntity confirmation = userConfirmRepository.findByUserId(id);
        if (confirmation != null) {
            auditService.log(OperationTypes.CHANGE_USER_CONFIRM, AuditEventTypes.INVALID_STATE,
                    null, confirmation.toString(), "User is already confirmed");
            return;
        }

        UserEntity currentUser = getLoggedInUser();
        UserEntity user = findById(id);

        confirmation = new UserConfirmEntity();
        confirmation.setUser(user);
        if (comment != null && !comment.trim().isEmpty()) {
            confirmation.setComment(comment);
        }
        confirmation.setConfirmedBy(currentUser);

        try {
            confirmation = userConfirmRepository.save(confirmation);
            auditService.log(OperationTypes.CHANGE_USER_CONFIRM, AuditEventTypes.SUCCESSFUL, null, confirmation.toString());
        } catch (Exception e) {
            logger.error("Error due to save user confirmation", e);
            auditService.log(OperationTypes.CHANGE_USER_CONFIRM, AuditEventTypes.SAVING_ERROR,
                    null, confirmation.toString(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void unconfirmUser(Long id) {
        UserConfirmEntity confirmation = userConfirmRepository.findByUserId(id);
        if (confirmation == null) {
            auditService.log(OperationTypes.CHANGE_USER_UNCONFIRM, AuditEventTypes.INVALID_STATE, null, null, "User is not confirmed");
            return;
        }

        try {
            userConfirmRepository.delete(confirmation);
            auditService.log(OperationTypes.CHANGE_USER_UNCONFIRM, AuditEventTypes.SUCCESSFUL, null, confirmation.toString());
        } catch (Exception e) {
            logger.error("Error due to delete user confirmation", e);
            auditService.log(OperationTypes.CHANGE_USER_UNCONFIRM, AuditEventTypes.SAVING_ERROR,
                    null, confirmation.toString(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void lockUser(Long id, String reason) {
        UserLockEntity lock = userLockRepository.findByUserId(id);
        if (lock != null) {
            auditService.log(OperationTypes.CHANGE_USER_LOCK, AuditEventTypes.INVALID_STATE, null, lock.toString(), "User is already locked");
            return;
        }

        UserEntity currentUser = getLoggedInUser();
        UserEntity user = findById(id);

        lock = new UserLockEntity();
        lock.setUser(user);
        lock.setReason(reason);
        lock.setLockedBy(currentUser);

        try {
            lock = userLockRepository.save(lock);
            auditService.log(OperationTypes.CHANGE_USER_LOCK, AuditEventTypes.SUCCESSFUL, null, lock.toString());
        } catch (Exception e) {
            logger.error("Error due to save user locking", e);
            auditService.log(OperationTypes.CHANGE_USER_LOCK, AuditEventTypes.SAVING_ERROR, null, lock.toString(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void unlockUser(Long id) {
        UserLockEntity lock = userLockRepository.findByUserId(id);
        if (lock == null) {
            auditService.log(OperationTypes.CHANGE_USER_UNLOCK, AuditEventTypes.INVALID_STATE, null, null, "User is not locked");
            return;
        }

        try {
            userLockRepository.delete(lock);
            auditService.log(OperationTypes.CHANGE_USER_UNLOCK, AuditEventTypes.SUCCESSFUL, null, lock.toString());
        } catch (Exception e) {
            logger.error("Error due to delete user lock", e);
            auditService.log(OperationTypes.CHANGE_USER_UNCONFIRM, AuditEventTypes.SAVING_ERROR, null, lock.toString(), e.getMessage());
        }
    }
}
