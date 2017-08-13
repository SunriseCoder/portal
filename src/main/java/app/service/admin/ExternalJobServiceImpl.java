package app.service.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import app.dto.JobInfoDTO;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;

@Component
public class ExternalJobServiceImpl implements ExternalJobService {
    private static final Logger logger = LogManager.getLogger(ExternalJobServiceImpl.class);

    public static final DateTimeFormatter FILE_NAME_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static byte[] buffer = new byte[2048];

    @Autowired
    private AuditService auditService;

    @Value("${logging.ext-jobs.folder}")
    private String extJobsLogPath;

    private JobInfoDTO currentJobInfo;
    private Process process;

    @Override
    public void start(String command) throws Exception {
        String started = LocalDateTime.now().format(DATETIME_FORMATTER);
        currentJobInfo = new JobInfoDTO(command, started);
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        process = processBuilder.start();
        logger.info("process started");
        ThreadWrapper threadWrapper = new ThreadWrapper(process, command);
        threadWrapper.start();
        logger.info("Thread started");
        process.waitFor();
        logger.info("process finished");

        currentJobInfo = null;
        process = null;
    }

    @Override
    public JobInfoDTO getСurrentJobInfo() {
        return currentJobInfo;
    }

    @Override
    public boolean killCurrentJob() {
        Process process = this.process;
        JobInfoDTO currentJobInfo = this.currentJobInfo;
        if (process != null && process.isAlive()) {
            String processName = currentJobInfo == null ? null : currentJobInfo.getCommand();
            try {
                process.destroyForcibly().waitFor();
                auditService.log(OperationTypes.CHANGE_EXTJOBS_KILL, AuditEventTypes.SUCCESSFUL, processName);
                return true;
            } catch (InterruptedException e) {
                logger.error("Error due to kill process: " + processName, e);
                auditService.log(OperationTypes.CHANGE_EXTJOBS_KILL, AuditEventTypes.IO_ERROR, processName);
            }
        }
        return false;
    }

    public class ThreadWrapper implements Runnable {
        private Process process;
        private String command;
        private Channel out;
        private Channel err;

        public ThreadWrapper(Process process, String command) {
            this.process = process;
            this.command = command;
            try {
                out = new Channel(process.getInputStream(), getOutput("out"));
                err = new Channel(process.getErrorStream(), getOutput("err"));
            } catch (Exception e) {
                // Nothing to do
            }
        }

        private FileOutputStream getOutput(String type) throws Exception {
            File folder = new File(extJobsLogPath);
            folder.mkdirs();
            String filename = LocalDateTime.now().format(FILE_NAME_DATE_FORMATTER) + "-" + command + "-" + type + ".log";
            File file = new File(folder, filename);
            return new FileOutputStream(file);
        }

        public void start() {
            Thread thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            while (process.isAlive()) {
                readFromInputStream(out);
                readFromInputStream(err);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // Nothing to do here
                }
            }
            close(out);
            close(err);
        }

        private void close(Channel channel) {
            if (channel == null) {
                return;
            }

            if (channel.is != null) {
                try {
                    channel.is.close();
                } catch (IOException e) {
                    // Nothing to do here
                }
            }

            if (channel.os != null) {
                try {
                    channel.os.close();
                } catch (IOException e) {
                    // Nothing to do here
                }
            }
        }

        private void readFromInputStream(Channel channel) {
            if (channel == null || channel.is == null || channel.os == null) {
                return;
            }

            boolean alive = true;
            while (alive) {
                try {
                    alive = channel.is.available() > 0;
                    if (alive) {
                        int read = channel.is.read(buffer);
                        channel.os.write(buffer, 0, read);
                        channel.os.flush();
                    }
                } catch (IOException e) {
                    // Nothing to do here
                }
            }
        }
    }

    private static class Channel {
        private InputStream is;
        private OutputStream os;

        public Channel(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }
    }
}
