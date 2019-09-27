package com.topcoder.productsearch.crawler.service;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.topcoder.productsearch.common.entity.WebSite;
import com.topcoder.productsearch.crawler.CrawlerTask;
import com.topcoder.productsearch.crawler.CrawlerThreadPoolExecutor;

public interface CrawlerService {
    public void crawler();
    public WebSite getWebSite();
    public CrawlerThreadPoolExecutor getThreadPoolExecutor();
    Map<String, Boolean> getShouldVisit();
    LinkedBlockingQueue<CrawlerTask> getQueueTasks();
}