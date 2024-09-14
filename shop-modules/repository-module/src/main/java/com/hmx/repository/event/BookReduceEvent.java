package com.hmx.repository.event;

import com.hmx.repository.domain.Book;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class BookReduceEvent implements BookEvent{

    private String orderId;

    private List<Object> bookReduceResult = new ArrayList<>();

    public void addResult(ErrorReduceResult singleResult){
        bookReduceResult.add(singleResult);
    }

    @Override
    public Object getKeyPoint(){
        return orderId;
    }

    @Override
    public List<Object> getEventObjects() {
        return bookReduceResult;
    }

    @Override
    public String getReplayMessage() {
        return null;
    }

    @Data
    public static class ErrorReduceResult{
        private Long orderId;
        private Book bookInfo;
        private Integer number;
    }



}
