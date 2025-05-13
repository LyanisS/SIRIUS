CREATE TABLE train (
    train_id INT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE station (
    station_name VARCHAR(50) PRIMARY KEY,
    station_sort INT NOT NULL UNIQUE
);

CREATE TABLE alert_gravity (
    alert_gravity_type VARCHAR(50) PRIMARY KEY
);

CREATE TABLE role (
    role_name VARCHAR(50) PRIMARY KEY
);

CREATE TABLE trip (
    trip_id INT PRIMARY KEY,
    train_id INT NOT NULL REFERENCES train(train_id)
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

INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Info');
INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Avertissement');
INSERT INTO alert_gravity (alert_gravity_type) VALUES ('Critique');

INSERT INTO station (station_name, station_sort) VALUES ('POSE', 1);
INSERT INTO station (station_name, station_sort) VALUES ('JASM', 2);
INSERT INTO station (station_name, station_sort) VALUES ('TROC', 3);
INSERT INTO station (station_name, station_sort) VALUES ('BONO', 4);
INSERT INTO station (station_name, station_sort) VALUES ('STSD', 5);
INSERT INTO station (station_name, station_sort) VALUES ('NATN', 6);
INSERT INTO station (station_name, station_sort) VALUES ('MAMO', 7);
