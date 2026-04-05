# java-filmorate

Template repository for Filmorate project.

## Database schema

![ER diagram](https://github.com/RykhtaYaroslav/java-filmorate/blob/main/Database%20ER%20diagram.png)

The schema includes the following tables:
- **films** — film data with a reference to MPA rating
- **users** — user data
- **genres** — genre reference table
- **mpa_ratings** — MPA rating reference table
- **film_genres** — junction table linking films and genres (many-to-many)
- **film_likes** — junction table linking films and users who liked them (many-to-many)
- **user_friends** — junction table for friendship between users with confirmation status

## Example queries

### Get all films
```sql
SELECT f.*, m.name AS mpa_rating
FROM films f
JOIN mpa_ratings m ON f.rating_id = m.id;
```

### Get film by id
```sql
SELECT f.*, m.name AS mpa_rating
FROM films f
JOIN mpa_ratings m ON f.rating_id = m.id
WHERE f.id = 1;
```

### Get all genres of a film
```sql
SELECT g.name
FROM genres g
JOIN film_genres fg ON g.id = fg.genre_id
WHERE fg.film_id = 1;
```

### Get top N most popular films by likes
```sql
SELECT f.*, COUNT(fl.user_id) AS likes_count
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
GROUP BY f.id
ORDER BY likes_count DESC
LIMIT 10;
```

### Get all users
```sql
SELECT * FROM users;
```

### Get user by id
```sql
SELECT * FROM users WHERE id = 1;
```

### Get friends of a user
```sql
SELECT u.*
FROM users u
WHERE u.id IN (
    SELECT to_user_id FROM user_friends WHERE from_user_id = 1
    UNION
    SELECT from_user_id FROM user_friends WHERE to_user_id = 1
);
```

### Get common friends of two users
```sql
SELECT u.*
FROM users u
WHERE u.id IN (
    SELECT to_user_id FROM user_friends WHERE from_user_id = 1
    UNION
    SELECT from_user_id FROM user_friends WHERE to_user_id = 1
)
AND u.id IN (
    SELECT to_user_id FROM user_friends WHERE from_user_id = 2
    UNION
    SELECT from_user_id FROM user_friends WHERE to_user_id = 2
);
```

### Add like
```sql
INSERT INTO film_likes (film_id, user_id) VALUES (1, 1);
```

### Remove like
```sql
DELETE FROM film_likes WHERE film_id = 1 AND user_id = 1;
```

### Add friend request
```sql
INSERT INTO user_friends (from_user_id, to_user_id, status)
VALUES (1, 2, 'UNCONFIRMED');
```

### Confirm friendship
```sql
UPDATE user_friends SET status = 'CONFIRMED'
WHERE from_user_id = 1 AND to_user_id = 2;
```

### Remove friend
```sql
DELETE FROM user_friends
WHERE (from_user_id = 1 AND to_user_id = 2)
   OR (from_user_id = 2 AND to_user_id = 1);
```
