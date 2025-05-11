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
    FOREIGN KEY (userID) references userInfo(userID) ON DELETE CASCADE
);

INSERT INTO googleMapKey (keyVal) 
    VALUES ('AIzaSyDGIPlUnED-AeiAjI7oZaQm6WDPzpSpMPg');

-- Insert USC campus locations
INSERT INTO locations (name, latitude, longitude) VALUES
    ('Century Apartments', 34.025991631840185, -118.28939683591348),
    ('Jefferson Boulevard Structure', 34.025276152811166, -118.28982775579888),
    ('McClintock Theatre', 34.02502501322721, -118.28732226740752),
    ('Lyons Recreation Center (LRC)', 34.024422747587444, -118.28837651125075),
    ('Webb Tower (WTO)', 34.024599846409174, -118.28758597485078),
    ('Jefferson Building (JEF)', 34.02487535537206, -118.2871131945148),
    ('Norris Dental Center (DEN)', 34.023845606174625, -118.28622056228284),
    ('Glorya Kaufman International Dance Center (KDC)', 34.02349002815275, -118.28515775581941),
    ('University Religious Center (URC)', 34.02324597823232, -118.28481362970315),
    ('College House (CLH)', 34.02318630530172, -118.28457212596095),
    ('Dramatic Arts Building (DAB)', 34.02319120591172, -118.28423509522804),
    ('Ahn House (AHN)', 34.022929512878974, -118.28411092601362),
    ('Joint Educational Project House (JEP)', 34.02286805592788, -118.28396053326968),
    ('Michelson Hall (MCB)', 34.02171552493226, -118.28953026397654),
    ('Seeley G. Mudd Building (SGM)', 34.02134463713598, -118.28905099349416),
    ('Grace Ford Salvatori Hall (GFS)', 34.02132632250171, -118.2880272807592),
    ('USC Bookstores (BKS)', 34.02067075999858, -118.28652232741061),
    ('Ronald Tutor Campus Center (TCC)', 34.02038623659843, -118.28628889122761),
    ('Salvatori Computer Science Center (SAL)', 34.01956679739418, -118.28942598636931),
    ('Ronald Tutor Hall (RTH)', 34.02014464323872, -118.28974431847362),
    ('Fertitta Hall (JFF)', 34.01877743270321, -118.2824155414839),
    ('USC Village', 34.02509591457999, -118.28416796795764),
    ('Leavey Library (LVL)', 34.02194929151205, -118.2829079826223),
    ('North Residential College (NRC)', 34.021095430552066, -118.28142109204249),
    ('Doheny Memorial Library (DML)', 34.02044877326751, -118.28380070745195),
    ('Galen Center (GEC)', 34.02126578033979, -118.28019541136307),
    ('Hoffman Hall (HOH)', 34.018789287992426, -118.28525424726628),
    ('Bovard Administration Building (ADM)', 34.021087113036856, -118.28554269133816),
    ('Student Union (STU)', 34.020309244341526, -118.28564280906507),
    ('Taper Hall (THH)', 34.022441844176164, -118.28449674838684);