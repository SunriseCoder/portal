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

import app.controller.BaseController;
import app.dto.CacheDTO;
import app.service.admin.IPBanServiceImpl;

@Controller
@RequestMapping("/admin/cache")
public class CacheController extends BaseController {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(CacheController.class.getName());

    private static final String ADMIN_CACHE_LIST = "admin/cache/list";

    @Autowired
    private IPBanServiceImpl ipBanService;

    @GetMapping
    public String cacheList(HttpServletRequest request, Model model) {
        injectUser(model);

        List<CacheDTO> cacheList = new ArrayList<>();
        addIpBanCache(cacheList);
        model.addAttribute("cacheList", cacheList);

        return ADMIN_CACHE_LIST;
    }

    @PostMapping("/ip-ban")
    public String refreshCache(HttpServletRequest request, Model model) {
        ipBanService.updateCache();
        model.addAttribute("message", "IP-Ban cache has been refreshed");
        return cacheList(request, model);
    }

    private void addIpBanCache(List<CacheDTO> cacheList) {
        CacheDTO ipBanCache = new CacheDTO();
        ipBanCache.setName("IP-Ban");
        ipBanCache.setUrl("ip-ban");
        ipBanCache.setSize(ipBanService.getSize());
        ipBanCache.setLastUpdate(ipBanService.getLastUpdate());
        cacheList.add(ipBanCache);
    }
}
