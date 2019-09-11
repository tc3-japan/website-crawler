package com.topcoder.productsearch.common.url;

import java.net.MalformedURLException;

public interface URLNormalizer {

    public String normalize(String url) throws MalformedURLException;

}