-- V2__add_version_to_customer.sql

ALTER TABLE customer
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;