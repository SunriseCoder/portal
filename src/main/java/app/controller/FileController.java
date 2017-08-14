package app.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.service.FileStorageService;

@Controller
@RequestMapping("/files")
public class FileController extends BaseController {
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String files(Model model) {
        injectUser(model);
        return "files/list";
    }

    //TODO cut off
    @GetMapping("create")
    public String createFiles(Model model, RedirectAttributes redirectAttributes) throws IOException {
        for (int i = 0; i < 1000; i++) {
            fileStorageService.createFilePlaceHolder("eh", 5);
        }
        redirectAttributes.addFlashAttribute("message", "Files were created");
        return "redirect:/files";
    }
}
