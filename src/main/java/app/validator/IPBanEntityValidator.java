package app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import app.entity.IPBanEntity;
import app.service.admin.IPBanService;

@Component
public class IPBanEntityValidator extends AbstractValidator {
    @Autowired
    private IPBanService ipBanService;

    @Override
    public boolean supports(Class<?> cl) {
        return IPBanEntity.class.equals(cl);
    }

    @Override
    public void validate(Object o, Errors errors) {
        IPBanEntity entity = (IPBanEntity) o;

        validateIP(entity.getIp(), errors);
        validateReason(entity.getReason(), errors);
    }

    public void validateIP(String ip, Errors errors) {
        if (ip == null || ip.trim().isEmpty()) {
            errors.rejectValue("ip", "required");
        }
        if (ip.length() > 64) {
            errors.rejectValue("ip", "ipBan.ip.size");
        }
        if (ipBanService.findByIp(ip) != null) {
            errors.rejectValue("ip", "ipBan.ip.duplication");
        }
    }

    public void validateReason(String reason, Errors errors) {
        if (reason == null || reason.trim().isEmpty()) {
            errors.rejectValue("reason", "required");
        }
        if (reason.length() > 64) {
            errors.rejectValue("reason", "ipBan.reason.size");
        }
    }
}
