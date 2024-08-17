此为伙伴匹配系统的后端代码，包含后端接口，数据库，以及后端的代码。
### 1.技术要点
* SpringBoot 框架
* MySQL数据库
* Redis缓存
* MyBatis-Plus
* Swagger + Knife4j 接口文档
* WebSocket
* Redisson 分布式锁 (用于解决加入组队的并发问题)
* GSON 解析json tags
#### 2.后端接口文档：
使用knife4j+swagger2生成接口文档，文档地址：http://localhost:8080/api/doc.html#/
![img.png](img.png)
### 关于设计问题
1.匹配伙伴：
在这里我们使用Jaccard相似度:
* 编辑距离：适合文本字符串的精确匹配。如果你的标签数据是非常短的字符串，并且你关心每个字符的匹配情况，可以选择编辑距离。
* Jaccard相似度：适合用于比较两个标签集合的相似性，是标签匹配的首选方法。
* 余弦相似度：适合用于文本向量的比较.
2.关于精准匹配
* 这里待开发，原目标使用ip地址，用归属地来匹配