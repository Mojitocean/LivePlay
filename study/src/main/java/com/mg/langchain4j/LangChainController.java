package com.mg.langchain4j;

import dev.langchain4j.model.chat.ChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.mg.langchain4j
 * LLM 是大脑，RAG 是眼睛（看资料），Function Calling 是手（做事情），Agent 是灵魂（会思考规划），LangChain 是骨架（搭系统），MCP 是神经（连万物），AIGC 是产出（最终内容）。
 * @author mj
 * @className LangChainController
 * @date 2025/11/10
 * @description TODO
 */
@Log4j2
@Tag(name = "LangChain学习")
@RestController
@RequestMapping("/lc")
public class LangChainController {

    @Resource
    private ChatModel chatModel;

    @Operation(summary = "LangChain模型")
    @GetMapping("/chat")
    public String model(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return chatModel.chat(message);
    }
}