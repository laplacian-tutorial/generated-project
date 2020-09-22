/**
 * The table of tasks.
 */
CREATE TABLE t_task (
    id INTEGER NOT NULL,
    user_id INTEGER,
    title VARCHAR(4096) NOT NULL,
    completed INTEGER NOT NULL,
    PRIMARY KEY (id)
);