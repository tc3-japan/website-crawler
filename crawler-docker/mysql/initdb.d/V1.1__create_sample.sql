
INSERT INTO `web_sites`
(
`id`,
`name`,
`description`,
`url`,
`content_url_patterns`,
`crawl_max_depth`,
`crawl_time_limit`
) VALUES (
1,
'UNIQLO',
'Shop UNIQLO.com for the latest essentials for women, men, kids &amp; babies. Clothing with innovation and real value, engineered to enhance your life every day, all year round. UNIQLO US.',
'https://www.uniqlo.com/us/en/',
'https://www.uniqlo.com/us/en/[^/]+?.html.*?cgid=.*?$',
10,
3600
);


