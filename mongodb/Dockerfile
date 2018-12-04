FROM mongo
COPY replconfig.js /docker-entrypoint-initdb.d/replconfig.js
CMD ["mongod", "--replSet", "rs0"]
