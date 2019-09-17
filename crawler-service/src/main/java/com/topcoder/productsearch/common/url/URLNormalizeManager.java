package com.topcoder.productsearch.common.url;

import java.io.IOException;


public class URLNormalizeManager {

    URLNormalizers urlNormalizers;

    public URLNormalizeManager() {
        urlNormalizers = new URLNormalizers();
    }

    public void setURLNormalizer(URLNormalizer urlNormalizer) {
        urlNormalizers.addURLNormalizer(urlNormalizer);
    }

    public String normalizeURL(String url) throws IOException {
        return urlNormalizers.execute(url);
    }

}