package com.topcoder.productsearch.common.url.normalizers;

import com.topcoder.productsearch.common.url.URLNormalizer;

public class BasicURLNormalizer implements URLNormalizer {

    public String normalize(String url) {
        return "Basic Normalized "+url;
    }
}