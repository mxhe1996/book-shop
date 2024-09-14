package com.hmx.es.domain;

import java.io.Serializable;

public interface BasicEsObject extends Serializable {

//    private String indexName;


    String getIndexName();

    String getObjectId();
}
