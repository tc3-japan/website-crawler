package com.topcoder.productsearch.common.url;

import java.io.IOException;
import java.net.MalformedURLException;

import com.topcoder.productsearch.common.url.normalizers.BasicURLNormalizer;
import com.topcoder.productsearch.common.url.normalizers.PassURLNormalizer;
import com.topcoder.productsearch.common.url.normalizers.RegexURLNormalizer;

public class ClientURLNormalizer {

    public static void main(String args[]) throws IOException {
        URLNormalizeManager normalizeManager = new URLNormalizeManager();
        normalizeManager.setURLNormalizer(new BasicURLNormalizer());
        normalizeManager.setURLNormalizer(new RegexURLNormalizer());
        String normalizedURL = normalizeManager.normalizeURL("http://www.abc.com/xx/../");
        System.out.println(normalizedURL);

        normalizedURL = normalizeManager.normalizeURL("http://www.abc.com/../");
        System.out.println(normalizedURL);

        normalizedURL = normalizeManager.normalizeURL("http://www.abc.com/./");
        System.out.println(normalizedURL);

        normalizedURL = normalizeManager.normalizeURL("http://www.abc.com/xx//yy");
        System.out.println(normalizedURL);        
        
    }

}