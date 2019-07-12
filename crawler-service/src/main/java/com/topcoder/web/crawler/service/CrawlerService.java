package com.topcoder.web.crawler.service;

import java.net.URL;
import java.util.List;

interface Crawler {
    List<String> getProductPagesForWebSite(URL url) throws Exception;
}