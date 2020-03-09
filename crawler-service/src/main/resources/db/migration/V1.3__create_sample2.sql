
INSERT INTO `web_sites`
(
`id`,
`name`,
`description`,
`url`,
`content_url_patterns`,
`supports_robots_txt`,
`supports_js`,
`crawl_max_depth`,
`crawl_time_limit`,
`crawl_interval`,
`parallel_size`,
`timeout_page_download`,
`retry_times`,
`page_expired_period`,
`category_extraction_pattern`,
`content_selector`,
`google_param`
) VALUES (
2,
'UNIQLO JP',
'UNIQLO Japan',
'https://www.uniqlo.com/jp/',
'https://www.uniqlo.com/jp/store/goods/[\d-]+',
1,
1,
10,
3600,
2000,
12,
2,
2,
30,
NULL,
'#prodInfo;.breadcrumbs;#prodDetail .content;#prodReview;#blkItemRelated',
'+site:https://www.uniqlo.com/jp/store/goods/'
);


INSERT INTO `web_sites`
(
`id`,
`name`,
`description`,
`url`,
`content_url_patterns`,
`supports_robots_txt`,
`supports_js`,
`crawl_max_depth`,
`crawl_time_limit`,
`crawl_interval`,
`parallel_size`,
`timeout_page_download`,
`retry_times`,
`page_expired_period`,
`category_extraction_pattern`,
`content_selector`,
`google_param`
) VALUES (
3,
'IKEA JP',
'IKEA Japan',
'https://www.ikea.com/jp/ja/',
'https://www.ikea.com/jp/ja/p/.+$',
1,
1,
10,
3600,
2000,
12,
2,
2,
30,
NULL,
'html > head > title;meta[name="description"];div.product-pip__right-container;.range-expandable__content--pip_environment_and_material;.range-expandable__content--pip_package_details;.range-expandable__content--pip_designer_thoughts;ol.bv-content-list-reviews;.range-product-recommendations',
'+site:https://www.ikea.com/jp/ja/p/'
);


