FROM mysql:8.0.17
VOLUME /tmp

RUN apt update && apt install -y zip

COPY backup_click_log.sh .

ENTRYPOINT ["bash", "backup_click_log.sh"]
