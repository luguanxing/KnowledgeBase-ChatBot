package com.luguanxing.controller;

import com.luguanxing.pojo.AjaxRequest;
import com.luguanxing.pojo.AjaxResponse;
import com.luguanxing.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/askQuestion")
    public AjaxResponse askQuestion(HttpServletRequest request, @RequestBody AjaxRequest ajaxRequest) {
        String userIp = getClientIp(request);
        String question = ajaxRequest.getQuestion();
        return chatService.askQuestion(userIp, question);
    }

    @GetMapping("/getKnowledge")
    public List<String> getKnowledge() {
        return chatService.getKnowledge();
    }

    // 获取客户端真实IP的方法
    private String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            header = request.getHeader("Proxy-Client-IP");
        }
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            header = request.getHeader("WL-Proxy-Client-IP");
        }
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            header = request.getHeader("HTTP_CLIENT_IP");
        }
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            header = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (header == null || header.isEmpty() || "unknown".equalsIgnoreCase(header)) {
            header = request.getRemoteAddr();
        }
        // 如果有多个IP地址，取第一个
        return header.split(",")[0];
    }

}
