package com.kim.api.core.model.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Max;

@Entity(name = "trx_id")
@Getter
@Setter
@NoArgsConstructor
public class TransactionIdGenerator {
    @Id
    private String type;

    @Max(999999999999L)
    @Column
    private Long seq; // Sequence 최대길이까지 사용되면 1 로 초기화

    public TransactionIdGenerator(String type, @Max(999999999999L) Long seq) {
        this.type = type;
        this.seq = seq;
    }
}
