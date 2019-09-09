package com.topcoder.productsearch.common.url;

import com.topcoder.productsearch.common.url.normalizers.BasicURLNormalizer;
import com.topcoder.productsearch.common.url.normalizers.PassURLNormalizer;

public class ClientURLNormalizer {

    public static void main(String args[]) {
        URLNormalizeManager normalizeManager = new URLNormalizeManager();
        normalizeManager.setURLNormalizer(new BasicURLNormalizer());
        normalizeManager.setURLNormalizer(new PassURLNormalizer());
        String normalizedURL = normalizeManager.normalizeURL("first url");
        System.out.println(normalizedURL);

    }

}