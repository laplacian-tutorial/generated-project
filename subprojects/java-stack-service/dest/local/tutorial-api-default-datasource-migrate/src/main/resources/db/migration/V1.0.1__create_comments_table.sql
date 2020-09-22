/**
 * The table of comments.
 */
CREATE TABLE t_comment (
    post_id INTEGER NOT NULL,
    seq_number INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL,
    body VARCHAR(4096) NOT NULL,
    PRIMARY KEY (post_id, seq_number)
);