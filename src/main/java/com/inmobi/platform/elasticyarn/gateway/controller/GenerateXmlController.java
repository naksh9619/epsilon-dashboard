package com.inmobi.platform.elasticyarn.gateway.controller;

import lombok.extern.slf4j.Slf4j;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


@Controller
@Slf4j
public class GenerateXmlController {

  @PostMapping("/generatexml")
  public void generate(@RequestParam(name="xml", required=true) String xmlStr, @RequestParam("fileName") String fileName, HttpServletResponse response) {

    System.out.println(xmlStr);
    convertStringToFile(xmlStr, fileName);
    uploadToBlob(fileName);
    response.setStatus(200);
  }

  public static void convertStringToFile(String xmlStr, String fileName) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(new InputSource(new StringReader(xmlStr)));

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource domSource = new DOMSource(document);

      StreamResult streamResult = new StreamResult(new File(fileName + ".xml"));
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(domSource, streamResult);
    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {

    }
  }

  public static void uploadToBlob(String fileName) {
    String storageConnectionString =
        "DefaultEndpointsProtocol=https;" +
            "AccountName=dataartifacts;" +
            "AccountKey=x2W4iTS0CYkFPuoz/x1eWBrI3siSeNU3WP9pfv4Mm9m/q30aqfk9pH4i198iWDqfLKHc0ODoLILwYSu4CdoxMg==";
    try {
      CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
      CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
      CloudBlobContainer container = blobClient.getContainerReference("ml-resources");
      container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());

      CloudBlockBlob blob = container.getBlockBlobReference(fileName + ".xml");
      blob.uploadFromFile("/Users/naksh.arora/Work/epsilon-dashboard/target/" + fileName + ".xml");
    } catch(StorageException | URISyntaxException | IOException | InvalidKeyException e) {
      System.out.println("error is: " + e.getMessage());
    }

  }
}
