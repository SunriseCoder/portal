package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/upload")
public class UploadController extends BaseController {

    @GetMapping
    public String uploadFiles(Model model) {
        injectUser(model);
        return "pages/upload/files";
    }
}
