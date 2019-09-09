package com.topcoder.productsearch.common.url;

import java.util.ArrayList;
import java.util.List;

public class URLNormalizers {

    private List<URLNormalizer> normalizers = new ArrayList<URLNormalizer>();

    public void addURLNormalizer(URLNormalizer urlNormalizer) {
        normalizers.add(urlNormalizer);
    }

    public String execute(String url) {
        StringBuffer result = new StringBuffer();
        for (URLNormalizer urlNormalizer: normalizers) {
            result.append(urlNormalizer.normalize(url));
        }
        return result.toString();

    }

    

}