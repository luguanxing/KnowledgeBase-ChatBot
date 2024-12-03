package com.luguanxing.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AjaxResponse {
    private int role;
    private String answer;

    public static AjaxResponse systemAnswer(String answer) {
        return new AjaxResponse(0, answer);
    }

    public static AjaxResponse aiAnswer(String answer) {
        return new AjaxResponse(1, answer);
    }
}
