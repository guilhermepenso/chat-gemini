package com.geminiapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.geminiapi.model.InformationModel;
import com.geminiapi.model.InformationRepository;
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

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final InformationRepository informationRepository;

    public GeminiService(InformationRepository informationRepository) {
        this.informationRepository = informationRepository;
    }

    public ResponseEntity<String> getGeminiResponse(String userInput) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String requestUrl = apiUrl + "?key=" + apiKey;
            System.out.println("requestUrl: " + requestUrl);

            // Parse o userInput para extrair o valor da chave "message"
            JsonNode jsonNode = objectMapper.readTree(userInput);
            String message = jsonNode.path("message").asText(); // Extrai o valor de "message"

            // Criação do HTTP POST
            HttpPost post = new HttpPost(requestUrl);
            post.setHeader("Content-Type", "application/json");

            // Monta o corpo da requisição
            String json = String.format("{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}", message);
            System.out.println("json: " + json);
            post.setEntity(new StringEntity(json));

            // Envia a requisição e captura a resposta
            CloseableHttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                // Lê a resposta da API Gemini
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode responseJsonNode = objectMapper.readTree(responseBody);

                // Concatena todos os "text" das partes
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

                String geminiResponse = concatenatedText.toString().trim();
                System.out.println("Gemini Response: " + geminiResponse);

                // Salvar no banco de dados
                InformationModel information = new InformationModel();
                information.setPerguntaUser(message);
                information.setRespostaIa(geminiResponse);
                informationRepository.save(information); // Persistindo no banco

                // Criação do JSON de resposta
                JsonNode responseJson = objectMapper.createObjectNode();
                ((ObjectNode) responseJson).put("response", geminiResponse);

                return ResponseEntity.ok(responseJson.toString());
            } else {
                // Trata erros de API
                String errorMessage = String.format("{\"error\": \"Erro da API Gemini: %s\"}",
                        response.getStatusLine().getReasonPhrase());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
            }
        } catch (IOException e) {
            // Tratamento de erros de I/O
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao se comunicar com a API Gemini: " + e.getMessage() + "\"}");
        }
    }
}
