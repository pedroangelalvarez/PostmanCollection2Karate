package com.aim;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {

    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Por favor, proporciona el nombre del archivo de colección de Postman como argumento.");
            return;
        }
        System.out.println(args[0]);
        String postmanCollectionFile = args[0];
        try {
            JSONParser parser = new JSONParser();
            JSONObject postmanCollection = (JSONObject) parser.parse(new FileReader(postmanCollectionFile));
            JSONArray items = (JSONArray) postmanCollection.get("item");

            String karateFeatureFile = postmanCollectionFile.replace(".json", ".feature");
            FileWriter writer = new FileWriter(karateFeatureFile);

            for (Object item : items) {
                JSONObject requestItem = (JSONObject) item;
                String name = requestItem.get("name").toString();
                JSONArray arrayItems = (JSONArray) requestItem.get("item");
                requestItem = (JSONObject) arrayItems.get(0);
                JSONObject request = (JSONObject) requestItem.get("request");
                String method = (String) request.get("method");
                JSONObject urlInfo = (JSONObject) request.get("url");
                String protocol = (String) urlInfo.get("protocol");
                JSONArray hostArray = (JSONArray) urlInfo.get("host");
                String host = String.join(".", hostArray);
                JSONArray pathArray = (JSONArray) urlInfo.get("path");
                String path = String.join("/", pathArray);
                String rawUrl = (String) urlInfo.get("raw");
                writer.write("Feature: " + name + "\n");
                writer.write("Scenario: " + (String) requestItem.get("name") + "\n");
                writer.write("Given url '" + rawUrl  + "'\n");
                writer.write("When method " + method + "\n");

                JSONArray headers = (JSONArray) request.get("header");
                if (headers != null) {
                    headers.forEach(header -> {
                        JSONObject headerInfo = (JSONObject) header;
                        String type = (String) headerInfo.get("type");
                        String value = (String) headerInfo.get("value");
                        String key = (String) headerInfo.get("key");
                        try {
                            writer.write("And header " + key + " = '" + value + "'\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                JSONObject objectBody = (JSONObject) request.get("body");
                if (objectBody != null && objectBody instanceof JSONObject) {
                    String body = (String) objectBody.get("raw");
                    if (body != null && !body.isEmpty()) {
                        writer.write("And request ```\n" + body + "\n```\n");
                    }
                }
                writer.write("Then status 200");
                writer.write("\n\n");
            }

            writer.close();
            System.out.println("Archivo de feature de Karate generado exitosamente: " + karateFeatureFile);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
