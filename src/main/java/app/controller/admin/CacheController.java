package app.controller.admin;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.controller.BaseController;
import app.dto.CacheDTO;
import app.service.FileService;
import app.service.admin.IPBanServiceImpl;

@Controller
@RequestMapping("/admin/cache")
public class CacheController extends BaseController {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(CacheController.class.getName());

    private static final String ADMIN_CACHE_LIST = "admin/cache/list";
    private static final String REDIRECT_CACHE_LIST = "redirect:/admin/cache";

    @Autowired
    private FileService fileService;
    @Autowired
    private IPBanServiceImpl ipBanService;

    @GetMapping
    public String cacheList(HttpServletRequest request, Model model) {
        injectUser(model);

        List<CacheDTO> cacheList = new ArrayList<>();
        addIpBanCache(cacheList);
        addFileListCache(cacheList);
        model.addAttribute("cacheList", cacheList);

        return ADMIN_CACHE_LIST;
    }

    @PostMapping("/ip-ban")
    public String refreshIPBanCache(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ipBanService.updateCache();
        redirectAttributes.addFlashAttribute("message", "IP-Ban cache has been refreshed");
        return REDIRECT_CACHE_LIST;
    }

    @PostMapping("/file-list")
    public String refreshFileListCache(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        fileService.forceRescan();
        redirectAttributes.addFlashAttribute("message", "FileList cache has been refreshed");
        return REDIRECT_CACHE_LIST;
    }

    private void addIpBanCache(List<CacheDTO> cacheList) {
        CacheDTO ipBanCache = new CacheDTO();
        ipBanCache.setName("IP-Ban");
        ipBanCache.setUrl("ip-ban");
        ipBanCache.setSize(ipBanService.getSize());
        ipBanCache.setLastUpdate(ipBanService.getLastUpdate());
        cacheList.add(ipBanCache);
    }

    private void addFileListCache(List<CacheDTO> cacheList) {
        CacheDTO fileListCache = new CacheDTO();
        fileListCache.setName("FileList");
        fileListCache.setUrl("file-list");
        fileListCache.setSize(fileService.getFileListSize());
        fileListCache.setLastUpdate(fileService.getLastUpdate());
        cacheList.add(fileListCache);
    }
}
