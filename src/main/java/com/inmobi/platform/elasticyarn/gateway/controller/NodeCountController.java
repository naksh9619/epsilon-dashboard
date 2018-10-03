package com.inmobi.platform.elasticyarn.gateway.controller;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@Slf4j
public class NodeCountController {

    @GetMapping("/countfetch")
    public String  welcome(@RequestParam(name="name", required=false) String name, Model model) {

        int numberOfnodes = getNumberOfnodes();
        model.addAttribute("numberOfNodes",numberOfnodes);
        log.info("Count of nodes is : {}",numberOfnodes);
        return "countfetch";
    }

    private int getNumberOfnodes() {

        Azure azureClient;

        String clientId = "b5476f31-c781-4976-be27-f5b5d8a4e6a6";
        String tenentId = "89359cf4-9e60-4099-80c4-775a0cfe27a7";
        String secretKey = "eWICFIprk824RZR0ErF7eKAvImk9czjun+4O3myLMQQ=";
        String subscriptiondId = "e83cc0bb-4e4b-47db-9e99-9c3eea7e3e9d";

        //TODO : check for socket handles leakages
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(clientId, tenentId,
                secretKey, null);
        azureClient  = Azure.authenticate(applicationTokenCredentials).withSubscription(subscriptiondId);
        VirtualMachineScaleSet vmss =  azureClient.virtualMachineScaleSets().getByResourceGroup(
                "platform-elastic-yarn-1","anand-test-dashboard-nodemanager");

        int numresource = vmss.capacity();

        return numresource;

    }


}
