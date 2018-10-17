package com.inmobi.platform.elasticyarn.gateway.controller;

import static com.inmobi.platform.elasticyarn.gateway.util.WriteXmlStringToFile.convertXmlStringToFile;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

  @PostMapping("/generateconf")
  public static void generate(@RequestParam(name="xml", required=true) String xmlStr, @RequestParam("fileName") String fileName, HttpServletResponse response) throws ParserConfigurationException, IOException, SAXException {

    convertXmlStringToFile(xmlStr, fileName);
    StringBuilder builder = new StringBuilder();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document config = dBuilder.parse(new File("/Users/naksh.arora/Work/epsilon-dashboard/target/" + fileName + ".xml"));
    config.getDocumentElement().normalize();
    String pipeLineName = config.getElementsByTagName("pipeline.name").item(0).getFirstChild().getNodeValue();
    String startDate = config.getElementsByTagName("start").item(0).getFirstChild().getNodeValue();
    String endDate = config.getElementsByTagName("end").item(0).getFirstChild().getNodeValue();
    String queueName = config.getElementsByTagName("queue").item(0).getFirstChild().getNodeValue();
    String formatName = config.getElementsByTagName("format").item(0).getFirstChild().getNodeValue();
    String codec = config.getElementsByTagName("compressionCodec").item(0).getFirstChild().getNodeValue();
    String falconClusterName = config.getElementsByTagName("falcon-cluster").item(0).getFirstChild().getNodeValue();

    builder.append("pipeline.name: \"").append(pipeLineName).append("\"\n");
    builder.append("xml {\n");
    builder.append("schema.path: \"").append("/ml-resources/schema.xml\"\n");
    builder.append("metadata.path: \"").append("/ml-resources/metadata.xml\"\n");
    builder.append("training.path: \"").append("/ml-resources/training.xml\"\n\n");
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
        falconClusterName +": {}\n }\n}").append("\n");


    builder.append("source {").append("\n");
    addDataset(config, "source-datasets", builder, "datasets");
    addDataset(config, "source-training-datasets", builder, "training-datasets");
    builder.append("}\n");

    builder.append("target {").append("\n");
    builder.append("clusters-ext {\n").append("clusters: [").append(falconClusterName).append("]\n").append("}\n");
    addDataset(config, "target-datasets", builder, "datasets");
    addDataset(config, "target-training-datasets", builder, "training-datasets");
    builder.append("}\n");

    String confString = builder.toString();
    writeToFile(confString);

    response.setStatus(200);
  }

  private static void addDataset(Document config, String datasetName, StringBuilder builder, String tag) {
    NodeList nodeList = config.getElementsByTagName(datasetName);

    if (nodeList.getLength() > 0) {
      builder.append(tag).append(" {\n");
    }
    for (int i = 0 ; i < nodeList.getLength() ; i++) {
      Node node = nodeList.item(i);
      String name = node.getFirstChild().getAttributes().getNamedItem("name").getNodeValue();
      String from = node.getFirstChild().getAttributes().getNamedItem("from").getNodeValue();
      String to = node.getFirstChild().getAttributes().getNamedItem("to").getNodeValue();
      String frequency = node.getFirstChild().getFirstChild().getFirstChild().getNodeValue();
      builder.append(datasetName).append(": ${").append(frequency).append("} {\n");
      builder.append("from: \"").append(from).append("\"\n");
      builder.append("to: \"").append(to).append("\"\n");
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

}
