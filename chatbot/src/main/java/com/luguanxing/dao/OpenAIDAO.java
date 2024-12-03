package com.luguanxing.dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luguanxing.pojo.ChatMessage;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Repository
public class OpenAIDAO {

    @Value("${gpt.api.key}")
    private String apiKey;

    @Value("${gpt.api.url}")
    private String baseURL;

    private OkHttpClient client;
    private Gson gson;

    @PostConstruct
    public void init() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * 创建嵌入向量
     *
     * @param input List of input strings
     * @param model Embedding模型名称
     * @return List of embeddings
     * @throws IOException
     */
    public List<Double> createEmbedding(List<String> input, String model) throws IOException {
        String url = baseURL + "/embeddings";

        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        JsonArray inputArray = new JsonArray();
        for (String text : input) {
            inputArray.add(text);
        }
        json.add("input", inputArray);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray data = jsonResponse.getAsJsonArray("data");
            if (data.size() == 0) {
                throw new IOException("No embedding data found");
            }

            JsonObject embeddingData = data.get(0).getAsJsonObject();
            JsonArray embeddingArray = embeddingData.getAsJsonArray("embedding");

            // 将JsonArray转换为List<Float>
            List<Double> embedding = gson.fromJson(embeddingArray, List.class);
            return embedding;
        }
    }

    /**
     * 生成GPT回复
     *
     * @param messages    消息列表
     * @param model       GPT模型名称
     * @param temperature 生成温度
     * @return GPT回复内容
     * @throws IOException
     */
    public String createChatCompletion(List<ChatMessage> messages, String model, double temperature) throws IOException {
        String url = baseURL + "/chat/completions";

        JsonObject json = new JsonObject();
        json.addProperty("model", model);

        JsonArray messagesArray = new JsonArray();
        for (ChatMessage msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.getRole());
            msgObj.addProperty("content", msg.getContent());
            messagesArray.add(msgObj);
        }
        json.add("messages", messagesArray);
        json.addProperty("temperature", temperature);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() == 0) {
                throw new IOException("No choices found in the response");
            }

            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            return message.get("content").getAsString();
        }
    }
}