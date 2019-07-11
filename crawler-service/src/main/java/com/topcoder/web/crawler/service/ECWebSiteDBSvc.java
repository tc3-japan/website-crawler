package com.topcoder.web.crawler.service;

import java.net.URL;

interface ECWebSiteDBSvc {

    URL getSiteURLForWebsiteId(String siteId) throws Exception;
}


