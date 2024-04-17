package com.suibe.suibe_mma.controller;

import com.suibe.suibe_mma.domain.processedResponse;
import com.suibe.suibe_mma.domain.request.sentenceRequest;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.suibe.suibe_mma.util.ControllerUtil.requestFail;

@RestController
@RequestMapping("/sentence")
public class SentenceController {

    /**
     * 中文分词处理
     * @param sendSentenceRequest 原始语句请求类
     * @return 分词结果
     */
    @PostMapping("/send")
    public processedResponse sendSentence(
            @RequestBody sentenceRequest sendSentenceRequest) {

        processedResponse prodResponse = new processedResponse();
        try {
            requestFail(sendSentenceRequest);
            String sentenceContent = sendSentenceRequest.getSentenceContent();
            Result temp = ToAnalysis.parse(sentenceContent);
            String analysisedTextWithTags = temp.toString();
            String analysisedText = temp.toStringWithOutNature();
            String[] strArrayWithTags = analysisedTextWithTags.split(",");
            String[] strArray = analysisedText.split(",");
            String[] Tags = new String[strArrayWithTags.length];
            for(int idx = 0; idx < strArrayWithTags.length; idx++)
                Tags[idx] = strArrayWithTags[idx].substring(strArrayWithTags[idx].indexOf('/') + 1);

            int indexOfn = 0;
            for(int idx = 0; idx < strArray.length; idx++){
                if (Tags[idx].equals("n") && indexOfn == 0){
                    prodResponse.setMain(strArray[idx]);
                    indexOfn++;
                }else if (Tags[idx].equals("m") || Tags[idx].equals("a")){
                    prodResponse.setValue(strArray[idx]);
                }else if (Tags[idx].equals("n") && indexOfn == 1){
                    prodResponse.setAttribute(strArray[idx]);
                    indexOfn++;
                }
                else continue;
            }
            prodResponse.setMessage("运行成功");
            return prodResponse;
        }catch (RuntimeException e) {
            prodResponse.setMessage(e.getMessage());
            return prodResponse;
        }
    }
}
