package com.topcoder.productsearch.common.url.normalizers;

import com.topcoder.productsearch.common.url.URLNormalizer;

public class PassURLNormalizer implements URLNormalizer{

    public String normalize(String url) {
        return "Pass Normalized URL: "+url;
    }
}