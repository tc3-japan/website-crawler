package com.topcoder.crawler.service;

import java.net.URL;

interface ECWebSiteDBService {

    URL getSiteURLForWebsiteId(String siteId) throws Exception;
}


