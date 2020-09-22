/**
 * The table of albums.
 */
CREATE TABLE t_album (
    id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    title VARCHAR(4096) NOT NULL,
    PRIMARY KEY (id)
);