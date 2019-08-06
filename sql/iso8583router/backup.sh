#!/bin/sh

FILE_OUT="$(dirname $0)"/backup.sql;

echo "exporting tables to" $FILE_OUT;

pg_dump --section data --column-inserts -t crud_service > $FILE_OUT;
pg_dump --section data --column-inserts -t crud_user >> $FILE_OUT;
pg_dump --section data --column-inserts -t crud_translation >> $FILE_OUT;

pg_dump --section data --column-inserts -t iso8583_router_comm >> $FILE_OUT;
pg_dump --section data --column-inserts -t iso8583_router_message_adapter >> $FILE_OUT;
pg_dump --section data --column-inserts -t iso8583_router_message_adapter_item >> $FILE_OUT;
pg_dump --section data --column-inserts -t iso8583_router_log >> $FILE_OUT;
pg_dump --section data --column-inserts -t iso8583_router_transaction >> $FILE_OUT;
