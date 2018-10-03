package com.inmobi.platform.elasticyarn.gateway.controller;
import com.inmobi.platform.elasticyarn.gateway.service.FileStorageService;
import com.inmobi.platform.elasticyarn.gateway.service.UploadFileResponse;
import com.inmobi.platform.elasticyarn.gateway.util.SystemCommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SparkController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/submitsparkjar")
    public String submitsparkjar(@RequestPart("params") String appParams,@RequestPart("params") String params, @RequestParam("file") MultipartFile file, Model model) {
        String [] paramList = params.split(" ");
        String [] appParamsList = appParams.split(" ");
        String fileName = fileStorageService.storeFile(file);
        String error = submitJar(fileName,paramList,appParamsList);
        model.addAttribute("error",error);
        return "submitsparkjar";
    }

    private String submitJar(String fileName,String [] paramList,String [] appParamsList) {

        List<String> command = new ArrayList<String>();
        command.add("/usr/local/spark/bin/spark-submit");
        //Spark arguments
        for (String param : paramList){
            command.add(param);
        }
        command.add("/dashboard/uploads/" + fileName);
        //application arguments
        for (String param : appParamsList){
            command.add(param);
        }
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
            if(result == -99) {
                return "UnidentifiedCommand";
            }
        } catch (IOException e) {
            return "Error Happened";
        } catch (InterruptedException e) {
            return "Error Happened";
        }
        return commandExecutor.getStandardErrorFromCommand().toString();
    }
}

