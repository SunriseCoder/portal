package app.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import app.dto.FestivalDTO;
import app.entity.FestivalEntity;
import app.service.FestivalService;

@Controller
public class FestivalController extends BaseController {
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger(FestivalController.class.getName());

    private static final String FESTIVAL_PAGE = "festivals/list";

    @Autowired
    private FestivalService festivalService;

    @GetMapping("/festivals")
    public String festivalList(Model model) {
        injectUser(model);
        injectFestivalList(model);
        return FESTIVAL_PAGE;
    }

    private void injectFestivalList(Model model) {
        List<FestivalEntity> entityList = festivalService.findAll();
        List<FestivalDTO> dtoList = festivalService.entityToDTO(entityList);
        model.addAttribute("festivalList", dtoList);
    }
}
