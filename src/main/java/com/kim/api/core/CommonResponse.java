package com.kim.api.core;

import lombok.Getter;
import lombok.Setter;

/**
 * 공통으로 사용할 Response 형식
 */
@Getter
@Setter
public class CommonResponse<I> {
    private String result; // 성공 혹은 실패 코드
    private String message; // 호출 결과
    private I info; // 기타 필요한 정보
}
