CREATE TABLE `click_logs`  (
	`id`                     	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`query_id`               	varchar(512) NULL,
	`search_words`           	varchar(512) NOT NULL,
	`normalized_search_words`	varchar(512) NOT NULL,
	`page_url`               	varchar(768) NOT NULL,
	`page_rank`              	tinyint(3) UNSIGNED NULL,
	`created_date`           	date NOT NULL,
	`created_at`             	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`       	datetime NULL,
	PRIMARY KEY(`id`,`created_date`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

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
	`content`          	longtext NULL,
	`category`         	varchar(256) NULL,
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

CREATE TABLE `search_opt_evaluations`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`site_id`         	int(11) UNSIGNED NOT NULL,
	`truth_id`        	int(11) UNSIGNED NOT NULL,
	`result_id`       	int(11) UNSIGNED NOT NULL,
	`score`           	decimal(15,5) UNSIGNED NOT NULL DEFAULT '0',
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_result_details`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`result_id`       	int(11) UNSIGNED NOT NULL,
	`rank`            	int(5) UNSIGNED NULL DEFAULT '0',
	`url`             	varchar(768) NOT NULL,
	`title`           	varchar(1024) NULL,
	`score`           	decimal(15,5) UNSIGNED NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_results`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`site_id`         	int(11) UNSIGNED NOT NULL,
	`search_words`    	varchar(512) NOT NULL,
	`weight1`         	decimal(15,5) UNSIGNED NULL,
	`weight2`         	decimal(15,5) UNSIGNED NULL,
	`weight3`         	decimal(15,5) UNSIGNED NULL,
	`weight4`         	decimal(15,5) UNSIGNED NULL,
	`weight5`         	decimal(15,5) UNSIGNED NULL,
	`weight6`         	decimal(15,5) UNSIGNED NULL,
	`weight7`         	decimal(15,5) UNSIGNED NULL,
	`weight8`         	decimal(15,5) UNSIGNED NULL,
	`weight9`         	decimal(15,5) UNSIGNED NULL,
	`weight10`        	decimal(15,5) UNSIGNED NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_truth_details`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`truth_id`        	int(11) UNSIGNED NOT NULL,
	`rank`            	int(5) UNSIGNED NOT NULL DEFAULT '0',
	`url`             	varchar(768) NOT NULL,
	`title`           	varchar(1024) NULL,
	`score`           	decimal(15,5) NULL,
	`sim_area1`       	decimal(15,10) NULL,
	`sim_area2`       	decimal(15,10) NULL,
	`sim_area3`       	decimal(15,10) NULL,
	`sim_area4`       	decimal(15,10) NULL,
	`sim_area5`       	decimal(15,10) NULL,
	`sim_area6`       	decimal(15,10) NULL,
	`sim_area7`       	decimal(15,10) NULL,
	`sim_area8`       	decimal(15,10) NULL,
	`sim_area9`       	decimal(15,10) NULL,
	`sim_area10`      	decimal(15,10) NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_truths`  (
	`id`              	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`site_id`         	int(11) UNSIGNED NOT NULL,
	`search_words`    	varchar(512) NOT NULL,
	`created_at`      	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`	datetime NULL,
	`invalid`         	tinyint(1) UNSIGNED NOT NULL DEFAULT '0',
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
	`id`                         	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`name`                       	varchar(200) NOT NULL,
	`description`                	varchar(1024) NULL,
	`url`                        	varchar(768) NOT NULL,
	`content_url_patterns`       	varchar(2048) NULL,
	`supports_robots_txt`        	tinyint(1) UNSIGNED NULL DEFAULT '1',
	`supports_js`                	tinyint(1) UNSIGNED NULL DEFAULT '0',
	`crawl_max_depth`            	smallint(5) UNSIGNED NULL DEFAULT '5',
	`crawl_time_limit`           	mediumint(8) UNSIGNED NULL DEFAULT '0',
	`crawl_interval`             	mediumint(8) UNSIGNED NULL DEFAULT '1000',
	`parallel_size`              	mediumint(8) UNSIGNED NULL DEFAULT '12',
	`timeout_page_download`      	tinyint(1) UNSIGNED NULL DEFAULT '2',
	`retry_times`                	tinyint(1) UNSIGNED NULL DEFAULT '2',
	`page_expired_period`        	smallint(5) UNSIGNED NULL DEFAULT '30',
	`category_extraction_pattern`	varchar(2048) NULL,
	`content_selector`           	varchar(2048) NULL,
	`google_param`               	varchar(1024) NULL,
	`weight1`                    	decimal(15,5) UNSIGNED NULL,
	`weight2`                    	decimal(15,5) UNSIGNED NULL,
	`weight3`                    	decimal(15,5) UNSIGNED NULL,
	`weight4`                    	decimal(15,5) UNSIGNED NULL,
	`weight5`                    	decimal(15,5) UNSIGNED NULL,
	`weight6`                    	decimal(15,5) UNSIGNED NULL,
	`weight7`                    	decimal(15,5) UNSIGNED NULL,
	`weight8`                    	decimal(15,5) UNSIGNED NULL,
	`weight9`                    	decimal(15,5) UNSIGNED NULL,
	`weight10`                   	decimal(15,5) UNSIGNED NULL,
	`created_at`                 	datetime NULL DEFAULT CURRENT_TIMESTAMP,
	`last_modified_at`           	datetime NULL,
	`deleted`                    	tinyint(1) UNSIGNED NULL DEFAULT '0',
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

ALTER TABLE `search_opt_result_details`
	ADD CONSTRAINT `fk_sopt_result_details_resultid`
	FOREIGN KEY(`result_id`)
	REFERENCES `search_opt_results`(`id`)
	ON DELETE CASCADE ;

ALTER TABLE `search_opt_evaluations`
	ADD CONSTRAINT `fk_sopt_evaluations_resultid`
	FOREIGN KEY(`result_id`)
	REFERENCES `search_opt_results`(`id`);

ALTER TABLE `search_opt_truth_details`
	ADD CONSTRAINT `fk_sopt_truth_details_truthid`
	FOREIGN KEY(`truth_id`)
	REFERENCES `search_opt_truths`(`id`)
	ON DELETE CASCADE ;

ALTER TABLE `search_opt_evaluations`
	ADD CONSTRAINT `fk_sopt_evaluations_truthid`
	FOREIGN KEY(`truth_id`)
	REFERENCES `search_opt_truths`(`id`);

ALTER TABLE `pages`
	ADD CONSTRAINT `fk_pages_siteid`
	FOREIGN KEY(`site_id`)
	REFERENCES `web_sites`(`id`)
	ON DELETE NO ACTION ;

ALTER TABLE `search_opt_truths`
	ADD CONSTRAINT `fk_sopt_truths_siteid`
	FOREIGN KEY(`site_id`)
	REFERENCES `web_sites`(`id`);

ALTER TABLE `search_opt_results`
	ADD CONSTRAINT `fk_sopt_results_siteid`
	FOREIGN KEY(`site_id`)
	REFERENCES `web_sites`(`id`);

ALTER TABLE `search_opt_evaluations`
	ADD CONSTRAINT `fk_sopt_evaluations_siteid`
	FOREIGN KEY(`site_id`)
	REFERENCES `web_sites`(`id`);

CREATE INDEX `idx_clicklogs_cdate_nsearchwords`
	ON `click_logs`(`created_date`, `normalized_search_words`);

CREATE INDEX `idx_clicklogs_id`
	ON `click_logs`(`id`);

CREATE INDEX `idx_destination_urls_pageid` USING BTREE
	ON `destination_urls`(`page_id`);

CREATE INDEX `idx_pages_siteid_type_lastmodifiedat` USING BTREE
	ON `pages`(`site_id`, `type`, `last_modified_at` DESC);

CREATE UNIQUE INDEX `idx_pages_url` USING BTREE
	ON `pages`(`url`);

CREATE INDEX `idx_sopt_evaluations_siteid` USING BTREE
	ON `search_opt_evaluations`(`site_id`);

CREATE INDEX `idx_sopt_result_details_resultid_rank`
	ON `search_opt_result_details`(`result_id`, `rank`);

CREATE INDEX `idx_sopt_results_siteid` USING BTREE
	ON `search_opt_results`(`site_id`);

CREATE INDEX `idx_sopt_truth_details_truthid_rank` USING BTREE
	ON `search_opt_truth_details`(`truth_id`, `rank`);

CREATE INDEX `idx_sopt_truths_siteid` USING BTREE
	ON `search_opt_truths`(`site_id`);

CREATE INDEX `idx_sopt_truths_invalid` USING BTREE
	ON `search_opt_truths`(`site_id`, `invalid`, `id`);

CREATE INDEX `idx_source_urls_pageid` USING BTREE
	ON `source_urls`(`page_id`);

CREATE UNIQUE INDEX `idx_users_username` USING BTREE
	ON `users`(`username`);

CREATE INDEX `idx_users_email` USING BTREE
	ON `users`(`email`);

CREATE INDEX `idx_web_sites_name` USING BTREE
	ON `web_sites`(`name`);
