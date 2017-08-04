package app.validator;

import java.beans.PropertyDescriptor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class AbstractValidator implements Validator {
    private static final Logger logger = LogManager.getLogger(UserEntityValidator.class.getName());

    public void clearValue(Errors errors, String property) {
        if (!(errors instanceof BeanPropertyBindingResult)) {
            return;
        }

        BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) errors;
        Object target = bindingResult.getTarget();
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(target.getClass(), property);
        try {
            pd.getWriteMethod().invoke(target, "");
        } catch (Exception e) {
            logger.error("Error due to clear bean value", e);
        }
    }
}
