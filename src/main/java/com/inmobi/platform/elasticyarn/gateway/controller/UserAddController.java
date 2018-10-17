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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class UserAddController {

    @Autowired
    private FileStorageService fileStorageService;


    @PostMapping("/adduser")
    public String addUser(@RequestPart("user") String user,@RequestPart("group") String group, @RequestParam("key") MultipartFile key, Model model) {



        String error ="";
        String fileName = fileStorageService.storeFile(key);

//        addUserToTheDataBase(user,group,fileName);

        if (user.length()==0) {
            error = "Fields missing";
        } else {
            error = addUserAndKey(user,group,fileName);
        }


        model.addAttribute("error",error);
        return "adduser";
    }

//    private void addUserToTheDataBase(String user, String group, String fileName) {
//
//        String key = "";
//
//        try {
//            key = new String(Files.readAllBytes(Paths.get("/dashboard/uploads/" + fileName)));
//        } catch (IOException e) {
//            log.error("Could not read key file");
//        }
//
//        User userToAdd = new User();
//        userToAdd.setUserName(user);
//        userToAdd.setUserGroup(group);
//        userToAdd.setSshKey(key);
//
//        userRepository.save(userToAdd);
//    }

    private String addUserAndKey(String user,String group,String filename) {
        List<String> commandUseradd = new ArrayList<>();

        commandUseradd.add("adduser");
        commandUseradd.add("--disabled-password");
        commandUseradd.add("--gecos");
        commandUseradd.add("\"\"");
        commandUseradd.add(user);


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

        List<String> commandmkdir= new ArrayList<>();
        //sudo mkdir /home/username/.ssh


        commandmkdir.add("mkdir");
        commandmkdir.add("/home/"+ user +"/.ssh");

        commandExecutor = new SystemCommandExecutor(commandmkdir);
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

        //echo "key" >> /home/username/.ssh/authorized_keys

        List<String> commandmkfile = new ArrayList<>();

        commandmkfile.add("mv");
        commandmkfile.add("/dashboard/uploads/" + filename);
        commandmkfile.add("/home/"+user+"/.ssh/authorized_keys");

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




        //chmod 600 /home/username/.ssh/authorized_keys

        List<String> commandpermissionfile = new ArrayList<>();
        commandpermissionfile.add("chmod");
        commandpermissionfile.add("600");
        commandpermissionfile.add("/home/"+ user +"/.ssh/authorized_keys");
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


        //chmod 700 /home/username/.ssh
        List<String> commandpermissionfldr = new ArrayList<>();
        commandpermissionfldr.add("chmod");
        commandpermissionfldr.add("700");
        commandpermissionfldr.add("/home/"+ user +"/.ssh");
        commandExecutor = new SystemCommandExecutor(commandpermissionfldr);
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


        //change owner
        //chown username /home/username/.ssh/authorized_keys /home/username/.ssh
        List<String> commandownerchng = new ArrayList<>();
        commandownerchng.add("chown");
        commandownerchng.add(user);
        commandownerchng.add("/home/"+ user +"/.ssh");
        commandownerchng.add("/home/"+ user +"/.ssh/authorized_keys");
        commandExecutor = new SystemCommandExecutor(commandownerchng);
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

        //add the new user to the mentioned group
        List<String> commandaddtogroup = new ArrayList<>();
        commandaddtogroup.add("usermod");
        commandaddtogroup.add("-a");
        commandaddtogroup.add("-G");
        commandaddtogroup.add(group);
        commandaddtogroup.add(user);
        commandExecutor = new SystemCommandExecutor(commandaddtogroup);
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
