### 1. Create a Postgres Database on Vercel
- When you deploy the project on Vercel, it will automatically prompt you to **create a new Postgres database**.
- This will set up the necessary environment variables (like `POSTGRES_URL`) in your project.
- Vercel provides a Postgres dashboard where you can manage the database.

### 2. Create a Table in the Database
Once the database is created, follow these steps to set up the schema for your project:

1. Open the **Vercel Postgres Dashboard**.
2. Run the following SQL commands to create a custom data type `status` and the `products` table:
   
   ```sql
   CREATE TYPE status AS ENUM ('active', 'inactive', 'archived');
   
   CREATE TABLE products (
     id SERIAL PRIMARY KEY,
     image_url TEXT NOT NULL,
     name TEXT NOT NULL,
     status status NOT NULL,
     price NUMERIC(10, 2) NOT NULL,
     stock INTEGER NOT NULL,
     available_at TIMESTAMP NOT NULL
   );
   ```

### 3. Seed the Database
- Uncomment the `app/api/seed.ts` file in your project, which will handle adding some initial product data to the database.
- After uncommenting, run the seeding process by accessing `http://localhost:3000/api/seed` in your browser or through an API tool (e.g., Postman). This will populate your database with product entries.

### 4. Configure Environment Variables
- You need to copy the `.env.example` file into a new `.env` file in the root of your project:
  
  ```bash
  cp .env.example .env
  ```

- Open the `.env` file and update the values, particularly for:
  - `POSTGRES_URL`: Your Postgres connection URL (you can get this from the Vercel Postgres dashboard).
  - `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET`: Follow the instructions in the `.env.example` file to set up a **GitHub OAuth application**.

### 5. Install Vercel CLI and Deploy
Install the Vercel CLI globally if you haven't already:

```bash
npm i -g vercel
```

### 6. Link the Project with Vercel
Run the following command to link your local project with the Vercel project:

```bash
vercel link
```

This will guide you through selecting or creating a Vercel project to link with your local repository.

### 7. Pull Environment Variables
After linking, pull the environment variables from Vercel into your local project:

```bash
vercel env pull
```

This will create a `.env` file in your project with all the environment variables from Vercel.

Once these steps are complete, your project will be ready to run locally or deploy with Vercel!