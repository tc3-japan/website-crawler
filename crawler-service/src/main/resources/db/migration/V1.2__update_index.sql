
ALTER TABLE `destination_urls`
  DROP FOREIGN KEY `fk_destination_urls_pageid`;

DROP INDEX `idx_destination_urls_pageid` ON destination_urls;

CREATE INDEX `idx_destination_urls_pageid` USING BTREE 
  ON `destination_urls`(`page_id`);

ALTER TABLE `destination_urls`
  ADD CONSTRAINT `fk_destination_urls_pageid`
  FOREIGN KEY(`page_id`)
  REFERENCES `pages`(`id`);


ALTER TABLE `source_urls`
  DROP FOREIGN KEY `fk_source_urls_pageid`;

DROP INDEX `idx_source_urls_pageid` ON source_urls;

CREATE INDEX `idx_source_urls_pageid` USING BTREE 
  ON `source_urls`(`page_id`);

ALTER TABLE `source_urls`
  ADD CONSTRAINT `fk_source_urls_pageid`
  FOREIGN KEY(`page_id`)
  REFERENCES `pages`(`id`);

  