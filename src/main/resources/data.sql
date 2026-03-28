MERGE INTO mpa_ratings (id, name) KEY(id) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

MERGE INTO genres (id, name) KEY(id) VALUES
(1, 'Comedy'),
(2, 'Drama'),
(3, 'Animation'),
(4, 'Thriller'),
(5, 'Documentary'),
(6, 'Action'),
(7, 'Romance'),
(8, 'Horror'),
(9, 'Sci-Fi'),
(10, 'Fantasy'),
(11, 'Adventure'),
(12, 'Crime'),
(13, 'Mystery'),
(14, 'Biography'),
(15, 'History'),
(16, 'Musical'),
(17, 'Western'),
(18, 'Sport'),
(19, 'War'),
(20, 'Family');