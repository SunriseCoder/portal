package app.controller;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

@Controller
@RequestMapping("/error")
public class ErrorPageController extends BaseController implements ErrorController {
    private static final String ERROR_PAGE = "errors/error";

    @RequestMapping
    public String index(HttpServletRequest request, Model model, Principal user) {
        injectUser(model);

        int errorCode = getErrorCode(request);
        String message = getStatusMessage(errorCode);

        model.addAttribute("code", errorCode);
        model.addAttribute("message", message);

        return ERROR_PAGE;
    }

    private String getStatusMessage(int errorCode) {
        try {
            HttpStatus status = HttpStatus.valueOf(errorCode);
            return status.getReasonPhrase();
        } catch (IllegalArgumentException e) {
            return "Unknown error";
        }
    }

    private int getErrorCode(HttpServletRequest request) {
        Object attributeValue = request.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE);
        if (attributeValue == null) {
            return 0;
        }
        int errorCode = (Integer) attributeValue;
        return errorCode;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
