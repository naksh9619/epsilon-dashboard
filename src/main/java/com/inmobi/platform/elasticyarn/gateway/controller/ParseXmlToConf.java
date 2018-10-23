package com.inmobi.platform.elasticyarn.gateway.controller;

import static com.inmobi.platform.elasticyarn.gateway.util.WriteXmlStringToFile.convertXmlStringToFile;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.inmobi.platform.elasticyarn.gateway.util.SystemCommandExecutor;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class ParseXmlToConf {

  private static String pipelineName;


  public static void uploadToBlob(String fileName) {
    String storageConnectionString =
        "DefaultEndpointsProtocol=https;" +
            "AccountName=merlinmlstorage;" +
            "AccountKey=BoPKLJlJUNAZe5seRCj1hkr8DAqNyO647BHEBuDbeiYPRDxDwTr7l5xIdntR9j8zRygIpD+NEqd64w39RkhF7w==";
    try {
      CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
      CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
      //change needs to be done here for changing location of files inside blob
      CloudBlobContainer container = blobClient.getContainerReference("batchstore");
      container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());

      CloudBlockBlob blob = container.getBlockBlobReference("ml-resources/" + pipelineName + "/" + fileName + ".xml");
      String currentDir = System.getProperty("user.dir");
      blob.uploadFromFile(currentDir + "/" + fileName + ".xml");
    } catch(StorageException | URISyntaxException | IOException | InvalidKeyException e) {
      System.out.println("error is: " + e.getMessage());
    }

  }

  @PostMapping("/generatexml")
  public void generateXml(@RequestParam(name="xml", required=true) String xmlStr, @RequestParam("fileName") String fileName, HttpServletResponse response) {

    System.out.println("xml is:" + xmlStr);
    String replaceSchemaStr = "<schemas xmlns=\"uri:merlin:schema:0.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"uri:merlin:schema:0.1\">";
    String replaceMetaDataStr = "<metadata xmlns=\"uri:merlin:schema:0.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"uri:merlin:schema:0.1\">";
    String replaceTrainingStr = "<training xmlns=\"uri:merlin:schema:0.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"uri:merlin:schema:0.1 schema-0.1.xsd\">";
    if (fileName.equals("schema")) {
      xmlStr = xmlStr.replace("<schemas>", replaceSchemaStr);
    } else if (fileName.equals("metadata")) {
      xmlStr = xmlStr.replace("<metadata>", replaceMetaDataStr);
    } else {
      xmlStr = xmlStr.replace("<training>", replaceTrainingStr);
    }
    convertXmlStringToFile(xmlStr, fileName);
    uploadToBlob(fileName);
    response.setStatus(200);
  }

  @PostMapping("/generateconf")
  public static void generateConf(@RequestParam(name="xml", required=true) String xmlStr, @RequestParam("fileName") String fileName, HttpServletResponse response) {

    convertXmlStringToFile(xmlStr, fileName);
    parseXml(fileName);
    response.setStatus(200);
  }

  private static void parseXml(String fileName) {
    try {
      StringBuilder builder = new StringBuilder();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      String currentDir = System.getProperty("user.dir");
      Document config = dBuilder.parse(new File(currentDir + "/" + fileName + ".xml"));
      config.getDocumentElement().normalize();
      pipelineName = config.getElementsByTagName("pipeline.name").item(0).getFirstChild().getNodeValue();
      String startDate = config.getElementsByTagName("start").item(0).getFirstChild().getNodeValue();
      String endDate = config.getElementsByTagName("end").item(0).getFirstChild().getNodeValue();
      String queueName = config.getElementsByTagName("queue").item(0).getFirstChild().getNodeValue();
      String formatName = config.getElementsByTagName("format").item(0).getFirstChild().getNodeValue();
      String trainingOnly = config.getElementsByTagName("trainingOnly").item(0).getFirstChild().getNodeValue();
      String codec = config.getElementsByTagName("compressionCodec").item(0).getFirstChild().getNodeValue();
      String falconClusterName = config.getElementsByTagName("falcon-cluster").item(0).getFirstChild().getNodeValue();

      builder.append("pipeline.name: \"").append(pipelineName).append("\"\n");
      builder.append("xml {\n");
      builder.append("schema.path: \"").append("/ml-resources/").append(pipelineName).append("/schema.xml\"\n");
      builder.append("metadata.path: \"").append("/ml-resources/").append(pipelineName).append("/metadata.xml\"\n");
      builder.append("training.path: \"").append("/ml-resources/").append(pipelineName).append("/training.xml\"\n");
      builder.append("}\n");
      builder.append("trainingOnly: ").append(trainingOnly).append("\n");
      builder.append("start: \"").append(startDate).append("\"\n");
      builder.append("end: \"").append(endDate).append("\"\n");
      builder.append("queue: \"").append(queueName).append("\"\n");
      builder.append("format: \"").append(formatName).append("\"\n");
      builder.append("compressionCodec: \"").append(codec).append("\"\n");

      builder.append("spark-conf {\n" +
          "  properties{\n" +
          "    file: \"/ml-resources/spark-conf.properties\"\n" +
          "  }\n" +
          "  udf {\n" +
          "    modules:com.inmobi.data.merlin.udfs.UDFModule\n" +
          "  }\n" +
          "}").append("\n");

      builder.append("falcon-cluster {\n" +
          "  clusters {\n" +
          falconClusterName + ": {}\n }\n}").append("\n");


      builder.append("source {").append("\n");
      addDataset(config, "source-datasets", builder, "datasets");
      addDataset(config, "source-training-datasets", builder, "training-datasets");
      builder.append("}\n");

      builder.append("target {").append("\n");
      builder.append("clusters-ext {\n").append("clusters: [").append(falconClusterName).append("]\n").append("}\n");
      addDatasetTarget(config, "target-datasets", builder, "datasets");
      addDatasetTarget(config, "target-training-datasets", builder, "training-datasets");
      builder.append("}\n");

      String confString = builder.toString();
      writeToFile(confString);
      String error = triggerJob(pipelineName);
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.warn("{} Exception occurred while parsing xml to conf", e.getMessage());
    }
  }

  private static void addDataset(Document config, String datasetName, StringBuilder builder, String tag) {
    NodeList nodeList = config.getElementsByTagName(datasetName);

    if (nodeList.getLength() > 0) {
      builder.append(tag).append(" {\n");
    }
    for (int i = 0 ; i < nodeList.getLength() ; i++) {
      Node node = nodeList.item(i);
      String name = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("name").getNodeValue();
      String from = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("from").getNodeValue();
      String to = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("to").getNodeValue();
      String frequency = node.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getFirstChild().getNodeValue();
      builder.append(name).append(": ${").append(frequency).append("} {\n");
      builder.append("from: \"").append(from).append("\"\n");
      builder.append("to: \"").append(to).append("\"\n");
      builder.append("}\n");
    }
    if (nodeList.getLength() > 0) {
      builder.append(" }").append("\n");
    }
  }

  private static void addDatasetTarget(Document config, String datasetName, StringBuilder builder, String tag) {
    NodeList nodeList = config.getElementsByTagName(datasetName);

    if (nodeList.getLength() > 0) {
      builder.append(tag).append(" {\n");
    }
    for (int i = 0 ; i < nodeList.getLength() ; i++) {
      Node node = nodeList.item(i);
      String name = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("name").getNodeValue();
      String cluster = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("cluster").getNodeValue();
      String datasetNameSuffix = node.getFirstChild().getNextSibling().getAttributes().getNamedItem("dataset-name.suffix").getNodeValue();
      String pathPrefix = nodeList.item(0).getFirstChild().getNextSibling().getAttributes().getNamedItem("path.prefix").getNodeValue();
      String frequency = node.getFirstChild().getNextSibling().getFirstChild().getNextSibling().getFirstChild().getNodeValue();
      builder.append(name).append(": {\n");
      builder.append("create: ${").append(frequency).append("} {\n");
      builder.append("clusters: [").append(cluster).append("]\n");
      builder.append("path.prefix: \"").append(pathPrefix).append("\"\n");
      builder.append("falcon-name.suffix = \"-").append(pipelineName).append("\"\n");
      builder.append("dataset-name.suffix: \"").append(datasetNameSuffix).append("\"\n");
      builder.append("}\n");
      builder.append("}\n");
    }
    if (nodeList.getLength() > 0) {
      builder.append(" }").append("\n");
    }
  }

  private static void writeToFile(String text) {

    try {
      FileWriter writer = new FileWriter(new File("pipeline.conf"));
      writer.write(text);
      writer.close();
    } catch (IOException e) {
      System.out.println("Exception occurred" +  e.getMessage());
    }
  }

  private static String triggerJob(String pipeline) {
    List<String> commandToExecute = new ArrayList<>();
    //falcon extension -extensionName merlin-summarization -submitAndSchedule -jobName <pipeline-name> -file pipeline.conf
    commandToExecute.add("falcon");
    commandToExecute.add("extension");
    commandToExecute.add("-extensionName");
    commandToExecute.add("merlin");
    commandToExecute.add("-submitAndSchedule");
    commandToExecute.add("-jobName");
    commandToExecute.add(pipeline);
    commandToExecute.add("-file");
    commandToExecute.add("pipeline.conf");
    System.out.println("Executing command "+commandToExecute);
    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commandToExecute);
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
