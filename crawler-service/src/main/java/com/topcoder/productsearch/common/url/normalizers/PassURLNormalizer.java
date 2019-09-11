package com.topcoder.productsearch.common.url.normalizers;

import java.net.MalformedURLException;

import com.topcoder.productsearch.common.url.URLNormalizer;

/**
 * Code adapted from Apache Nutch URL Normalization routines 
 * 
 * This URLNormalizer doesn't change urls. It is sometimes useful if for a given
 * scope at least one normalizer must be defined but no transformations are
 * required.
 * 
 */
public class PassURLNormalizer implements URLNormalizer {


        public String normalize(String urlString)
            throws MalformedURLException {
          return urlString;
        }

}