CREATE DATABASE flightDB;

USE flightDB;

CREATE TABLE flights(
    flightNo INT PRIMARY KEY,
    source VARCHAR(50),
    destination VARCHAR(50),
    seats INT,
    price DOUBLE
);