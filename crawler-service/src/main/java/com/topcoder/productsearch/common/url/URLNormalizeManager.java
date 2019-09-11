package com.topcoder.productsearch.common.url;

import java.net.MalformedURLException;

public class URLNormalizeManager {

    URLNormalizers urlNormalizers;

    public URLNormalizeManager() {
        urlNormalizers = new URLNormalizers();
    }

    public void setURLNormalizer(URLNormalizer urlNormalizer) {
        urlNormalizers.addURLNormalizer(urlNormalizer);
    }

    public String normalizeURL(String url) throws MalformedURLException {
        return urlNormalizers.execute(url);
    }

}