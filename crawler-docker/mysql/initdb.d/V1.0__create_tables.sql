
CREATE TABLE `destination_urls`  ( 
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `url`               varchar(768) NOT NULL,
  `page_id`           int(11) UNSIGNED NOT NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `pages`  ( 
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `url`               varchar(768) NOT NULL,
  `site_id`           int(11) UNSIGNED NOT NULL,
  `type`              varchar(20) NOT NULL,
  `title`             varchar(1024) NULL,
  `body`              longtext NULL,
  `etag`              varchar(256) NULL,
  `last_modified`     varchar(256) NULL,
  `created_at`        datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_processed_at` datetime NULL,
  `deleted`           tinyint(1) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `source_urls`  ( 
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `url`               varchar(768) NOT NULL,
  `page_id`           int(11) UNSIGNED NOT NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `web_sites`  ( 
  `id`                    int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `name`                  varchar(200) NOT NULL,
  `description`           varchar(1024) NULL,
  `url`                   varchar(768) NOT NULL,
  `content_url_patterns`  varchar(2048) NULL,
  `created_at`            datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`      datetime NULL,
  `supports_robots_txt` 	tinyint(1) UNSIGNED NULL DEFAULT 1,
  `crawl_max_depth`       SMALLINT(5) UNSIGNED NULL DEFAULT 0,
  `crawl_time_limit`      MEDIUMINT(8) UNSIGNED NULL DEFAULT 0,
  `crawl_interval`        MEDIUMINT(8) UNISIGNED NULL DEFAULT 1000,
  `parallel_size`         tinyint(1) UNSIGNED NULL DEFAULT 12,
  `timeout_page_download` tinyint(1) UNSIGNED NULL DEFAULT 2,
  `retry_times`           tinyint(1) UNSIGNED NULL DEFAULT 2,
  `page_expired_period`   SMALLINT(5) UNSIGNED NULL DEFAULT 30,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `destination_urls`
  ADD CONSTRAINT `fk_destination_urls_pageid`
  FOREIGN KEY(`page_id`)
  REFERENCES `pages`(`id`)
  ON DELETE CASCADE;

ALTER TABLE `source_urls`
  ADD CONSTRAINT `fk_source_urls_pageid`
  FOREIGN KEY(`page_id`)
  REFERENCES `pages`(`id`)
  ON DELETE CASCADE;

ALTER TABLE `pages`
  ADD CONSTRAINT `fk_pages_siteid`
  FOREIGN KEY(`site_id`)
  REFERENCES `web_sites`(`id`)
  ON DELETE NO ACTION ;

CREATE INDEX `idx_destination_urls_pageid` USING BTREE 
  ON `destination_urls`(`page_id`);

CREATE INDEX `idx_pages_siteid_type_lastmodifiedat` USING BTREE 
  ON `pages`(`site_id`, `type`, `last_modified_at` DESC);

CREATE UNIQUE INDEX `idx_pages_url` USING BTREE 
  ON `pages`(`url`);

CREATE INDEX `idx_source_urls_pageid` USING BTREE
  ON `source_urls`(`page_id`);

CREATE INDEX `idx_web_sites_name` USING BTREE 
  ON `web_sites`(`name`);


