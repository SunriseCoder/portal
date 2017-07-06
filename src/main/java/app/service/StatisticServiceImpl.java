package app.service;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.dao.StatisticLogRepository;
import app.entity.StatisticLogEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.security.SecurityService;
import app.util.StringUtils;

@Component
public class StatisticServiceImpl implements StatisticService {
    private static final Logger logger = LogManager.getLogger(StatisticServiceImpl.class.getName());

    @Autowired
    private AuditService auditService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private UserService userService;

    @Autowired
    private StatisticLogRepository statisticLogRepository;

    @Override
    @Transactional
    public void log(String url) {
        StatisticLogEntity entity = createEntity(url);
        try {
            statisticLogRepository.save(entity);
        } catch (Exception e) {
            String auditObject = entity.toString();
            String message = StringUtils.format("Error due to save statistic log: {0}", auditObject);
            logger.error(message, e);
            auditService.log(OperationTypes.SAVE_STATISTIC_LOG, AuditEventTypes.SAVING_ERROR, auditObject, null, e.getMessage());
        }
    }

    private StatisticLogEntity createEntity(String url) {
        StatisticLogEntity entity = new StatisticLogEntity();
        entity.setUser(userService.getLoggedInUser());
        entity.setDateTime(new Date());
        entity.setIp(securityService.getIps().get(0));
        entity.setUrl(url);
        return entity;
    }
}
