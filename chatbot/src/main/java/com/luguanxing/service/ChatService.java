package com.luguanxing.service;

import com.luguanxing.dao.MysqlWebDAO;
import com.luguanxing.dao.RedisDAO;
import com.luguanxing.pojo.AjaxResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Log4j2
@Service
public class ChatService {

    @Autowired
    private RedisDAO redisDAO;

    @Autowired
    private GPTService gptService;

    @Autowired
    private MysqlWebDAO mysqlWebDAO;

    public AjaxResponse askQuestion(String userIp, String question) {
        // 先看ip次数是否超限
        boolean ipAvailable = redisDAO.checkAndUpdateIpCnt(userIp);
        if (!ipAvailable) {
            log.error("Ip daily limit reached, ip=" + userIp);
            return AjaxResponse.systemAnswer("今日提问已超限，请明天再来<br/>Ip daily limit reached, please try tomorrow");
        }
        // 调用http获得问题
        String answer = gptService.askQuestion(question);
        if (answer == null) {
            return AjaxResponse.systemAnswer("获取AI答案失败<br/>Get AI Answer failed");
        }
        // 使用mysql记录问答
        mysqlWebDAO.record(userIp, question, answer);
        return AjaxResponse.aiAnswer(answer);
    }

    public List<String> getKnowledge() {
        return mysqlWebDAO.getKnowledge();
    }
}
