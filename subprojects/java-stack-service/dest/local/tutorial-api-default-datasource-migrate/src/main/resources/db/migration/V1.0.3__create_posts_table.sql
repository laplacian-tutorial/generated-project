/**
 * The table of posts.
 */
CREATE TABLE t_post (
    id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    title VARCHAR(4096) NOT NULL,
    body VARCHAR(4096) NOT NULL,
    PRIMARY KEY (id)
);