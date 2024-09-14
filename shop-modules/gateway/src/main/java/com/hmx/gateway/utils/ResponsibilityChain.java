package com.hmx.gateway.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.function.Predicate;

@Data
public class ResponsibilityChain<T> {

    private ResponsibilityChain<T> nextNode;

    private String errorMessage;

    private Predicate<T> predicate;

    public ResponsibilityChain(){}

    public ResponsibilityChain(String errorMessage, Predicate<T> predicate) {
        this.errorMessage = errorMessage;
        this.predicate = predicate;
    }

    public boolean exec(T t, @NotNull Predicate<T> predicate){
        if (predicate.test(t)){
            return Objects.isNull(nextNode) || nextNode.exec(t);
        }
        throw new RuntimeException(errorMessage);
    }

    public boolean exec(T t){
        if (Objects.nonNull(predicate) && predicate.test(t)){
            return Objects.isNull(nextNode)||nextNode.exec(t);
        }
        throw new RuntimeException(errorMessage);
    }


}
