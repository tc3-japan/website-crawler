
ALTER TABLE `search_opt_truths` ADD COLUMN (
    `invalid` tinyint(1) UNSIGNED NOT NULL DEFAULT '0'
);

CREATE INDEX `idx_sopt_truths_invalid` USING BTREE
    ON `search_opt_truths`(`site_id`, `invalid`, `id`);
