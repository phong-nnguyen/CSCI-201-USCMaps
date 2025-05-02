DROP DATABASE IF EXISTS trojanMapsDB;

-- Main database
CREATE DATABASE trojanMapsDB;
USE trojanMapsDB;

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

CREATE TABLE friendGroups (
    groupID INT AUTO_INCREMENT PRIMARY KEY,
    groupName VARCHAR(256)
);

CREATE TABLE userFriendGroup(
    groupID INT,
    userID INT,
    userVisible BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (groupID) REFERENCES friendGroups(groupID) ON DELETE CASCADE,
    FOREIGN KEY (userID) REFERENCES userInfo(userID) ON DELETE CASCADE
);

INSERT INTO googleMapKey (keyVal) 
VALUES ('AIzaSyDGIPlUnED-AeiAjI7oZaQm6WDPzpSpMPg');
