package com.hmx.search.controller;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hmx.es.service.ElasticService;
import com.hmx.search.domain.OrderDocument;
import com.hmx.shop.domain.CommonResponseBody;
import com.hmx.shop.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchCenterController {

    private final ElasticService<OrderDocument> elasticService;

    @GetMapping("/query")
    public CommonResponseBody<OrderDocument> queryDoc(String index, String docId) throws IOException {
        SearchResponse<OrderDocument> searchResponse = elasticService.searchDocument(index, docId, OrderDocument.class);
        OrderDocument source = searchResponse.hits().hits().get(0).source();
        return ResponseUtils.toResponse(source);
    }


    @GetMapping("/fuzzy/query")
    public CommonResponseBody<List<OrderDocument>> queryDoc(String likeWord) throws IOException {
        SearchResponse<OrderDocument> searchResponse = elasticService.searchLikeWord("order", likeWord, OrderDocument.class);
        List<Hit<OrderDocument>> hits = searchResponse.hits().hits();
        List<OrderDocument> orderDocumentList = hits.stream().map(Hit::source).collect(Collectors.toList());
        return ResponseUtils.toResponse(orderDocumentList);
    }



}
