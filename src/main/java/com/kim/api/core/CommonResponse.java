package com.kim.api.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 공통으로 사용할 Response 형식
 */
@Getter
@Setter
@NoArgsConstructor
public class CommonResponse<I> {
    protected String result; // 성공 혹은 실패 코드
    protected String message; // 호출 결과
    protected I info; // 기타 필요한 정보

    public CommonResponse(String result, String message) {
        this.result = result;
        this.message = message;
    }
}
