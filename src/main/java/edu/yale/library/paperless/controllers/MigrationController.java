package edu.yale.library.paperless.controllers;

import edu.yale.library.paperless.entities.Barcode;
import edu.yale.library.paperless.entities.Task;
import edu.yale.library.paperless.entities.TaskFillProblem;
import edu.yale.library.paperless.repositories.BarcodeRepository;
import edu.yale.library.paperless.repositories.TaskRepository;
import edu.yale.library.paperless.services.UnauthorizedRequestException;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MigrationController {
    private final BarcodeRepository barcodeRepository;
    private final TaskRepository taskRepository;

    @RolesAllowed("ADMIN")
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/migrate-barcodes")
    public String migrateBarcodes() throws UnauthorizedRequestException {
        int cnt = 0;
        for (Task task : taskRepository.findAll()) {
            List<String> barcodes = new ArrayList<>(barcodeRepository.findByTask(task).stream().map(Barcode::getBarcode).toList());
            cnt += checkAndAdd(task, task.getItemBarcode(), barcodes);
            cnt += checkAndAdd(task, task.getOriginalItemBarcode(), barcodes);
        }
        return "Added " + cnt + " barcodes.";
    }

    @RolesAllowed("ADMIN")
    @RequestMapping(method = RequestMethod.GET, value = "/admin-actions")
    public String adminActions() {
        return "admin-actions.html";
//        return """

    }

    private int checkAndAdd(Task task, String itemBarcode, List<String> barcodes) {
        if (StringUtils.hasText(itemBarcode) && !barcodes.contains(itemBarcode)) {
            Barcode b = new Barcode();
            b.setTask(task);
            b.setBarcode(itemBarcode);
            barcodes.add(itemBarcode);
            barcodeRepository.save(b);
            return 1;
        } else {
            return 0;
        }
    }
}
