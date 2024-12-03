package com.luguanxing.dao;

import com.luguanxing.pojo.Knowledge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.*;
import java.util.List;


@Repository
public class MysqlGptDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 插入知识到数据库
     *
     * @param text      待插入的文本
     * @param embedding 嵌入向量
     * @throws SQLException
     */
    public void insertKnowledge(String text, float[] embedding) throws SQLException {
        String insertQuery = "INSERT INTO knowledge (text, embedding) VALUES (?, ?)";
        byte[] embeddingBytes = floatArrayToByteArray(embedding);
        jdbcTemplate.update(insertQuery, text, embeddingBytes);
    }

    /**
     * 获取所有知识
     *
     * @return List of Knowledge
     * @throws SQLException
     */
    public List<Knowledge> getAllKnowledge() throws SQLException {
        String selectQuery = "SELECT text, embedding FROM knowledge";
        return jdbcTemplate.query(selectQuery, (rs, rowNum) -> {
            String text = rs.getString("text");
            byte[] embeddingBytes = rs.getBytes("embedding");
            float[] embedding = byteArrayToFloatArray(embeddingBytes);
            return new Knowledge(text, embedding);
        });
    }

    /**
     * 将float数组转换为byte数组
     *
     * @param floats float数组
     * @return byte数组
     */
    private byte[] floatArrayToByteArray(float[] floats) {
        int bytes = Float.BYTES * floats.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            byteBuffer.putFloat(f);
        }
        return byteBuffer.array();
    }

    /**
     * 将byte数组转换为float数组
     *
     * @param bytes byte数组
     * @return float数组
     */
    private float[] byteArrayToFloatArray(byte[] bytes) {
        int floats = bytes.length / Float.BYTES;
        float[] floatArray = new float[floats];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN); // 设置为小端序
        for (int i = 0; i < floats; i++) {
            floatArray[i] = byteBuffer.getFloat();
        }
        return floatArray;
    }

}