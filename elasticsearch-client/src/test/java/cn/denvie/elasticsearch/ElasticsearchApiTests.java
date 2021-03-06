/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch;

import cn.denvie.elasticsearch.client.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

/**
 * Elasticsearch api tests.
 *
 * @author denvie
 * @since 2020/8/5
 */
public class ElasticsearchApiTests {
    private RestHighLevelClient restHighLevelClient;
    private ObjectMapper objectMapper;
    private String index;

    @BeforeEach
    public void setUp() {
        List<String> hosts = Collections.singletonList("localhost:9200");
        String scheme = "http";
        HttpHost[] httpHosts = new HttpHost[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            String[] splits = hosts.get(i).split(":");
            httpHosts[i] = new HttpHost(splits[0].trim(),
                    Integer.parseInt(splits[1].trim()), scheme);
        }
        this.restHighLevelClient = new RestHighLevelClient(RestClient.builder(httpHosts));
        this.objectMapper = new ObjectMapper();
        this.index = "api-index";
    }

    @Ignore
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);

        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
                .build());

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> ikSmart = new HashMap<>();
        ikSmart.put("type", "text");
        ikSmart.put("analyzer", "ik_smart");
        properties.put("name", ikSmart);
        properties.put("tags", ikSmart);
        request.mapping(properties);

        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void existIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @Ignore
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void createDocument() throws IOException {
        User user = new User(1, "尛飛俠", 33,
                Arrays.asList("技术宅男", "户外强驴", "旅游达人"));
        IndexRequest request = new IndexRequest(index);
        request.id(user.getId() + "");
        request.timeout(TimeValue.timeValueSeconds(5));
        request.source(objectMapper.writeValueAsString(user), XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void existDocument() throws IOException {
        GetRequest request = new GetRequest(index, "1");
        // 不获取返回的_source内容，效率更高
        request.fetchSourceContext(new FetchSourceContext(false));
        System.out.println(restHighLevelClient.exists(request, RequestOptions.DEFAULT));
    }

    @Ignore
    @Test
    public void getDocument() throws IOException {
        GetRequest request = new GetRequest(index, "1");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void updateDocument() throws IOException {
        User user = new User(1, "尛飛俠", 18,
                Arrays.asList("技术宅男", "户外强驴", "旅游达人"));
        UpdateRequest request = new UpdateRequest(index, "1");
        request.doc(objectMapper.writeValueAsString(user), XContentType.JSON);
        UpdateResponse response = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest(index, "1");
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void bulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(10));
        // 批处理请求
        for (int i = 0; i < 10; i++) {
            User user = new User(100 + i, "尛飛俠" + i, 20 + i,
                    Collections.singletonList("全能牛人"));
            request.add(new IndexRequest(index)
                    .id(user.getId() + "")
                    .source(objectMapper.writeValueAsString(user), XContentType.JSON));
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        System.out.println(!response.hasFailures());
        System.out.println(objectMapper.writeValueAsString(response));
    }

    @Ignore
    @Test
    public void searchRequest() throws IOException {
        SearchRequest request = new SearchRequest(index);
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "尛飛俠");
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "俠");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must().add(QueryBuilders.matchQuery("name", "尛飛俠"));
        boolQueryBuilder.mustNot().add(QueryBuilders.termQuery("age", 20));
        builder.query(boolQueryBuilder)
                .timeout(TimeValue.timeValueSeconds(10))
                .from(0)
                .size(20);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false); // 多个高亮显示的字段
        highlightBuilder.field("name");
        highlightBuilder.preTags("<span style='color=red;'>");
        highlightBuilder.postTags("</span>");
        builder.highlighter(highlightBuilder);
        request.source(builder);

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println("total: " + hits.getTotalHits().value);
        System.out.println("============================================================");
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }
}
