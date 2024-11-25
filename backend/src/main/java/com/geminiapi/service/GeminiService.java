package com.geminiapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<String> getGeminiResponse(String userInput) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String requestUrl = apiUrl + "?key=" + apiKey;
            System.out.println("requestUrl: " + requestUrl);

            JsonNode jsonNode = objectMapper.readTree(userInput);
            String message = jsonNode.path("message").asText();

            HttpPost post = new HttpPost(requestUrl);
            post.setHeader("Content-Type", "application/json");

            String json = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", message);

            System.out.println("json: " + json);
            post.setEntity(new StringEntity(json));

            CloseableHttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                String responseBody = EntityUtils.toString(response.getEntity());

                JsonNode responseJsonNode = objectMapper.readTree(responseBody);
                StringBuilder concatenatedText = new StringBuilder();

                if (responseJsonNode.has("candidates")) {
                    for (JsonNode candidate : responseJsonNode.get("candidates")) {
                        JsonNode content = candidate.path("content");
                        if (content.has("parts")) {
                            for (JsonNode part : content.get("parts")) {
                                concatenatedText.append(part.path("text").asText()).append(" ");
                            }
                        }
                    }
                }

                JsonNode responseJson = objectMapper.createObjectNode();
                ((ObjectNode) responseJson).put("response", concatenatedText.toString().trim());

                return ResponseEntity.ok(responseJson.toString());
            } else {
                String errorMessage = String.format("{\"error\": \"Erro da API Gemini: %s\"}",
                        response.getStatusLine().getReasonPhrase());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Erro de cliente ao se comunicar com a API Gemini: " + e.getMessage() + "\"}");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao se comunicar com a API Gemini: " + e.getMessage() + "\"}");
        }
    }
}
