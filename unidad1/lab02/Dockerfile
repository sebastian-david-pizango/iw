FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y apache2 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN sed -i 's/^Listen 80$/Listen 8081/' /etc/apache2/ports.conf

COPY web/ /var/www/html/

COPY sites/sisacad/ /iw/webapps/sisacad/
COPY sites/sistradoc/ /iw/webapps/sistradoc/

COPY apache/000-default.conf \
     /etc/apache2/sites-available/000-default.conf

EXPOSE 8088

CMD ["apachectl", "-D", "FOREGROUND"]