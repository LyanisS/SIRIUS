CREATE DATABASE pcc;

-- drop all tables
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- give access to postgres
GRANT ALL ON SCHEMA public TO test;
GRANT ALL ON SCHEMA public TO public;

-- create table

CREATE TABLE IF NOT EXISTS sample (
    id_sample SERIAL PRIMARY KEY,
    date_sample DATE NOT NULL,
    string_sample VARCHAR(50),
    float_sample FLOAT
);

INSERT INTO sample (date_sample, string_sample, float_sample)
VALUES
('2017-03-14', 3, 2),
('2018-03-14', 2, 3),
('2017-03-14', 1, 4),
('2019-03-14', 4, 1)