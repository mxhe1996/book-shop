package com.hmx.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.CreateOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.hmx.es.domain.BasicEsObject;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticService<T extends BasicEsObject> {

    private final ElasticsearchClient elasticsearchClient;

    public CreateIndexResponse createIndex(T t){
        String indexName =  t.getIndexName();
        CreateIndexResponse createIndexResponse = new CreateIndexResponse.Builder().acknowledged(false).build();
        try{
            if (elasticsearchClient.indices().exists(ExistBuilder->ExistBuilder.index(indexName)).value()){
                createIndexResponse = elasticsearchClient.indices().create(createBuilder -> createBuilder.index(indexName));
                log.info("创建索引成功:{}",indexName);
                return createIndexResponse;
            }else {
                log.warn("索引{}已存在",indexName);
                throw new RuntimeException(String.format("索引%s已存在，请勿重复创建", indexName));
            }
        }catch (Exception e){
            log.error("创建索引异常:{}",indexName);
            throw new RuntimeException(e.getMessage());
//            return createIndexResponse;
        }
    }


    public UpdateResponse updateDocument(String indexName, String docId, Object object) throws IOException {
        UpdateRequest<Void, Object> updateRequest = new UpdateRequest.Builder<Void, Object>().id(docId).index(indexName).doc(object).build();
        UpdateResponse<Void> update = elasticsearchClient.update(updateRequest, object.getClass());
        return update;
    }

    public SearchResponse searchDocument(String indexName, String docId, Class returnClass) throws IOException {
        return elasticsearchClient.search(searchBuild ->
                        searchBuild.index(indexName).query(queryBuild -> queryBuild.ids(idsQuery -> idsQuery.values(docId)))
                , returnClass);
    }

    public SearchResponse<T> searchDocument(String indexName, String searchField, String searchValue, Class<T> returnClass)throws IOException{
        return elasticsearchClient.search(searchBuild ->
                        searchBuild
                                .index(indexName)
                                .query(queryBuild ->
                                        queryBuild
                                                .term(termBuild -> termBuild.field(searchField).value(searchValue)))
                , returnClass);
    }

    public SearchResponse<T> searchLikeWord(String indexName, String likeWord, Class<T> returnClas) throws IOException {
        return elasticsearchClient.search(searchBuild -> searchBuild.index(indexName).query(queryBuild ->
                queryBuild.bool(boolBuild -> boolBuild.should(objBuild ->
                                objBuild.term(termQuery -> termQuery.field("itemsList.itemName").value(likeWord))
                        )
                )
        ), returnClas);

    }


    // 批量插入
    public BulkResponse insertDocuments(String indexName, List<T> docs){

        List<BulkOperation> createOperationList = docs.stream().map(doc -> {
            return new BulkOperation.Builder().create(createBuild -> {
                CreateOperation.Builder<Object> docBuilder = createBuild.document(doc);
                if (StringUtils.isNotEmpty(doc.getObjectId())){
                    docBuilder = docBuilder.id(doc.getObjectId());
                }
                if (StringUtils.isNotEmpty(doc.getIndexName())){
                    docBuilder = docBuilder.index(doc.getIndexName());
                }
                return docBuilder;
            }).build();
                }
        ).collect(Collectors.toList());

        BulkResponse bulkResponse = commonBulkOperation(indexName, createOperationList);
        return bulkResponse;
    }


    // 批量删除
    public BulkResponse deleteDocuments(String indexName, List<String> ids){
        List<BulkOperation> delBulkOperation = ids.stream().map(id -> new BulkOperation.Builder().delete(delBuilder -> delBuilder.id(id)).build()).collect(Collectors.toList());
        BulkResponse bulkResponse = commonBulkOperation(indexName, delBulkOperation);
        return bulkResponse;
    }


    public BulkResponse commonBulkOperation(String indexName, List<BulkOperation> operationList){
        try{
            return elasticsearchClient.bulk(bulkBuild -> bulkBuild.index(indexName).operations(operationList));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }





}
