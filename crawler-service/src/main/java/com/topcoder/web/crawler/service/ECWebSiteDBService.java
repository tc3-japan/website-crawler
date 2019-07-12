package com.topcoder.web.crawler.service;

import java.net.URL;

interface ECWebSiteDBService {

    URL getSiteURLForWebsiteId(String siteId) throws Exception;
}


