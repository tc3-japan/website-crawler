#!/bin/bash

DAYS=$1
DATE=$(date +"%Y-%m-%d 00:00:00" --date "${DAYS} day ago")

mysql -h ${HOST} -P ${PORT} -u${USER} -p${PASSWORD} -e "DELETE FROM click_logs WHERE created_at <= '${DATE}';" website_crawler
