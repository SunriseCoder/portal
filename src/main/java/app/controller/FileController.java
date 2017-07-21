package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/files")
public class FileController extends BaseController {

    @GetMapping
    public String files(Model model) {
        injectUser(model);
        return "pages/files/list";
    }
}
