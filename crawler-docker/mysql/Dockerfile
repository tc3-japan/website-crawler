FROM mysql:8.0.17

COPY initdb.d/*.sql /docker-entrypoint-initdb.d/

COPY conf.d/my.cnf /etc/mysql/conf.d/

