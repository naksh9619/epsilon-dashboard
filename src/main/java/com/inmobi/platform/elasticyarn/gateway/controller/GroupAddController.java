package com.inmobi.platform.elasticyarn.gateway.controller;



import com.inmobi.platform.elasticyarn.gateway.service.FileStorageService;
import com.inmobi.platform.elasticyarn.gateway.util.SystemCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class GroupAddController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/addgroup")
    public String addGroup(@RequestPart("group") String group, @RequestParam("perm") MultipartFile perm, Model model) {

        String error ="";
        String fileName = fileStorageService.storeFile(perm);

        if (group.length()==0) {
            error = "Fields missing";
        } else {
            error = addGroupAndPermission(group,fileName);
        }

        model.addAttribute("error",error);
        return "addgroup";
    }

    private String addGroupAndPermission(String group,String filename) {
        List<String> commandUseradd = new ArrayList<>();

        commandUseradd.add("adduser");
        commandUseradd.add("--disabled-password");
        commandUseradd.add("--gecos");
        commandUseradd.add("\"\"");
        commandUseradd.add(group);


        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commandUseradd);
        try {
            int result = commandExecutor.executeCommand();
            if (result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException | InterruptedException e) {
            return "Error Happened";
        }

        log.info(commandExecutor.getStandardErrorFromCommand().toString());
        log.info(commandExecutor.getStandardOutputFromCommand().toString());

        //groupadd groupname


        List<String> commandGroupAdd = new ArrayList<>();

        commandUseradd.add("groupadd");
        commandUseradd.add(group);

        commandExecutor = new SystemCommandExecutor(commandUseradd);
        try {
            int result = commandExecutor.executeCommand();
            if (result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException | InterruptedException e) {
            return "Error Happened";
        }

        log.info(commandExecutor.getStandardErrorFromCommand().toString());
        log.info(commandExecutor.getStandardOutputFromCommand().toString());



        // mv /dashboard/uploads/filename /etc/sudoers.d/

        List<String> commandmkfile = new ArrayList<>();

        commandmkfile.add("mv");
        commandmkfile.add("/dashboard/uploads/" + filename);
        commandmkfile.add("/etc/" + "sudoers.d/");

        commandExecutor = new SystemCommandExecutor(commandmkfile);
        try {
            int result = commandExecutor.executeCommand();
            if (result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException | InterruptedException e) {
            return "Error Happened";
        }
        log.info(commandExecutor.getStandardErrorFromCommand().toString());
        log.info(commandExecutor.getStandardOutputFromCommand().toString());




        //chmod 440 /etc/sudoers.d/filename

        List<String> commandpermissionfile = new ArrayList<>();
        commandpermissionfile.add("chmod");
        commandpermissionfile.add("440");
        commandpermissionfile.add("/etc/sudoers.d/"+filename);
        commandExecutor = new SystemCommandExecutor(commandpermissionfile);
        try {
            int result = commandExecutor.executeCommand();
            if (result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException | InterruptedException e) {
            return "Error Happened";
        }
        log.info(commandExecutor.getStandardErrorFromCommand().toString());
        log.info(commandExecutor.getStandardOutputFromCommand().toString());

        return "Executed";
    }
}
