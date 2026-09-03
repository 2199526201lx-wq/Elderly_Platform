package com.example.elderly_Platform.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssessmentSubmitDTO {
    @NotNull(message = "问卷编号不能为空")
    private Long questionnaireId;
    @NotNull(message = "内容不能为空")
    @Valid
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem{
        @NotNull(message = "题目编号不能为空")
        private Long questionId;
        private List<String> selectedTexts;
        private String meaning;
        private String text;
    }


}
