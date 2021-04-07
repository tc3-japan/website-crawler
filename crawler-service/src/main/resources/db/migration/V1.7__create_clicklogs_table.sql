CREATE TABLE `click_logs`  (
	`id`                     	int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
	`search_id`              	varchar(512) NULL,
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


CREATE INDEX `idx_clicklogs_cdate_nsearchwords`
	ON `click_logs`(`created_date`, `normalized_search_words`);

CREATE INDEX `idx_clicklogs_id`
	ON `click_logs`(`id`);
