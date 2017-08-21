package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import app.entity.StorageFileEntity;
import app.service.FileStorageService;

@Controller
@RequestMapping("/upload")
public class UploadController extends BaseController {
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public String uploadFiles(Model model) {
        injectUser(model);

        injectUploads(model);

        return "upload/files";
    }

    private void injectUploads(Model model) {
        List<StorageFileEntity> nonCompleted = fileStorageService.findAllNonCompletedUploadedByCurrentUser();
        model.addAttribute("nonCompleted", nonCompleted);

        List<StorageFileEntity> completed = fileStorageService.findAllCompletedUploadedByCurrentUser();
        model.addAttribute("completed", completed);
    }
}
