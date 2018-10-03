package com.inmobi.platform.elasticyarn.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Controller
public class BabarController {

    @GetMapping("/babar")
    public String  welcome(@RequestParam(name="name", required=false) String name, Model model) {

       List<String> listOfFiles = getListOfFiles("/dashboard/babarhtmls/");
        model.addAttribute("listoffiles", listOfFiles);
        return "babar";
    }

    private List<String> getListOfFiles(String dirPath) {

        File dir = new File(dirPath);
        String[] files = dir.list();
        return  Arrays.asList(files);
    }
}
