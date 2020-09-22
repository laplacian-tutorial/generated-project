/**
 * The table of users.
 */
CREATE TABLE t_user (
    id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    username VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    phone VARCHAR(200),
    website VARCHAR(200),
    PRIMARY KEY (id)
);



/**
 * The table of addresses.
 */
CREATE TABLE t_address (
    user_id INTEGER NOT NULL,
    street VARCHAR(200) NOT NULL,
    suite VARCHAR(200) NOT NULL,
    city VARCHAR(200) NOT NULL,
    zipcode VARCHAR(200) NOT NULL,
    latitude VARCHAR(200) NOT NULL,
    longitude VARCHAR(200) NOT NULL,
    PRIMARY KEY (user_id)
);



/**
 * The table of companies.
 */
CREATE TABLE t_company (
    user_id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    catch_phrase VARCHAR(200),
    bs VARCHAR(200),
    PRIMARY KEY (user_id)
);