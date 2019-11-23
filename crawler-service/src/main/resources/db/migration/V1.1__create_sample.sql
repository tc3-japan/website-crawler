
INSERT INTO `web_sites`
(
`id`,
`name`,
`description`,
`url`,
`content_url_patterns`,
`supports_robots_txt`,
`crawl_max_depth`,
`crawl_time_limit`,
`crawl_interval`,
`parallel_size`,
`timeout_page_download`,
`retry_times`,
`page_expired_period`,
`category_extraction_pattern`,
`content_selector`
) VALUES (
1,
'UNIQLO',
'Shop UNIQLO.com for the latest essentials for women, men, kids &amp; babies. Clothing with innovation and real value, engineered to enhance your life every day, all year round. UNIQLO US.',
'https://www.uniqlo.com/us/en/',
'https://www.uniqlo.com/us/en/[^/]+?.html.*?cgid=.*?$',
1,
10,
3600,
1000,
12,
2,
2,
30,
'<a itemprop=\"item\" itemscope=\"\" itemtype=\"[\\w-:/.]+\" class=\"breadcrumb-element\" href=\"[\\w-:/.]+\" title=\"[-\\p{Blank}\\w]+\">([\\w-:/\\t\\n\\r\\p{Blank}]+)</a>',
'.product-name,.product-price,.product-info'
);


INSERT INTO `users`
(
`username`,
`password`,
`description`,
`email`
)
VALUES
(
'admin',
'aa2d6395dafbff009afbf0dcb22dfaecbd37731d7eec3b42285ddd45f62f5eea',
'Administrator',
'admin@topcoder-example.com'
);


