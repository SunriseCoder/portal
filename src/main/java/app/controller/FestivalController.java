package app.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.dto.FestivalDTO;
import app.entity.FestivalEntity;
import app.entity.PlaceEntity;
import app.entity.UserEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.FestivalService;
import app.service.PlaceService;
import app.validator.FestivalEntityValidator;

@Controller
@RequestMapping("/festivals")
public class FestivalController extends BaseController {
    private static final Logger logger = LogManager.getLogger(FestivalController.class.getName());

    private static final String FESTIVAL_LIST = "pages/festivals/list";
    private static final String FESTIVAL_EDIT = "pages/festivals/edit";

    private static final String REDIRECT_FESTIVALS = "redirect:/festivals";

    @Autowired
    private FestivalService festivalService;
    @Autowired
    private PlaceService placeService;

    @Autowired
    private FestivalEntityValidator validator;

    @GetMapping
    public String festivalList(Model model) {
        injectUser(model);
        injectFestivalList(model);
        return FESTIVAL_LIST;
    }

    @GetMapping("/create")
    public String createFestival(Model model) {
        injectFormData(model, new FestivalEntity(), "create");
        return FESTIVAL_EDIT;
    }

    @PostMapping("/create")
    public String saveNewFestival(@ModelAttribute("festEntity") FestivalEntity festEntity, Model model,
                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        String saveUrl = "create";

        validator.validate(festEntity, bindingResult);
        if (bindingResult.hasErrors()) {
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        PlaceEntity place = null;
        try {
            place = getOrCreatePlaceEntity(festEntity);
        } catch (Exception e) {
            model.addAttribute("error", "System error due to add festival place");
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        festEntity.setPlace(place);
        UserEntity user = userService.getLoggedInUser();
        festEntity.setAddedBy(user);

        try {
            festEntity = festivalService.save(festEntity);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_ADD, AuditEventTypes.SUCCESSFUL, null, festEntity.toString());
            redirectAttributes.addFlashAttribute("message", "Festival has been saved successfully");
        } catch (Exception e) {
            String error = "System error due to add festival";
            logger.error(error, e);
            model.addAttribute("error", error);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_ADD, AuditEventTypes.SAVING_ERROR, null, festEntity.toString(), e.getMessage());
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        injectUser(model);
        return REDIRECT_FESTIVALS;
    }

    @GetMapping("/edit")
    public String editFestival(@RequestParam("id") Long id, Model model) {
        FestivalEntity entity = festivalService.findById(id);
        injectFormData(model, entity, "edit");
        return FESTIVAL_EDIT;
    }

    @PostMapping("/edit")
    public String saveExistingFestival(@ModelAttribute("festEntity") FestivalEntity festEntity, Model model,
                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        String saveUrl = "edit";

        if (festEntity.getId() == null) {
            logger.warn("Trying to save existing entity with empty ID: {}", festEntity.toString());
            auditService.log(OperationTypes.CHANGE_FESTIVAL_EDIT, AuditEventTypes.SUSPICIOUS_ACTIVITY,
                            festEntity.toString(), null, "Saving existing festival with empty ID");
            return REDIRECT_FESTIVALS;
        }

        FestivalEntity storedFestEntity = festivalService.findById(festEntity.getId());
        if (storedFestEntity == null) {
            logger.warn("Trying to save non-existing entity with ID: {}", festEntity.getId());
            auditService.log(OperationTypes.CHANGE_FESTIVAL_EDIT, AuditEventTypes.SUSPICIOUS_ACTIVITY,
                            festEntity.getId().toString(), null, "Saving non-existing festival");
            return REDIRECT_FESTIVALS;
        }

        validator.validate(festEntity, bindingResult);
        if (bindingResult.hasErrors()) {
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        PlaceEntity place = null;
        try {
            place = getOrCreatePlaceEntity(festEntity);
        } catch (Exception e) {
            model.addAttribute("error", "System error due to add festival place");
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        String objectBefore = storedFestEntity.toString();
        storedFestEntity.setStart(festEntity.getStart());
        storedFestEntity.setEnd(festEntity.getEnd());
        storedFestEntity.setDetails(festEntity.getDetails());
        storedFestEntity.setPlace(place);

        try {
            storedFestEntity = festivalService.save(storedFestEntity);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_EDIT, AuditEventTypes.SUCCESSFUL, objectBefore, storedFestEntity.toString());
            redirectAttributes.addFlashAttribute("message", "Festival has been saved successfully");
        } catch (Exception e) {
            String error = "System error due to save festival";
            logger.error(error, e);
            model.addAttribute("error", error);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_EDIT, AuditEventTypes.SAVING_ERROR, objectBefore, storedFestEntity.toString(), e.getMessage());
            injectFormData(model, festEntity, saveUrl);
            return FESTIVAL_EDIT;
        }

        injectUser(model);
        return REDIRECT_FESTIVALS;
    }

    @PostMapping("/delete")
    public String deleteFestival(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            FestivalEntity entity = festivalService.delete(id);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_DELETE, AuditEventTypes.SUCCESSFUL, entity.toString(), null);
            redirectAttributes.addFlashAttribute("message", "Festival has been deleted successfully");
        } catch (Exception e) {
            String error = "System error due to delete festival";
            logger.error(error, e);
            auditService.log(OperationTypes.CHANGE_FESTIVAL_DELETE, AuditEventTypes.DELETE_ERROR, null, id.toString(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", error);
        }

        injectUser(model);
        return REDIRECT_FESTIVALS;
    }

    private void injectFestivalList(Model model) {
        List<FestivalEntity> entityList = festivalService.findAll();
        List<FestivalDTO> dtoList = festivalService.entityToDTO(entityList);
        model.addAttribute("festivalList", dtoList);
    }

    private void injectFormData(Model model, FestivalEntity entity, String saveUrl) {
        injectUser(model);
        model.addAttribute("festEntity", entity);
        List<PlaceEntity> places = placeService.findAll();
        model.addAttribute("allPlaces", places);
        model.addAttribute("saveUrl", saveUrl);
    }

    private PlaceEntity getOrCreatePlaceEntity(FestivalEntity festEntity) {
        PlaceEntity place = null;
        if (festEntity.getPlace().getId().longValue() == PlaceService.PLACE_ELEMENT_OTHER) {
            place = new PlaceEntity();
            PlaceEntity other = placeService.findById(PlaceService.PLACE_ELEMENT_OTHER);
            place.setParent(other);
            place.setName(festEntity.getPlace().getName());
            try {
                place = placeService.save(place);
            } catch (Exception e) {
                logger.error("System error due to add festival place" , e);
                auditService.log(OperationTypes.CHANGE_PLACE_ADD, AuditEventTypes.SAVING_ERROR, null, place.toString(), e.getMessage());
                throw e;
            }
        } else {
            place = placeService.findById(festEntity.getPlace().getId());
        }
        return place;
    }
}
