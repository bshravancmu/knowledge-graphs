# Bundles PostGIS, so no apt-get is needed (Debian bullseye, used by postgres:14.2,
# is EOL and its security pool no longer serves the packages apt resolves to).
FROM postgis/postgis:14-3.4

# copying all SQL files to this folder will automatically put them in the default DB
COPY sql/*.sql /docker-entrypoint-initdb.d/
