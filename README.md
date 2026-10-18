# ShopEase — Corrected Version

This version connects the frontend to the Spring Boot + MySQL backend.

## Before running

1. Keep your existing `ecommerce_management` MySQL database and tables.
2. Open `backend/src/main/resources/application.properties`.
3. Replace `YOUR_MYSQL_PASSWORD` with your MySQL root password.
4. Start the backend on port 8080.
5. Serve the project folder through VS Code Live Server (do not open the HTML files directly with `file://`).

## Main API
- POST `/api/auth/register`
- POST `/api/auth/login`
- GET/POST/PUT/DELETE `/api/products`
- GET/POST/PUT/DELETE `/api/categories`
- GET/PUT `/api/customers`
- POST `/api/orders`
- GET `/api/orders/customer/{customerId}`
- GET `/api/orders/admin`
- PUT `/api/orders/{id}/status`
- GET `/api/payments`
- PUT `/api/payments/{id}/status`

## Important
The browser still uses localStorage only for the temporary cart and logged-in session. Business data is now stored in MySQL.
