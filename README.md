# Flink CDC 
Local testing of Flink CDC with Postgres

## Prerequisites

Setup a docker postgres DB with the following command:

```bash
docker run -d --name flink-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
```

Setup replication slot on the postgres DB:

```bash
show wal_level;
alter system set wal_level = logical;

docker restart <container-id>

 
```

Create some tables and change `replica identity` to `full`
```
CREATE TABLE public.missing_uuid (
	epoch int8 NULL,
	uuid varchar NULL,
	event_type varchar NULL,
	"comment" varchar NULL
);

alter table missing_uuid replica identity full

```

Add some data

Start the CDC connector by running [CDCPull](src/main/java/com/atntv/dp/CDCPull.java)

```
# check if the slot is created
select * from pg_replication_slots;
```

Output is printed to the console

```
3> {"before":null,"after":{"epoch":835104649959,"uuid":"uuid59","event_type":"event9","ts":1699574400000000},"source":{"version":"1.6.4.Final","connector":"postgresql","name":"postgres_cdc_source","ts_ms":1699915558611,"snapshot":"false","db":"postgres","sequence":"[null,\"22781616\"]","schema":"public","table":"missing_uuid","txId":784,"lsn":22781616,"xmin":null},"op":"c","ts_ms":1699915583940,"transaction":null}
4> {"before":{"epoch":835104649950,"uuid":"uuid50","event_type":"event50","ts":1699574400000000},"after":null,"source":{"version":"1.6.4.Final","connector":"postgresql","name":"postgres_cdc_source","ts_ms":1699915571225,"snapshot":"false","db":"postgres","sequence":"[\"22782520\",\"22782520\"]","schema":"public","table":"missing_uuid","txId":785,"lsn":22782576,"xmin":null},"op":"d","ts_ms":1699915583942,"transaction":null}
5> {"before":null,"after":{"epoch":835104649969,"uuid":"uuid9","event_type":"event9","ts":1699574400000000},"source":{"version":"1.6.4.Final","connector":"postgresql","name":"postgres_cdc_source","ts_ms":1699915681084,"snapshot":"false","db":"postgres","sequence":"[\"22782720\",\"22782720\"]","schema":"public","table":"missing_uuid","txId":787,"lsn":22782824,"xmin":null},"op":"c","ts_ms":1699915681552,"transaction":null}
```