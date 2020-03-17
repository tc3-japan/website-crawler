CREATE TABLE `search_opt_evaluations`  (
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `site_id`           int(11) UNSIGNED NOT NULL,
  `truth_id`          int(11) UNSIGNED NOT NULL,
  `result_id`         int(11) UNSIGNED NOT NULL,
  `score`             decimal(15,5) UNSIGNED NOT NULL DEFAULT '0',
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_result_details`  (
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `result_id`         int(11) UNSIGNED NOT NULL,
  `rank`              int(5) UNSIGNED NULL DEFAULT '0',
  `url`               varchar(768) NOT NULL,
  `title`             varchar(1024) NULL,
  `score`             decimal(15,5) UNSIGNED NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_results`  (
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `site_id`           int(11) UNSIGNED NOT NULL,
  `search_words`      varchar(512) NOT NULL,
  `weight1`           decimal(15,5) UNSIGNED NULL,
  `weight2`           decimal(15,5) UNSIGNED NULL,
  `weight3`           decimal(15,5) UNSIGNED NULL,
  `weight4`           decimal(15,5) UNSIGNED NULL,
  `weight5`           decimal(15,5) UNSIGNED NULL,
  `weight6`           decimal(15,5) UNSIGNED NULL,
  `weight7`           decimal(15,5) UNSIGNED NULL,
  `weight8`           decimal(15,5) UNSIGNED NULL,
  `weight9`           decimal(15,5) UNSIGNED NULL,
  `weight10`          decimal(15,5) UNSIGNED NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_truth_details`  (
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `truth_id`          int(11) UNSIGNED NOT NULL,
  `rank`              int(5) UNSIGNED NOT NULL DEFAULT '0',
  `url`               varchar(768) NOT NULL,
  `title`             varchar(1024) NULL,
  `score`             decimal(15,5) NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE `search_opt_truths`  (
  `id`                int(11) UNSIGNED AUTO_INCREMENT NOT NULL,
  `site_id`           int(11) UNSIGNED NOT NULL,
  `search_words`      varchar(512) NOT NULL,
  `created_at`        datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_at`  datetime NULL,
  PRIMARY KEY(`id`)
)
ENGINE = InnoDB
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

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

ALTER TABLE `search_opt_results`
  ADD CONSTRAINT `fk_sopt_results_siteid`
  FOREIGN KEY(`site_id`)
  REFERENCES `web_sites`(`id`);

ALTER TABLE `search_opt_truths`
  ADD CONSTRAINT `fk_sopt_truths_siteid`
  FOREIGN KEY(`site_id`)
  REFERENCES `web_sites`(`id`);

ALTER TABLE `search_opt_evaluations`
  ADD CONSTRAINT `fk_sopt_evaluations_siteid`
  FOREIGN KEY(`site_id`)
  REFERENCES `web_sites`(`id`);

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
