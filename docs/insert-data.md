### Step 1: Insert Products into the Database
You can directly insert products into the `products` table using an SQL `INSERT` query. Make sure the fields match the structure of your `products` table.

Here’s an example query to insert a product:

```sql
INSERT INTO products (image_url, name, status, price, stock, available_at) 
VALUES (
  'https://example.com/image.png', -- image_url
  'Sample Product',                -- name
  'active',                        -- status (must match one of the ENUM values: 'active', 'inactive', 'archived')
  29.99,                           -- price (NUMERIC)
  100,                             -- stock (INTEGER)
  NOW()                            -- available_at (TIMESTAMP, use the current time or a specific time)
);
```

You can run this query in your Postgres database to add a new product. Make sure to adjust the `image_url`, `name`, `status`, `price`, `stock`, and `available_at` to the actual product details.

### Step 2: Seed Products Using an API (Optional)
If your application has an API for adding products (e.g., `/api/products`), you can make a POST request to it to add products programmatically. 

Here’s an example using an HTTP request in JavaScript (using `fetch`):

```javascript
fetch('http://localhost:3000/api/products', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    image_url: 'https://example.com/image.png',
    name: 'Sample Product',
    status: 'active',
    price: 29.99,
    stock: 100,
    available_at: new Date().toISOString()
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

Make sure your API is running and can handle product creation.

### Step 3: Verify Data in Your Product Dashboard
Once products have been added to the database, your product dashboard (the one showing "Image, Name, Status, Price, Total Sales, Created at") should start displaying the added products. Ensure that:
- You have seeded the database with products.
- Your front-end is correctly fetching and displaying the product data.

If the dashboard is not updating, make sure the data-fetching logic is working as expected.

### Step 4: Updating the Product List in Your Frontend
If you have a dynamic frontend (e.g., using React or another framework), ensure that the product list is being updated from the API or database. For example, you might need to re-fetch the products after adding a new one. Here's an example using React to fetch and display the products:

```javascript
const [products, setProducts] = useState([]);

useEffect(() => {
  fetch('http://localhost:3000/api/products')
    .then(response => response.json())
    .then(data => setProducts(data))
    .catch(error => console.error('Error fetching products:', error));
}, []);

return (
  <div>
    {products.map(product => (
      <div key={product.id}>
        <img src={product.image_url} alt={product.name} />
        <h3>{product.name}</h3>
        <p>Status: {product.status}</p>
        <p>Price: ${product.price}</p>
        <p>Stock: {product.stock}</p>
        <p>Available at: {new Date(product.available_at).toLocaleString()}</p>
      </div>
    ))}
  </div>
);
```

### Final Notes:
- Ensure your database schema matches the fields in your code or queries.
- Make sure your product table’s columns (`image_url`, `name`, `status`, etc.) are correctly populated.
- If you’re using an API, ensure it’s working properly and can accept POST requests to add products.