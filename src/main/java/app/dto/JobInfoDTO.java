package app.dto;

public class JobInfoDTO {
    private String command;
    private String started;

    public JobInfoDTO(String command, String started) {
        this.command = command;
        this.started = started;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }
}
