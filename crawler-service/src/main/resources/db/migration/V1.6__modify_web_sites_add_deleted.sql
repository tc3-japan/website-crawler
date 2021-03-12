
ALTER TABLE `web_sites` ADD COLUMN (
    `deleted` tinyint(1) UNSIGNED NOT NULL DEFAULT '0'
);

ALTER TABLE `pages` ADD COLUMN (
    `content`              longtext NULL,
    `category`             varchar(256) NULL
);
