#!/bin/bash

DATE=$(date +"%Y%m%d%H%M%S")

echo 'id,search_id,search_words,normalized_search_words,page_url,page_rank,created_date,created_at,last_modified_at' > /tmp/click_log.csv
mysqldump -h ${HOST} -P ${PORT} -u${USER} -p${PASSWORD} --skip-extended-insert --no-create-info website_crawler click_logs | grep 'INSERT INTO' | sed -e 's/INSERT INTO `.*` VALUES //' -e 's/^(//' -e 's/);$//' -e "s/^'/\"/" -e "s/,'/,\"/g" -e "s/',/\",/g"  -e "s/'$/\"/" >> /tmp/click_log.csv

cd /tmp
zip click_log_${DATE}.zip click_log.csv
rm /tmp/click_log.csv
