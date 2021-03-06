/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HttpLoggingInterceptor。
 *
 * @author denvie
 * @since 2020/8/24
 */
@Slf4j
public class HttpLoggingInterceptor implements HttpResponseInterceptor, HttpRequestInterceptor {
    private static final String LOG_ID_ATTRIBUTE = "ES_LOG_ID_ATTRIBUTE";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {

        String logId = (String) context.getAttribute(LOG_ID_ATTRIBUTE);

        if (logId == null) {
            logId = Integer.toHexString(System.identityHashCode(new Object()));
            context.setAttribute(LOG_ID_ATTRIBUTE, logId);
        }

        if (request instanceof HttpEntityEnclosingRequest && ((HttpEntityEnclosingRequest) request).getEntity() != null) {
            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            entity.writeTo(buffer);

            if (!entity.isRepeatable()) {
                entityRequest.setEntity(new ByteArrayEntity(buffer.toByteArray()));
            }

            log.debug("[{}] Sending request {} {} with parameters: {} {}Request body: {}",
                    logId, request.getRequestLine().getMethod().toUpperCase(), request.getRequestLine().getUri(),
                    "", LINE_SEPARATOR, new String(buffer.toByteArray()));
        } else {
            log.debug("[{}] Sending request {} {} with parameters: {}", logId,
                    request.getRequestLine().getMethod().toUpperCase(), request.getRequestLine().getUri(), "");
        }
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        String logId = (String) context.getAttribute(LOG_ID_ATTRIBUTE);
        log.debug("[{}] Received raw response: {}", logId, response.getStatusLine().getStatusCode());
    }
}
