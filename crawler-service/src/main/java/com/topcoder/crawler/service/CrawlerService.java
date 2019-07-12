package com.topcoder.crawler.service;

import java.net.URL;
import java.util.List;

interface CrawlerService {
    List<String> getProductPagesForWebSite(URL url) throws Exception;
}