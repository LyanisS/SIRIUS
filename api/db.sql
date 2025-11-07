CREATE DATABASE pcc;

-- drop all tables
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- give access to postgres
GRANT ALL ON SCHEMA public TO test;
GRANT ALL ON SCHEMA public TO public;

-- create table

CREATE TYPE sample_type_enum AS ENUM ('SAMPLE_TYPE1', 'SAMPLE_TYPE2');
CREATE TABLE IF NOT EXISTS sample (
    id_sample SERIAL PRIMARY KEY,
    date_sample DATE NOT NULL,
    string_sample VARCHAR(50),
    float_sample FLOAT,
    sample_type sample_type_enum
);

INSERT INTO sample (date_sample, string_sample, float_sample, sample_type)
VALUES
('2017-03-14', 3, 2, 'SAMPLE_TYPE1'),
('2018-03-14', 2, 3, 'SAMPLE_TYPE2'),
('2017-03-14', 1, 4, 'SAMPLE_TYPE1'),
('2019-03-14', 4, 1, 'SAMPLE_TYPE2')