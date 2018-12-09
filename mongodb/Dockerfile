FROM mongo
COPY *.js /docker-entrypoint-initdb.d/
CMD ["mongod", "--replSet", "rs0"]
