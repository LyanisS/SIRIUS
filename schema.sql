CREATE TABLE station (
    station_id INT PRIMARY KEY AUTO_INCREMENT,
    station_name VARCHAR(50) NOT NULL
);

CREATE TABLE person (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    person_last_name VARCHAR(50) NOT NULL,
    person_first_name VARCHAR(50) NOT NULL,
    person_login VARCHAR(50) NOT NULL UNIQUE,
    person_password VARCHAR(50) NOT NULL
);

CREATE TABLE track (
    track_id INT PRIMARY KEY AUTO_INCREMENT,
    track_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE alert_gravity (
    alert_gravity_id INT PRIMARY KEY AUTO_INCREMENT,
    alert_gravity_level INT NOT NULL UNIQUE,
    alert_gravity_type VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE
);

CREATE TABLE track_element_type (
    track_element_type_id INT PRIMARY KEY AUTO_INCREMENT,
    track_element_type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE switch_position (
    switch_position_id INT PRIMARY KEY AUTO_INCREMENT,
    switch_position_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE train_status (
    train_status_id INT PRIMARY KEY AUTO_INCREMENT,
    train_status_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE track_element (
    track_element_id INT PRIMARY KEY AUTO_INCREMENT,
    track_element_is_working BOOLEAN NOT NULL,
    switch_position_id INT REFERENCES switch_position(switch_position_id),
    track_element_type_id INT NOT NULL REFERENCES track_element_type(track_element_type_id),
    track_id INT NOT NULL REFERENCES track(track_id),
    station_id INT REFERENCES station(station_id)
);

CREATE TABLE train (
    train_id INT PRIMARY KEY AUTO_INCREMENT,
    train_status_id INT NOT NULL REFERENCES train_status(train_status_id),
    track_element_id INT NOT NULL UNIQUE REFERENCES track_element(track_element_id)
);

CREATE TABLE trip (
    trip_id INT PRIMARY KEY AUTO_INCREMENT,
    train_id INT NOT NULL REFERENCES train(train_id),
    person_id INT NOT NULL REFERENCES person(person_id)
);

CREATE TABLE schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    schedule_timestamp DATETIME NOT NULL,
    schedule_stop BOOLEAN NOT NULL,
    track_element_id INT NOT NULL REFERENCES track_element(track_element_id),
    trip_id INT NOT NULL REFERENCES trip(trip_id)
);

CREATE TABLE alert (
    alert_id INT PRIMARY KEY AUTO_INCREMENT,
    alert_message VARCHAR(255) NOT NULL,
    alert_timestamp DATETIME NOT NULL,
    alert_gravity_id INT NOT NULL REFERENCES alert_gravity(alert_gravity_id),
    train_id INT NOT NULL REFERENCES train(train_id)
);

CREATE TABLE role_membership (
    person_id INT REFERENCES person(person_id),
    role_id INT REFERENCES role(role_id),
    PRIMARY KEY(person_id, role_id)
);