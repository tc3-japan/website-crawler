package com.topcoder.productsearch.common.url;

public class URLNormalizeManager {

    URLNormalizers urlNormalizers;

    public URLNormalizeManager() {
        urlNormalizers = new URLNormalizers();
    }

    public void setURLNormalizer(URLNormalizer urlNormalizer) {
        urlNormalizers.addURLNormalizer(urlNormalizer);
    }

    public String normalizeURL(String url) {
        return urlNormalizers.execute(url);
    }

}