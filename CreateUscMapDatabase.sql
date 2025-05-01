DROP DATABASE IF EXISTS trojanMapsDB;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Main database
CREATE DATABASE trojanMapsDB;
USE trojanMapsDB;

-- -- Drop tables in any order
-- DROP TABLE IF EXISTS userPins;
-- DROP TABLE IF EXISTS userInfo;
-- DROP TABLE IF EXISTS locations;
-- DROP TABLE IF EXISTS googleMapKey;
-- DROP TABLE IF EXISTS userFriendGroup;

-- Enable foreign key checks again
SET FOREIGN_KEY_CHECKS = 1;

-- Now recreate your tables
CREATE TABLE userInfo(
    userID int auto_increment primary key,
    username varchar(256),
    email varchar(256),
    password varchar(256),
    firstName varchar(256),
    lastName varchar(256),
    phoneNumber varchar(20),
    latitude decimal(9,6),
    longitude decimal(9,6)
);

CREATE TABLE locations(
    locationID int auto_increment primary key,
    name varchar(256),
    category varchar(256),
    address varchar(256),
    latitude decimal(9,6),
    longitude decimal(9,6)
);

CREATE TABLE userPins(
    pinId int auto_increment primary key,
    userID int,
    locationID int,
    foreign key (userID) references userInfo(userID),
    foreign key (locationID) references locations(locationID)
);

CREATE TABLE googleMapKey(
    keyID int auto_increment primary key,
    keyVal varchar(512)
);

CREATE TABLE userFriendGroup(
    userID int,
    groupID int,
    userVisible BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (userID, groupID),
    FOREIGN KEY (userID) REFERENCES userInfo(userID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Don't forget to quote the string
INSERT INTO googleMapKey (keyVal) 
VALUES ('AIzaSyDGIPlUnED-AeiAjI7oZaQm6WDPzpSpMPg');