#!/bin/bash

DATE=$(date +"%Y%m%d%H%M%S")

mysqldump -h ${HOST} -P ${PORT} -u${USER} -p${PASSWORD} website_crawler click_logs > /tmp/click_log.dump
cd /tmp
zip click_log_${DATE}.zip click_log.dump
rm /tmp/click_log.dump
