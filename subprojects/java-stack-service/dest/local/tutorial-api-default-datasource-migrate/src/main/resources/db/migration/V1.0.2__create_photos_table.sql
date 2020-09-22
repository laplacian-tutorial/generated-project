/**
 * The table of photos.
 */
CREATE TABLE t_photo (
    id INTEGER NOT NULL,
    album_id INTEGER NOT NULL,
    title VARCHAR(4096) NOT NULL,
    url VARCHAR(200) NOT NULL,
    thumbnail_url VARCHAR(200) NOT NULL,
    date_taken VARCHAR(200) NOT NULL,
    PRIMARY KEY (id)
);