package app.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.service.admin.ExternalJobService;

@Component
public class ExternalJobScheduler {
    private static final Logger logger = LogManager.getLogger(ExternalJobScheduler.class);

    @Autowired
    private ExternalJobService externalJobService;

    private List<String> dailyCommands;

    @Value("${backups.external-tasks.daily}")
    public void setDailyBackups(String value) {
        dailyCommands = new ArrayList<>();
        String[] commands = value.split(",");
        for (String command : commands) {
            if (command != null && !command.trim().isEmpty()) {
                dailyCommands.add(command);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily
    private void dailyBackup() {
        for (String command : dailyCommands) {
            try {
                logger.info("Starting daily external job: " + command);
                externalJobService.start(command);
                logger.info("Daily external job has been finished: " + command);
            } catch (Exception e) {
                logger.error("Exception during execution of command: " + command, e);
            }
        }
    }
}
