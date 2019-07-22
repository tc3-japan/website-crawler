package com.topcoder.productsearch.crawler.website;

import com.topcoder.productsearch.common.entity.WebSite;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Uniqlo website crawler.
 */
@Slf4j
public class UniqloWebSiteCrawler extends WebSiteCrawler {

  /**
   * The "home/" suffix of the uniqlo website url.
   */
  private static final String HOME_SUFFIX = "/home/";

  /**
   * RegExp to match the product variation url.
   */
  private static final Pattern PRODUCT_VARIATION_PREFIX = Pattern.compile(
      "^https?://www\\.uniqlo\\.com/on/demandware\\.store/Sites-[^/]+/[^/]+/Product-Variation?.*$",
      Pattern.CASE_INSENSITIVE);

  /**
   * Site url for testing external links with http:// protocol.
   */
  private final UriComponents siteUrl;

  /**
   * Constructor that takes the uniqlo website object.
   *
   * @param webSite - the website object.
   */
  public UniqloWebSiteCrawler(WebSite webSite) {
    super(webSite);

    String url = webSite.getUrl();
    if (!url.endsWith("/")) {
      url += "/";
    }
    if (url.endsWith(HOME_SUFFIX)) {
      url = url.substring(0, url.length() - HOME_SUFFIX.length());
    }

    siteUrl = UriComponentsBuilder.fromHttpUrl(url).build();
  }

  @Override
  protected boolean isExternal(String url) {
    try {
      UriComponents parsed = UriComponentsBuilder.fromHttpUrl(url).build();
      return !Objects.equals(parsed.getHost(), siteUrl.getHost())
          || !parsed.getPath().startsWith(siteUrl.getPath());
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  protected Set<String> crawlInternal(Document document) {
    Elements links = document.select("a[href]");

    Set<String> uris = new HashSet<>();

    for (Element element : links) {
      String href = normalize(element.attr("abs:href"), document.baseUri());
      if (!isExternal(href)) {
        uris.add(href);
      }
    }

    return uris;
  }

  /**
   * Normalize url. If the url is a product variation url, try to construct a product url from it.
   * Otherwise remove the fragment part, but move the cgid in fragment to query.
   *
   * @param href - the url to normal
   * @param baseUri - the base uri of the document.
   * @return normalized url
   */
  private String normalize(String href, String baseUri) {
    href = href.replaceAll("' \\+ anchorName \\+ '", "");
    Matcher variationMatcher = PRODUCT_VARIATION_PREFIX.matcher(href);
    if (variationMatcher.find()) {
      return updateBaseUriQuery(href, baseUri);
    } else {
      return removeFragment(href);
    }
  }

  /**
   * Update the query part of the base uri with the query from href.
   *
   * @param href - the variation url
   * @param baseUri - the base uri of the document.
   * @return url
   */
  private String updateBaseUriQuery(String href, String baseUri) {
    // Update original query with the variation
    MultiValueMap<String, String> query = UriComponentsBuilder.fromHttpUrl(href).build()
        .getQueryParams();
    MultiValueMap<String, String> originalQuery = UriComponentsBuilder.fromHttpUrl(baseUri)
        .build().getQueryParams();

    MultiValueMap<String, String> newQuery = new LinkedMultiValueMap<>();
    for (Map.Entry<String, List<String>> entry : originalQuery.entrySet()) {
      List<String> values = query.get(entry.getKey());
      if (values == null || values.stream()
          .map(StringUtils::trimWhitespace)
          .allMatch(StringUtils::isEmpty)) {
        values = entry.getValue();
      }
      newQuery.put(entry.getKey(), values);
    }

    return UriComponentsBuilder.fromHttpUrl(baseUri)
        .replaceQueryParams(newQuery)
        .fragment(null).build().toString();
  }

  /**
   * Remove fragment but keeps the cgid.
   *
   * @param href - url to process
   * @return the url without fragment.
   */
  private String removeFragment(String href) {
    int i = href.lastIndexOf('#');
    if (i > 0) {
      String url = href.substring(0, i);
      String frag = href.substring(i + 1);
      int j = frag.indexOf("cgid=");
      if (j > 0) {
        return UriComponentsBuilder.fromHttpUrl(url)
            .replaceQueryParam("cgid", frag.substring(j + "cgid=".length()))
            .build().toString();
      } else {
        return url;
      }
    }
    return href;
  }
}
