The error `Syntax error: relation "users" does not exist` indicates that the `users` table has not been created yet in your database. You need to create the `users` table before attempting to insert data into it.

Here's how you can fix the issue:

### Step 1: Create the `users` Table

First, define the structure of the `users` table. You can create it using a SQL command like this:

```sql
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email TEXT NOT NULL,
  name TEXT NOT NULL,
  username TEXT NOT NULL
);
```

This will create the `users` table with the required columns: `id`, `email`, `name`, and `username`.

### Step 2: Insert Data into the `users` Table

After creating the `users` table, you can insert data using your original SQL query:

```sql
INSERT INTO users (id, email, name, username) 
VALUES (1, 'me@site.com', 'Me', 'username');
```

### Full Example

Here’s a complete SQL block that handles both the `status` enum creation and the `users` table creation, followed by the data insertion:

```sql
-- Create status enum type if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'status') THEN
        CREATE TYPE status AS ENUM ('active', 'inactive', 'archived');
    END IF;
END $$;

-- Create the users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  email TEXT NOT NULL,
  name TEXT NOT NULL,
  username TEXT NOT NULL
);

-- Insert data into the users table
INSERT INTO users (id, email, name, username) 
VALUES (1, 'me@site.com', 'Me', 'username');
```

This will ensure that both the `status` enum and the `users` table are created before attempting to insert data.