--
-- Name: students; Type: TABLE; Schema: ezip_ing1
--

CREATE TABLE students (
    name varchar(64) NOT NULL,
    firstname varchar(64) NOT NULL,
    groupname varchar(8) NOT NULL,
    id int(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
);

INSERT INTO students (name, firstname, groupname) VALUES ("ANONYMOUS", "Stephane", "Admin");