
CREATE TABLE `destination_urls`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`url`             	varchar(768) NOT NULL,
	`page_id`         	int(11) UNSIGNED NOT NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `pages`  (
	`id`               	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`url`              	varchar(768) NOT NULL,
	`site_id`          	int(11) UNSIGNED NOT NULL,
	`type`             	varchar(20) NOT NULL,
	`title`            	varchar(1024) NULL,
	`body`             	longtext NULL,
	`etag`             	varchar(256) NULL,
	`last_modified`    	varchar(256) NULL DEFAULT 'CURRENT_TIMESTAMP',
	`created_at`       	datetime NOT NULL,
	`last_modified_at` 	datetime NOT NULL,
	`last_processed_at`	datetime NULL,
	`deleted`          	tinyint(1) UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `source_urls`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`url`             	varchar(768) NOT NULL,
	`page_id`         	int(11) UNSIGNED NOT NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `users`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`username`        	varchar(256) NOT NULL,
	`password`        	varchar(512) NOT NULL,
	`description`     	varchar(2018) NULL,
	`email`           	varchar(256) NULL,
	`deleted`         	tinyint(1) UNSIGNED NOT NULL DEFAULT '0',
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `web_sites`  (
	`id`                  	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`name`                	varchar(200) NOT NULL,
	`description`         	varchar(1024) NULL,
	`url`                 	varchar(768) NOT NULL,
	`content_url_patterns`	varchar(2048) NULL,
	`supports_robots_txt` 	tinyint(1) UNSIGNED NULL DEFAULT '1',
	`crawl_max_depth`     	smallint(5) UNSIGNED NULL DEFAULT '5',
	`crawl_time_limit`    	mediumint(8) UNSIGNED NULL DEFAULT '0',
	`created_at`          	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`    	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE `destination_urls`
	ADD CONSTRAINT `fk_destination_urls_pageid`
	FOREIGN KEY(`page_id`)
	REFERENCES `pages`(`id`)
	ON DELETE CASCADE ;

ALTER TABLE `source_urls`
	ADD CONSTRAINT `fk_source_urls_pageid`
	FOREIGN KEY(`page_id`)
	REFERENCES `pages`(`id`)
	ON DELETE CASCADE ;

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

CREATE UNIQUE INDEX `idx_users_username` USING BTREE
	ON `users`(`username`);

CREATE INDEX `idx_users_email` USING BTREE
	ON `users`(`email`);

CREATE INDEX `idx_web_sites_name` USING BTREE
	ON `web_sites`(`name`);
