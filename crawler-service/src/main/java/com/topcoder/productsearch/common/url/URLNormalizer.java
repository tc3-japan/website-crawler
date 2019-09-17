package com.topcoder.productsearch.common.url;

import java.io.IOException;

public interface URLNormalizer {

    public String normalize(String url) throws  IOException;

}