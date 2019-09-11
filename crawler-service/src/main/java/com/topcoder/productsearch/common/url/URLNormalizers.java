package com.topcoder.productsearch.common.url;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class URLNormalizers {

    private List<URLNormalizer> normalizers = new ArrayList<URLNormalizer>();

    public void addURLNormalizer(URLNormalizer urlNormalizer) {
        normalizers.add(urlNormalizer);
    }

    public String execute(String url) throws MalformedURLException {

        if (url == null) {
            return null;
        }
        for (URLNormalizer urlNormalizer: normalizers) {
            url = urlNormalizer.normalize(url);
        }
        return url;
    }

}