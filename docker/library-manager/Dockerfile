FROM denoland/deno:debian

ENV DENO_DIR=/deno-dir

RUN apt-get update && \
  apt-get install -y unzip

COPY scripts /usr/bin
