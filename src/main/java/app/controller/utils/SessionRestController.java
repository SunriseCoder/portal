package app.controller.utils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.controller.rest.BaseRestController;

@RestController
@RequestMapping("/rest/utils")
public class SessionRestController extends BaseRestController {

    @GetMapping("/session-heartbeat")
    public SimpleResult sessionHeartbeat() {
        return SimpleResult.Ok;
    }
}
