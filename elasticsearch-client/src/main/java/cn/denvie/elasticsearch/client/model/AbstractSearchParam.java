/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.List;

/**
 * 搜索参数。
 *
 * @author denvie
 * @since 2020/8/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractSearchParam {
    /**
     * 排序字段
     */
    protected OrderField orderField;
    /**
     * 高亮字段
     */
    protected HighlightField highlightField;
    /**
     * 页数
     */
    protected int pageNo = 1;
    /**
     * 每页大小
     */
    protected int pageSize = 10;
    /**
     * 聚合搜索项
     */
    protected List<AggregationBuilder> aggregationBuilders;
    /**
     * 是否获取实际文档总数。默认情况下，当文档总数超过10000时，总数只显示10000
     */
    protected boolean trackTotalHits = false;
    /**
     * 搜索结果是否包含原生的SearchResponse引用
     */
    protected boolean withOriginalResponse = false;
}
