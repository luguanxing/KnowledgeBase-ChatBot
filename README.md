# 知识库聊天机器人 KnowledgeBase-ChatBot 

## 总览 Overview
一个基于Springboot实现仿微信的聊天窗口界面加知识库机器人 => [演示地址](https://luguanxing.com/chatbot/)

A WeChat-like chatbot with a knowledge base implemented using a Spring Boot-based chat window interface => [DemoPage](https://luguanxing.com/chatbot/)

![image](https://github.com/user-attachments/assets/48f7e617-af55-40bb-aa4b-612827a96638)

![image](https://github.com/user-attachments/assets/e73573fc-0c19-43c9-94b3-4d1d158c8014)

<br/>


## 架构和细节 Architecture and Details

首先知识库的数据存储在mysql表中

First, the knowledge base data is stored in a MySQL table.
```
CREATE DATABASE db_chatbot;

USE db_chatbot;

CREATE TABLE `knowledge` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `text` longtext NOT NULL,
  `embedding` longblob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1

```

然后是Springboot的web，整体架构不复杂，前端是聊天窗口进行交互，后端直接查询知识数据库进行匹配相似知识，然后把topK结果和prompt一起发给GPT接口整理获取答案

Then there is the Spring Boot-based web application. The overall architecture is not complex: the front end provides a chat window for interactions, while the back end directly queries the knowledge database to match similar knowledge. It then sends the top-K results along with the prompt to the GPT interface, which organizes and retrieves the final answer.




<br/>


## 总结和优化 Summary and Optimization

目前整体效果还是比较满意，一方面通过提示词成功限制了聊天范围，另一方面基于embedding的相似度匹配效果也不错

The overall effect is quite satisfactory so far. On one hand, the prompt successfully limits the scope of the conversation, and on the other hand, the embedding-based similarity matching works well.


