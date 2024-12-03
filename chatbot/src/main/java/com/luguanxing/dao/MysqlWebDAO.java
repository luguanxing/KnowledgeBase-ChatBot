package com.luguanxing.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Repository
public class MysqlWebDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 定义时间格式
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void record(String ip, String question, String answer) {
        // 获取当前时间并转换为北京时间（UTC+8）
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

        // 格式化时间为字符串
        String formattedTime = now.format(formatter);

        // 定义 SQL 语句
        String sql = "INSERT INTO query_log (query_time, ip, question, answer) VALUES (?, ?, ?, ?)";

        // 执行插入操作
        jdbcTemplate.update(sql, formattedTime, ip, question, answer);
    }

    public List<String> getKnowledge() {
        // 定义查询语句，选择所有的 text 字段
        String sql = "SELECT text FROM knowledge";

        // 使用 queryForList 方法查询，并将结果映射为 List<String>
        return jdbcTemplate.queryForList(sql, String.class);
    }

}
