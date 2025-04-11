CREATE TABLE train (
    train_id INT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE station (
    station_name VARCHAR(50) PRIMARY KEY,
    station_sort INT NOT NULL UNIQUE
);

CREATE TABLE person (
    person_login VARCHAR(50) PRIMARY KEY,
    person_last_name VARCHAR(50) NOT NULL,
    person_first_name VARCHAR(50) NOT NULL,
    person_password VARCHAR(50) NOT NULL
);

CREATE TABLE alert_gravity (
    alert_gravity_type VARCHAR(50) PRIMARY KEY
);

CREATE TABLE role (
    role_name VARCHAR(50) PRIMARY KEY
);

CREATE TABLE trip (
    trip_id INT PRIMARY KEY,
    train_id INT NOT NULL REFERENCES train(train_id),
    person_login VARCHAR(50) NOT NULL REFERENCES person(person_login)
);

CREATE TABLE schedule (
    schedule_id INT PRIMARY KEY,
    schedule_time_arrival TIME NOT NULL,
    schedule_time_departure TIME NOT NULL,
    station_name VARCHAR(50) NOT NULL REFERENCES station(station_name),
    trip_id INT NOT NULL REFERENCES trip(trip_id)
);

CREATE TABLE alert (
    alert_id INT PRIMARY KEY,
    alert_message VARCHAR(255) NOT NULL,
    alert_time TIME NOT NULL,
    alert_duration INT NOT NULL DEFAULT 0,
    alert_gravity_type VARCHAR(50) NOT NULL REFERENCES alert_gravity(alert_gravity_type),
    train_id INT NOT NULL REFERENCES train(train_id)
);

CREATE TABLE role_membership (
    person_id INT REFERENCES person(person_id),
    role_name VARCHAR(50) REFERENCES role(role_name),
    PRIMARY KEY(person_id, role_name)
);

INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Faible');
INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Moyen');
INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Élevé');

INSERT INTO role (role_name) VALUES ('CREG');
INSERT INTO role (role_name) VALUES ('GT');
INSERT INTO role (role_name) VALUES ('Conducteur');

INSERT INTO station (station_name, station_sort) VALUES ('POSE', 1);
INSERT INTO station (station_name, station_sort) VALUES ('JASM', 2);
INSERT INTO station (station_name, station_sort) VALUES ('TROC', 3);
INSERT INTO station (station_name, station_sort) VALUES ('BONO', 4);
INSERT INTO station (station_name, station_sort) VALUES ('STSD', 5);
INSERT INTO station (station_name, station_sort) VALUES ('NATN', 6);
INSERT INTO station (station_name, station_sort) VALUES ('MAMO', 7);
