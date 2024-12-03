package com.luguanxing.service;

import com.luguanxing.dao.MysqlGptDAO;
import com.luguanxing.dao.OpenAIDAO;
import com.luguanxing.pojo.ChatMessage;
import com.luguanxing.pojo.Knowledge;
import com.luguanxing.pojo.RankedKnowledge;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class GPTService {

    @Autowired
    private OpenAIDAO openAIDAO;

    @Autowired
    private MysqlGptDAO mysqlGptDAO;

    @Value("${gpt.embedding_encoding}")
    private String EMBEDDING_ENCODING;

    @Value("${gpt.model}")
    private String GPT_MODEL;

    /**
     * 提问并获取回答
     *
     * @param question 用户问题
     * @return 回答
     * @throws IOException
     * @throws SQLException
     */
    public String askQuestion(String question) {
        try {
            // 获取相关知识，假设返回前10个
            List<RankedKnowledge> relatedKnowledge = getTopRelated(question, 10);

            // 构建信息段落
            String infoParagraph = relatedKnowledge.stream()
                    .map(RankedKnowledge::getText)
                    .collect(Collectors.joining(" "));

            // 构建介绍部分
            String introduction = "Use the following information to answer the question about you(Luguanxing's AI assistant), " +
                    "Luguanxing and his website comprehensively. " +
                    "If a user greets you, respond politely and simply, and guide them to ask questions related to Luguanxing. " +
                    "如果用户问\"他\"相关的问题，用Luguanxing的知识回答。 " +
                    "如果用户问你是谁，礼貌地回复用户你是Luguanxing的小助手，并引导用户问Luguanxing相关的问题。 " +
                    "If the user asks you in pure Chinese, you should respond in Chinese, but keep Luguanxing's name in English. " +
                    "If the English answer cannot be found, write \"I could not find a relevant answer, please ask something else about Luguanxing.\"." +
                    "If the Chinese answer cannot be found, write \"找不到相关答案，请问其它关于Luguanxing的问题\"." +
                    "===============================================\n\n" +
                    infoParagraph +
                    "===============================================\n\n";

            // 构建问题部分
            String questionPart = "\n\n**Question:** " + question + "\n\n**Answer:**";

            // 合成完整消息
            String fullMessage = introduction + questionPart;

            // 检查token预算（此处简化处理，实际可使用token计数工具）
            // 如果需要裁剪信息内容，可以只使用前N条知识
            // 这里假设不超过预算

            // 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are Luguanxing's AI assistant who provides comprehensive and coherent information about Luguanxing based on the provided data."));
            messages.add(new ChatMessage("user", fullMessage));

            // 获取GPT回复
            String response = openAIDAO.createChatCompletion(messages, GPT_MODEL, 0.0);

            return response;
        } catch (Exception e) {
            log.error("Failed to get AI answer", e);
            return null;
        }
    }

    /**
     * 添加知识到数据库
     *
     * @param text 待添加的文本
     * @throws IOException
     * @throws SQLException
     */
    public void addKnowledge(String text) throws IOException, SQLException {
        // 获取嵌入向量
        List<Double> embeddingList = openAIDAO.createEmbedding(Collections.singletonList(text), EMBEDDING_ENCODING);
        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = embeddingList.get(i).floatValue();
        }
        // 插入数据库
        mysqlGptDAO.insertKnowledge(text, embedding);
    }


    private List<RankedKnowledge> getTopRelated(String query, int topN) throws IOException, SQLException {
        // 获取查询的嵌入向量
        List<Double> queryEmbeddingList = openAIDAO.createEmbedding(Collections.singletonList(query), EMBEDDING_ENCODING);
        float[] queryEmbedding = new float[queryEmbeddingList.size()];
        for (int i = 0; i < queryEmbeddingList.size(); i++) {
            queryEmbedding[i] = queryEmbeddingList.get(i).floatValue();
        }

        // 获取所有知识
        List<Knowledge> allKnowledge = mysqlGptDAO.getAllKnowledge();

        // 计算相似度
        List<RankedKnowledge> rankedList = allKnowledge.stream()
                .map(k -> new RankedKnowledge(k.getText(), cosineSimilarity(queryEmbedding, k.getEmbedding())))
                .sorted(Comparator.comparingDouble(RankedKnowledge::getSimilarity).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        return rankedList;
    }

    private double cosineSimilarity(float[] vecA, float[] vecB) {
        if (vecA.length != vecB.length) {
            throw new IllegalArgumentException("向量长度不一致");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vecA.length; i++) {
            dotProduct += vecA[i] * vecB[i];
            normA += Math.pow(vecA[i], 2);
            normB += Math.pow(vecB[i], 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}