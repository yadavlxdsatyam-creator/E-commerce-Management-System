const API_BASE = "http://localhost:8080/api";

async function apiFetch(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    let data = null;
    const text = await response.text();
    if (text) {
        try { data = JSON.parse(text); }
        catch { data = text; }
    }

    if (!response.ok) {
        throw new Error(data?.message || data || "Request failed");
    }

    return data;
}

function getLoggedInUser() {
    try { return JSON.parse(localStorage.getItem("loggedInUser")); }
    catch { return null; }
}

function setLoggedInUser(user) {
    localStorage.setItem("loggedInUser", JSON.stringify(user));
}

function logout() {
    localStorage.removeItem("loggedInUser");
    window.location.href = "index.html";
}

function updateNavbar() {
    const nav = document.querySelector(".nav-links");
    if (!nav) return;

    const user = getLoggedInUser();
    const loginLink = nav.querySelector('a[data-login-link]');

    if (!loginLink) return;

    if (user) {
        loginLink.textContent = `Hello, ${user.name}`;
        loginLink.href = "#";
        loginLink.onclick = (e) => {
            e.preventDefault();
            logout();
        };
    } else {
        loginLink.textContent = "Login";
        loginLink.href = "login.html";
        loginLink.onclick = null;
    }
}

function requireLogin() {
    const user = getLoggedInUser();
    if (!user) {
        alert("Please login first.");
        window.location.href = "login.html";
        return null;
    }
    return user;
}

async function loadProducts() {
    const container = document.getElementById("productContainer");
    if (!container) return;

    try {
        const products = await apiFetch("/products");
        window.allProducts = products;
        renderProducts(products);
    } catch (error) {
        container.innerHTML = `<p class="error-message">${error.message}</p>`;
    }
}

function renderProducts(products) {
    const container = document.getElementById("productContainer");
    if (!container) return;

    if (!products.length) {
        container.innerHTML = "<p>No products found.</p>";
        return;
    }

    container.innerHTML = products.map(product => `
        <div class="product-card">
            ${product.imageUrl ? `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.productName)}">` : ""}
            <h3>${escapeHtml(product.productName)}</h3>
            <p class="category">${escapeHtml(product.categoryName || "")}</p>
            <p class="price">₹${Number(product.price).toLocaleString("en-IN")}</p>
            <p class="stock">${product.stockQuantity > 0 ? `Stock: ${product.stockQuantity}` : "Out of stock"}</p>
            <button onclick="viewProduct(${product.productId})">View Details</button>
        </div>
    `).join("");
}

window.viewProduct = function (id) {
    window.location.href = `product-details.html?id=${id}`;
};

function setupProductFilters() {
    const search = document.getElementById("searchInput");
    const category = document.getElementById("categoryFilter");

    if (!search || !category) return;

    const filter = () => {
        const text = search.value.trim().toLowerCase();
        const selected = category.value;

        const filtered = (window.allProducts || []).filter(product =>
            product.productName.toLowerCase().includes(text) &&
            (selected === "all" || product.categoryName === selected)
        );

        renderProducts(filtered);
    };

    search.addEventListener("input", filter);
    category.addEventListener("change", filter);
}

async function loadProductDetails() {
    const name = document.getElementById("productName");
    if (!name) return;

    const id = Number(new URLSearchParams(location.search).get("id"));
    if (!id) {
        name.textContent = "Product not found";
        return;
    }

    try {
        const product = await apiFetch(`/products/${id}`);
        window.selectedProduct = product;

        document.getElementById("productCategory").textContent = product.categoryName || "";
        document.getElementById("productName").textContent = product.productName;
        document.getElementById("productPrice").textContent =
            `₹${Number(product.price).toLocaleString("en-IN")}`;
        document.getElementById("productStock").textContent =
            product.stockQuantity > 0 ? `Stock: ${product.stockQuantity}` : "Out of stock";
        document.getElementById("productDescription").textContent =
            product.description || "No description available.";

      const image = document.getElementById("productImage");
const placeholder = document.querySelector(".image-placeholder"); // Target the placeholder

if (image && product.imageUrl) {
    image.src = product.imageUrl;
    image.alt = product.productName;
    image.style.display = "block"; // Make the real image visible
    
    if (placeholder) {
        placeholder.style.display = "none"; // Hide the text placeholder
    }
}

    } catch (error) {
        name.textContent = error.message;
    }
}

window.addToCart = function () {
    const product = window.selectedProduct;
    const input = document.getElementById("quantity");

    if (!product || !input) return;

    const quantity = Number(input.value);

    if (!Number.isInteger(quantity) || quantity < 1) {
        alert("Enter a valid quantity.");
        return;
    }

    if (product.stockQuantity <= 0) {
        alert("This product is out of stock.");
        return;
    }

    const cart = JSON.parse(localStorage.getItem("cart") || "[]");
    const existing = cart.find(item => item.productId === product.productId);
    const newQuantity = (existing?.quantity || 0) + quantity;

    if (newQuantity > product.stockQuantity) {
        alert("You cannot add more than the available stock.");
        return;
    }

    if (existing) existing.quantity = newQuantity;
    else cart.push({
        productId: product.productId,
        productName: product.productName,
        price: Number(product.price),
        quantity
    });

    localStorage.setItem("cart", JSON.stringify(cart));
    alert("Product added to cart.");
};

function getCart() {
    return JSON.parse(localStorage.getItem("cart") || "[]");
}

function saveCart(cart) {
    localStorage.setItem("cart", JSON.stringify(cart));
}

async function displayCart() {
    const container = document.getElementById("cartContainer");
    if (!container) return;

    const cart = getCart();
    const products = await apiFetch("/products");
    const productMap = new Map(products.map(p => [p.productId, p]));

    // Remove invalid/deleted products and cap quantities to current stock.
    const validCart = cart
        .filter(item => productMap.has(item.productId))
        .map(item => {
            const product = productMap.get(item.productId);
            return {
                ...item,
                price: Number(product.price),
                productName: product.productName,
                quantity: Math.min(item.quantity, product.stockQuantity)
            };
        })
        .filter(item => item.quantity > 0);

    saveCart(validCart);

    if (!validCart.length) {
        container.innerHTML = "<p>Your cart is empty.</p>";
        document.getElementById("cartTotal").textContent = "0";
        return;
    }

    let total = 0;

    container.innerHTML = validCart.map(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;

        return `
            <div class="cart-item">
                <h3>${escapeHtml(item.productName)}</h3>
                <p>Price: ₹${item.price.toLocaleString("en-IN")}</p>
                <div class="cart-quantity">
                    <button onclick="changeQuantity(${item.productId}, -1)">−</button>
                    <span>${item.quantity}</span>
                    <button onclick="changeQuantity(${item.productId}, 1)">+</button>
                </div>
                <p>Item Total: ₹${itemTotal.toLocaleString("en-IN")}</p>
                <button class="remove-button" onclick="removeFromCart(${item.productId})">Remove</button>
            </div>
        `;
    }).join("");

    document.getElementById("cartTotal").textContent = total.toLocaleString("en-IN");
}

window.changeQuantity = async function (productId, change) {
    const products = await apiFetch("/products");
    const product = products.find(p => p.productId === productId);
    const cart = getCart();
    const item = cart.find(i => i.productId === productId);

    if (!item || !product) return;

    const newQuantity = item.quantity + change;

    if (newQuantity <= 0) {
        removeFromCart(productId);
        return;
    }

    if (newQuantity > product.stockQuantity) {
        alert("Available stock limit reached.");
        return;
    }

    item.quantity = newQuantity;
    saveCart(cart);
    displayCart();
};

window.removeFromCart = function (productId) {
    saveCart(getCart().filter(item => item.productId !== productId));
    displayCart();
};

function setupCheckoutButton() {
    const button = document.getElementById("checkoutButton");
    if (!button) return;

    button.addEventListener("click", () => {
        if (!getCart().length) {
            alert("Your cart is empty.");
            return;
        }
        if (!requireLogin()) return;
        window.location.href = "checkout.html";
    });
}

async function loadCheckout() {
    const summary = document.getElementById("checkoutSummary");
    if (!summary) return;

    const user = requireLogin();
    if (!user) return;

    const cart = getCart();
    if (!cart.length) {
        summary.innerHTML = "<p>Your cart is empty.</p>";
        document.getElementById("placeOrderButton").disabled = true;
        return;
    }

    try {
        const customer = await apiFetch(`/customers/${user.customerId}`);

        document.getElementById("customerName").value = customer.name || "";
        document.getElementById("customerEmail").value = customer.email || "";
        document.getElementById("customerPhone").value = customer.phone || "";
        document.getElementById("address").value = customer.address || "";
        document.getElementById("city").value = customer.city || "";
        document.getElementById("state").value = customer.state || "";
        document.getElementById("pincode").value = customer.pincode || "";

        let total = 0;
        summary.innerHTML = cart.map(item => {
            const amount = item.price * item.quantity;
            total += amount;
            return `<div class="checkout-item">
                <p><strong>${escapeHtml(item.productName)}</strong> × ${item.quantity} — ₹${amount.toLocaleString("en-IN")}</p>
            </div>`;
        }).join("") + `<h3>Total: ₹${total.toLocaleString("en-IN")}</h3>`;
    } catch (error) {
        alert(error.message);
    }
}

async function placeOrder() {
    const user = requireLogin();
    if (!user) return;

    const payment = document.querySelector('input[name="paymentMethod"]:checked');
    const address = document.getElementById("address").value.trim();
    const city = document.getElementById("city").value.trim();
    const state = document.getElementById("state").value.trim();
    const pincode = document.getElementById("pincode").value.trim();

    if (!address || !city || !state || !pincode) {
        alert("Please fill in the complete delivery address.");
        return;
    }

    if (!/^[0-9]{6}$/.test(pincode)) {
        alert("Enter a valid 6-digit pincode.");
        return;
    }

    if (!payment) {
        alert("Please select a payment method.");
        return;
    }

    const cart = getCart();
    if (!cart.length) {
        alert("Your cart is empty.");
        return;
    }

    try {
        const customer = await apiFetch(`/customers/${user.customerId}`);

        await apiFetch(`/customers/${user.customerId}`, {
            method: "PUT",
            body: JSON.stringify({
                customerId: user.customerId,
                userId: customer.userId,
                name: customer.name,
                email: customer.email,
                phone: customer.phone,
                address,
                city,
                state,
                pincode
            })
        });

        const result = await apiFetch("/orders", {
            method: "POST",
            body: JSON.stringify({
                customerId: user.customerId,
                paymentMethod: payment.value,
                items: cart.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity
                }))
            })
        });

        localStorage.removeItem("cart");
        alert(`Order #${result.orderId} placed successfully.`);
        window.location.href = "my-orders.html";
    } catch (error) {
        alert(error.message);
        // Stock may have changed between adding to cart and checkout.
        if (error.message.toLowerCase().includes("stock")) {
            displayCart();
        }
    }
}

async function loadMyOrders() {
    const container = document.getElementById("ordersContainer");
    if (!container) return;

    const user = requireLogin();
    if (!user) return;

    try {
        const orders = await apiFetch(`/orders/customer/${user.customerId}`);

        if (!orders.length) {
            container.innerHTML = "<p>No orders found.</p>";
            return;
        }

        container.innerHTML = orders.map(({order, items}) => `
            <div class="order-card">
                <h2>Order #${order.orderId}</h2>
                <p><strong>Status:</strong> ${escapeHtml(order.status)}</p>
                <p><strong>Date:</strong> ${new Date(order.orderDate).toLocaleString("en-IN")}</p>
                <h3>Items</h3>
                ${items.map(item => `
                    <div class="order-item">
                        <p><strong>${escapeHtml(item.productName)}</strong> × ${item.quantity}</p>
                        <p>₹${Number(item.price).toLocaleString("en-IN")} each</p>
                    </div>
                `).join("")}
                <p class="order-total">Total: ₹${Number(order.totalAmount).toLocaleString("en-IN")}</p>
            </div>
        `).join("");
    } catch (error) {
        container.innerHTML = `<p class="error-message">${error.message}</p>`;
    }
}

async function loginUser(event) {
    event.preventDefault();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    if (!email || !password) {
        alert("Please enter email and password.");
        return;
    }

    try {
        const user = await apiFetch("/auth/login", {
            method: "POST",
            body: JSON.stringify({email, password})
        });

        setLoggedInUser(user);
        alert("Login successful.");

        if (user.role === "ADMIN") {
            window.location.href = "admin/dashboard.html";
        } else {
            window.location.href = "index.html";
        }
    } catch (error) {
        alert(error.message);
    }
}

async function registerUser(event) {
    event.preventDefault();

    const name = document.getElementById("registerName").value.trim();
    const email = document.getElementById("registerEmail").value.trim();
    const phone = document.getElementById("registerPhone").value.trim();
    const password = document.getElementById("registerPassword").value;
    const confirm = document.getElementById("confirmPassword").value;

    if (!name || !email || !phone || !password || !confirm) {
        alert("Please fill all fields.");
        return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        alert("Enter a valid email address.");
        return;
    }

    if (!/^[0-9]{10}$/.test(phone)) {
        alert("Enter a valid 10-digit phone number.");
        return;
    }

    if (password.length < 6) {
        alert("Password must be at least 6 characters.");
        return;
    }

    if (password !== confirm) {
        alert("Passwords do not match.");
        return;
    }

    try {
        await apiFetch("/auth/register", {
            method: "POST",
            body: JSON.stringify({name, email, phone, password})
        });

        alert("Registration successful. Please login.");
        window.location.href = "login.html";
    } catch (error) {
        alert(error.message);
    }
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

document.addEventListener("DOMContentLoaded", () => {
    updateNavbar();
    setupProductFilters();
    setupCheckoutButton();

    loadProducts();
    loadProductDetails();
    displayCart();
    loadCheckout();
    loadMyOrders();

    const loginForm = document.getElementById("loginForm");
    if (loginForm) loginForm.addEventListener("submit", loginUser);

    const registerForm = document.getElementById("registerForm");
    if (registerForm) registerForm.addEventListener("submit", registerUser);

    const placeOrderButton = document.getElementById("placeOrderButton");
    if (placeOrderButton) placeOrderButton.addEventListener("click", placeOrder);
});
