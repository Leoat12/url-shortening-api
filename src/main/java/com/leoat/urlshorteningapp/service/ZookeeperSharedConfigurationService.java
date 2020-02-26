package com.leoat.urlshorteningapp.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.VersionedValue;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;

@Slf4j
@Service
public class ZookeeperSharedConfigurationService implements SharedConfigurationService {

    @Value("${application.zookeeper.base-url}")
    private String baseUrl;

    @Override
    public Integer getSharedCounter(final String key) {

        final CuratorFramework client = newClient(baseUrl,
                new RetryNTimes(3, 100));
        client.start();

        SharedCount sharedCounter = new SharedCount(client, key, 0);
        try {
            sharedCounter.start();

            VersionedValue<Integer> counter = sharedCounter.getVersionedValue();
            while(!sharedCounter.trySetCount(counter, counter.getValue() + 1)) {
                counter = sharedCounter.getVersionedValue();
            }

            sharedCounter.close();
            client.close();
            return counter.getValue();
        } catch (Exception e) {
            log.error("Error while starting shared counter, impossible to update counter.", e);
            throw new RuntimeException();
        }
    }
}
