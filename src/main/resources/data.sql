MERGE INTO mpa_ratings (id, name) KEY(id) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

MERGE INTO genres (id, name) KEY(id) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

MERGE INTO types (id, name) KEY(id) VALUES
(1, 'LIKE'),
(2, 'REVIEW'),
(3, 'FRIEND');

MERGE INTO operations (id, name) KEY(id) VALUES
(1, 'ADD'),
(2, 'REMOVE'),
(3, 'UPDATE');