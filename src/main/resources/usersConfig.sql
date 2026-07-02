CREATE TABLE users(username TEXT UNIQUE, password TEXT, admin BOOLEAN, firstName TEXT, lastName TEXT, birthday DATE, gender CHAR, email TEXT);
INSERT INTO users (username, password, admin, firstName, lastname, birthday, gender, email) VALUES ('AhmetRakap', 'javaDevTest', true, 'Ahmet', 'Rakap', '12-11-2006', 'M', 'ahmet.rakap@metu.edu.tr');
INSERT INTO users (username, password, admin, firstName, lastname, birthday, gender, email) VALUES ('OtherAdmin', 'iAmAdmin', true, 'John', 'Smith', '01-01-1970', 'M', 'johnsmith@example.org');
INSERT INTO users (username, password, admin, firstName, lastname, birthday, gender, email) VALUES ('RandomUser', 'iAmUser', false, 'Sameen', 'Shaw', '06-12-1994', 'F', 'shaw_sameen@cia.gov');
INSERT INTO users (username, password, admin, firstName, lastname, birthday, gender, email) VALUES ('JohnReese', 'iamcia', true, 'John', 'Reese', '02-13-1985', 'M', 'johnreese@cia.gov');
