package com.inmobi.platform.elasticyarn.gateway.controller;

import static com.inmobi.platform.elasticyarn.gateway.util.WriteXmlStringToFile.convertXmlStringToFile;

import lombok.extern.slf4j.Slf4j;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import javax.servlet.http.HttpServletResponse;


@Controller
@Slf4j
public class GenerateXmlController {

  @PostMapping("/generatexml")
  public void generate(@RequestParam(name="xml", required=true) String xmlStr, @RequestParam("fileName") String fileName, HttpServletResponse response) {
    System.out.println("xml is:" + xmlStr);
    convertXmlStringToFile(xmlStr, fileName);
    uploadToBlob(fileName);
    response.setStatus(200);
  }

  public static void uploadToBlob(String fileName) {
    String storageConnectionString =
        "DefaultEndpointsProtocol=https;" +
            "AccountName=dataartifacts;" +
            "AccountKey=x2W4iTS0CYkFPuoz/x1eWBrI3siSeNU3WP9pfv4Mm9m/q30aqfk9pH4i198iWDqfLKHc0ODoLILwYSu4CdoxMg==";
    try {
      CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
      CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
      //change needs to be done here for changing location of files inside blob
      CloudBlobContainer container = blobClient.getContainerReference("ml-resources");
      container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());

      CloudBlockBlob blob = container.getBlockBlobReference("pipelineName/" + fileName + ".xml");
      String currentDir = System.getProperty("user.dir");
      blob.uploadFromFile(currentDir + "/" + fileName + ".xml");
    } catch(StorageException | URISyntaxException | IOException | InvalidKeyException e) {
      System.out.println("error is: " + e.getMessage());
    }

  }
}
