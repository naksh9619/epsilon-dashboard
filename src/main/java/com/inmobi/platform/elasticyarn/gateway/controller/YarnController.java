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
public class YarnController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/submityarnjar")
    public String submityarnjar(@RequestPart("params") String params, @RequestParam("file") MultipartFile file, Model model) {
        String [] paramList = params.split(" ");
        String fileName = fileStorageService.storeFile(file);
        String error = submitJar(fileName,paramList);
        model.addAttribute("error",error);
        return "submityarnjar";
    }

    private String submitJar(String fileName,String [] paramList) {
        List<String> command = new ArrayList<>();
        command.add("/usr/local/hadoop/bin/yarn");
        command.add("jar");
        command.add("/dashboard/uploads/" + fileName);

        for (String param : paramList){
            command.add(param);
        }
        for(String a : command) {
            log.info(a);
        }
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
            if (result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException | InterruptedException e) {
            return "Error Happened";
        }
        return "Executed";
    }
}
