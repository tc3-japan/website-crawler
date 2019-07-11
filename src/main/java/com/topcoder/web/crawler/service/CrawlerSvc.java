package com.topcoder.web.crawler.service;

import java.net.URL;
import java.util.List;

interface CrawlerService {
    List<String> getProductPagesForWebSite(URL url) throws Exception;
}