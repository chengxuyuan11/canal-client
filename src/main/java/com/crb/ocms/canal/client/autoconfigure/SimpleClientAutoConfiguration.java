package com.crb.ocms.canal.client.autoconfigure;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.crb.ocms.canal.client.client.SimpleCanalClient;
import com.crb.ocms.canal.client.factory.EntryColumnModelFactory;
import com.crb.ocms.canal.client.handler.EntryHandler;
import com.crb.ocms.canal.client.handler.MessageHandler;
import com.crb.ocms.canal.client.handler.RowDataHandler;
import com.crb.ocms.canal.client.handler.impl.RowDataHandlerImpl;
import com.crb.ocms.canal.client.handler.impl.SyncMessageHandlerImpl;
import com.crb.ocms.canal.client.properties.CanalProperties;
import com.crb.ocms.canal.client.properties.CanalSimpleProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CanalSimpleProperties.class)
@ConditionalOnBean(value = {EntryHandler.class})
@ConditionalOnProperty(value = CanalProperties.CANAL_MODE, havingValue = "simple", matchIfMissing = true)
//@Import(ThreadPoolAutoConfiguration.class)
public class SimpleClientAutoConfiguration {


    private CanalSimpleProperties canalSimpleProperties;


    public SimpleClientAutoConfiguration(CanalSimpleProperties canalSimpleProperties) {
        this.canalSimpleProperties = canalSimpleProperties;
    }


    @Bean
    public RowDataHandler<CanalEntry.RowData> rowDataHandler() {
        return new RowDataHandlerImpl(new EntryColumnModelFactory());
    }

//    @Bean
//    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "true", matchIfMissing = true)
//    public MessageHandler messageHandler(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers,
//                                         ExecutorService executorService) {
//        return new AsyncMessageHandlerImpl(entryHandlers, rowDataHandler, executorService);
//    }


    @Bean
    @ConditionalOnProperty(value = CanalProperties.CANAL_ASYNC, havingValue = "false")
    public MessageHandler messageHandler(RowDataHandler<CanalEntry.RowData> rowDataHandler, List<EntryHandler> entryHandlers) {
        return new SyncMessageHandlerImpl(entryHandlers, rowDataHandler);
    }


    @Bean(initMethod = "start", destroyMethod = "stop")
    public SimpleCanalClient simpleCanalClient(MessageHandler messageHandler) {
        String server = canalSimpleProperties.getServer();
        String[] array = server.split(":");
        return SimpleCanalClient.builder()
                .hostname(array[0])
                .port(Integer.parseInt(array[1]))
                .destination(canalSimpleProperties.getDestination())
                .userName(canalSimpleProperties.getUserName())
                .password(canalSimpleProperties.getPassword())
                .messageHandler(messageHandler)
                .batchSize(canalSimpleProperties.getBatchSize())
                .filter(canalSimpleProperties.getFilter())
                .timeout(canalSimpleProperties.getTimeout())
                .unit(canalSimpleProperties.getUnit())
                .build();
    }

}
