The error `duplicate key value violates unique constraint "users_pkey"` occurs because you're trying to insert a row with a `PRIMARY KEY` (`id`) value that already exists in the `users` table. Since the `id` is defined as `SERIAL PRIMARY KEY`, it must be unique.

Here are a few options to resolve the issue:

### Option 1: Allow the Database to Automatically Increment the ID
Since `id` is defined as `SERIAL`, it will automatically increment if you don't specify a value for it. You can let the database handle the `id` and skip providing the value in your `INSERT` query:

```sql
INSERT INTO users (email, name, username) 
VALUES ('me@site.com', 'Me', 'username');
```

This will insert the data and automatically assign a new unique `id` to the row.

### Option 2: Use a Different ID Value
If you still want to manually specify the `id`, make sure you're providing a value that doesn't already exist in the table. For example:

```sql
INSERT INTO users (id, email, name, username) 
VALUES (2, 'me@site.com', 'Me', 'username');
```

Ensure that the `id` value (`2` in this case) is unique and doesn't conflict with existing entries.

### Option 3: Use `ON CONFLICT` to Handle Duplicates
If you want to handle the conflict gracefully and update existing rows or skip the insert, you can use the `ON CONFLICT` clause. For example, if an entry with the same `id` already exists, you can choose to skip the insert:

```sql
INSERT INTO users (id, email, name, username) 
VALUES (1, 'me@site.com', 'Me', 'username')
ON CONFLICT (id) DO NOTHING;
```

Or you can update the existing row:

```sql
INSERT INTO users (id, email, name, username) 
VALUES (1, 'me@site.com', 'Me', 'username')
ON CONFLICT (id) 
DO UPDATE SET email = EXCLUDED.email, name = EXCLUDED.name, username = EXCLUDED.username;
```

This will update the `email`, `name`, and `username` of the row if there's already a conflict with the `id`.

### What to Do:
1. If you don't need to specify the `id`, **use Option 1** (automatic `id` generation).
2. If you want to manually specify `id`, ensure it's unique or use **Option 2**.
3. If you need to handle conflicts, go with **Option 3**.