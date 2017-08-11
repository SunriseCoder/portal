package app.controller;

import java.util.Iterator;
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

import app.dto.PlaceDTO;
import app.entity.PlaceEntity;
import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.PlaceService;
import app.util.SafeUtils;
import app.validator.PlaceEntityValidator;

@Controller
@RequestMapping("/places")
public class PlaceController extends BaseController {
    private static final Logger logger = LogManager.getLogger(PlaceController.class.getName());

    private static final String PLACES_LIST = "places/list";
    private static final String PLACES_EDIT = "places/edit";

    private static final String REDIRECT_PLACES = "redirect:/places";

    @Autowired
    private PlaceService placeService;

    @Autowired
    private PlaceEntityValidator validator;

    @GetMapping
    public String placeList(Model model) {
        injectUser(model);
        injectPlaceList(model);
        return PLACES_LIST;
    }

    @GetMapping("/add")
    public String addPlace(@RequestParam(name = "parent", required = false) Long parent, Model model) {
        injectFormData(model, new PlaceDTO(), parent);
        return PLACES_EDIT;
    }

    @GetMapping("/edit")
    public String editPlace(@RequestParam("id") Long id, Model model) {
        PlaceEntity entity = placeService.findById(id);
        PlaceDTO dto = placeService.entityToDTO(entity);
        injectFormData(model, dto, null);
        return PLACES_EDIT;
    }

    @PostMapping("/save")
    public String savePlace(@ModelAttribute("place") PlaceDTO place, Model model,
                    BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        Long id = place.getId();
        PlaceEntity storedEntity = id == null ? null : placeService.findById(id);
        if (storedEntity != null && storedEntity.isSystem()) {
            Long storedParentId = storedEntity.getParent() == null ? 0 : storedEntity.getParent().getId();
            Long newParentId = place.getParent() == null ? 0 : place.getParent().getId();
            if (!SafeUtils.safeEquals(storedParentId, newParentId)) {
                bindingResult.reject("parent", "place.parent.systemPlace");
            }
        }

        validator.validate(place, bindingResult);
        if (bindingResult.hasErrors()) {
            String objectBefore = storedEntity == null ? null : storedEntity.toString();
            auditService.log(OperationTypes.CHANGE_PLACE_EDIT, AuditEventTypes.VALIDATION_ERROR,
                            objectBefore, place.toString(), bindingErrorsToString(bindingResult));
            injectFormData(model, place, null);
            return PLACES_EDIT;
        }

        try {

            placeService.save(place);
            redirectAttributes.addFlashAttribute("message", "Place has been saved successfully");
        } catch (Exception e) {
            model.addAttribute("error", "System error due to save place");
            injectFormData(model, place, null);
            return PLACES_EDIT;
        }

        injectUser(model);
        return REDIRECT_PLACES;
    }

    @PostMapping("/delete")
    public String deletePlace(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        PlaceEntity place = placeService.findById(id);
        if (place != null && place.isSystem()) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete place, because it is a system place");
            return REDIRECT_PLACES;
        }

        if (placeService.hasChildren(id)) {
            redirectAttributes.addFlashAttribute("message", "Cannot delete place, because it has children places");
            return REDIRECT_PLACES;
        }

        try {
            PlaceEntity entity = placeService.delete(id);
            auditService.log(OperationTypes.CHANGE_PLACE_DELETE, AuditEventTypes.SUCCESSFUL, entity.toString(), null);
            redirectAttributes.addFlashAttribute("message", "Place has been deleted successfully");
        } catch (Exception e) {
            String error = "System error due to delete place";
            logger.error(error, e);
            auditService.log(OperationTypes.CHANGE_PLACE_DELETE, AuditEventTypes.DELETE_ERROR, null, id.toString(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", error);
        }

        injectUser(model);
        return REDIRECT_PLACES;
    }

    private void injectPlaceList(Model model) {
        List<PlaceEntity> entityList = placeService.findAllRoot();
        List<PlaceDTO> dtoList = placeService.entityToDTO(entityList);
        model.addAttribute("placeList", dtoList);
    }

    private void injectFormData(Model model, PlaceDTO dto, Long parent) {
        injectUser(model);
        if (parent != null) {
            PlaceEntity parentEntity = placeService.findById(parent);
            if (parentEntity != null) {
                PlaceDTO parentDto = placeService.entityToDTO(parentEntity);
                dto.setParent(parentDto);
            }
        }
        model.addAttribute("place", dto);

        List<PlaceEntity> placeEntities = placeService.findAll();
        deleteEntitysBranchRecursive(placeEntities, dto);
        List<PlaceDTO> placeDtos = placeService.entityToDTO(placeEntities);
        placeDtos.sort((a, b) -> a.getPath().compareTo(b.getPath()));
        model.addAttribute("allPlaces", placeDtos);
    }

    private void deleteEntitysBranchRecursive(List<PlaceEntity> placeEntities, PlaceDTO branchToExclude) {
        Iterator<PlaceEntity> iterator = placeEntities.iterator();
        while (iterator.hasNext()) {
            PlaceEntity entity = iterator.next();
            if (validator.isInBranch(entity, branchToExclude.getId())) {
                iterator.remove();
            }
        }

        placeEntities.stream()
                        .filter(entity -> entity.getChildren().size() > 0)
                        .forEach(entity -> deleteEntitysBranchRecursive(entity.getChildren(), branchToExclude));
    }
}
