package com.hmx.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmx.search.domain.SearchEventProcessed;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface SearchEventProcessedMapper extends BaseMapper<SearchEventProcessed> {

    @Select("select * from search_processed where aggregate_type=#{aggregateType} and aggregate_id=#{aggregateId}")
    List<SearchEventProcessed> searchMaxEventId(@Param("aggregateType")String aggregateType, @Param("aggregateId")String aggregateId);

}
