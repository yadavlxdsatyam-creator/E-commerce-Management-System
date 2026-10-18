const API_BASE = "http://localhost:8080/api";

async function api(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {"Content-Type":"application/json", ...(options.headers || {})}
    });
    const text = await response.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }
    if (!response.ok) throw new Error(data?.message || data || "Request failed");
    return data;
}

function requireAdmin() {
    const user = JSON.parse(localStorage.getItem("loggedInUser") || "null");
    if (!user || user.role !== "ADMIN") {
        alert("Admin login required.");
        window.location.href = "../login.html";
        return false;
    }
    return true;
}

function money(value) {
    return Number(value || 0).toLocaleString("en-IN");
}

function esc(value) {
    return String(value ?? "").replaceAll("&","&amp;").replaceAll("<","&lt;")
        .replaceAll(">","&gt;").replaceAll('"',"&quot;").replaceAll("'","&#039;");
}

let products = [];
let categories = [];
let editingProductId = null;
let editingCategoryId = null;

async function loadProducts() {
    products = await api("/products");
    const body = document.getElementById("adminProductTableBody");
    if (!body) return;

    body.innerHTML = products.length ? products.map(p => `
        <tr>
            <td>${p.productId}</td>
            <td>${p.imageUrl ? `<img src="${esc(p.imageUrl)}" class="admin-product-img" alt="">` : "No image"}</td>
            <td><strong>${esc(p.productName)}</strong></td>
            <td>${esc(p.categoryName)}</td>
            <td>₹${money(p.price)}</td>
            <td>${p.stockQuantity}</td>
            <td>
                <button class="btn-small" onclick="editProduct(${p.productId})">Edit</button>
                <button class="btn-delete" onclick="deleteProduct(${p.productId})">Delete</button>
            </td>
        </tr>
    `).join("") : `<tr><td colspan="7" style="text-align:center">No products found.</td></tr>`;
}

async function loadCategories() {
    categories = await api("/categories");
    const body = document.getElementById("adminCategoryTableBody");
    if (body) {
        body.innerHTML = categories.length ? categories.map(c => `
            <tr>
                <td>${c.categoryId}</td>
                <td><strong>${esc(c.categoryName)}</strong></td>
                <td>${esc(c.description || "")}</td>
                <td>
                    <button class="btn-small" onclick="editCategory(${c.categoryId})">Edit</button>
                    <button class="btn-delete" onclick="deleteCategory(${c.categoryId})">Delete</button>
                </td>
            </tr>
        `).join("") : `<tr><td colspan="4" style="text-align:center">No categories found.</td></tr>`;
    }

    const select = document.getElementById("prodCategory");
    if (select) {
        select.innerHTML = `<option value="">Select Category</option>` +
            categories.map(c => `<option value="${c.categoryId}">${esc(c.categoryName)}</option>`).join("");
    }
}

async function loadCustomers() {
    const body = document.getElementById("adminCustomerTableBody");
    if (!body) return;

    const customers = await api("/customers");
    body.innerHTML = customers.length ? customers.map((c,i) => `
        <tr><td>${i+1}</td><td>${esc(c.name)}</td><td>${esc(c.email)}</td><td>${esc(c.phone)}</td></tr>
    `).join("") : `<tr><td colspan="4" style="text-align:center">No customers found.</td></tr>`;
}

async function loadOrders() {
    const body = document.getElementById("adminOrderTableBody");
    if (!body) return;

    const orders = await api("/orders/admin");
    const payments = await api("/payments");
    const paymentMap = new Map(payments.map(p => [p.orderId, p]));

    body.innerHTML = orders.length ? orders.map(({order}) => {
        const payment = paymentMap.get(order.orderId);
        return `
        <tr>
            <td>#${order.orderId}</td>
            <td>${esc(payment?.paymentMethod || "—")}</td>
            <td>₹${money(order.totalAmount)}</td>
            <td><span class="status-badge-${order.status.toLowerCase()}">${esc(order.status)}</span></td>
            <td>
                <select class="admin-status-select" onchange="updateOrderStatus(${order.orderId}, this.value)">
                    ${["PLACED","CONFIRMED","SHIPPED","DELIVERED","CANCELLED"].map(s =>
                        `<option value="${s}" ${s===order.status?"selected":""}>${s}</option>`).join("")}
                </select>
            </td>
        </tr>`;
    }).join("") : `<tr><td colspan="5" style="text-align:center">No orders found.</td></tr>`;
}

async function loadPayments() {
    const body = document.getElementById("adminPaymentTableBody");
    if (!body) return;

    const payments = await api("/payments");
    body.innerHTML = payments.length ? payments.map(p => `
        <tr>
            <td>TXN${p.paymentId}</td>
            <td>#${p.orderId}</td>
            <td>${esc(p.paymentMethod)}</td>
            <td>₹${money(p.amount)}</td>
            <td><span class="pay-badge-${p.paymentStatus.toLowerCase()}">${p.paymentStatus}</span></td>
            <td>
                <select class="admin-payment-select" onchange="updatePaymentStatus(${p.paymentId}, this.value)">
                    ${["PENDING","SUCCESS","FAILED","REFUNDED"].map(s =>
                        `<option value="${s}" ${s===p.paymentStatus?"selected":""}>${s}</option>`).join("")}
                </select>
            </td>
        </tr>
    `).join("") : `<tr><td colspan="6" style="text-align:center">No payments found.</td></tr>`;
}

async function loadDashboard() {
    const [products, customers, orders] = await Promise.all([
        api("/products"), api("/customers"), api("/orders/admin")
    ]);

    document.getElementById("totalProductsCount")?.replaceChildren(document.createTextNode(products.length));
    document.getElementById("activeCustomersCount")?.replaceChildren(document.createTextNode(customers.length));
    document.getElementById("totalOrdersCount")?.replaceChildren(document.createTextNode(orders.length));

    const sales = orders
        .filter(x => x.order.status !== "CANCELLED")
        .reduce((sum,x) => sum + Number(x.order.totalAmount || 0), 0);

    const salesEl = document.getElementById("totalSales");
    if (salesEl) salesEl.textContent = `₹${money(sales)}`;
}

window.updateOrderStatus = async function(id, status) {
    try {
        await api(`/orders/${id}/status?status=${encodeURIComponent(status)}`, {method:"PUT"});
        alert("Order status updated.");
        loadOrders();
        loadDashboard();
    } catch(e) { alert(e.message); loadOrders(); }
};

window.updatePaymentStatus = async function(id, status) {
    try {
        await api(`/payments/${id}/status?status=${encodeURIComponent(status)}`, {method:"PUT"});
        alert("Payment status updated.");
        loadPayments();
    } catch(e) { alert(e.message); loadPayments(); }
};

function productElements() {
    return {
        modal: document.getElementById("productModal"),
        form: document.getElementById("addProductForm"),
        name: document.getElementById("prodName"),
        category: document.getElementById("prodCategory"),
        price: document.getElementById("prodPrice"),
        stock: document.getElementById("prodStock"),
        image: document.getElementById("prodImageUrl"),
        description: document.getElementById("prodDesc")
    };
}

window.editProduct = function(id) {
    const product = products.find(p => p.productId === id);
    const e = productElements();
    if (!product || !e.modal) return;

    editingProductId = id;
    e.name.value = product.productName;
    e.category.value = product.categoryId;
    e.price.value = product.price;
    e.stock.value = product.stockQuantity;
    e.image.value = product.imageUrl || "";
    e.description.value = product.description || "";
    e.modal.classList.add("active");
    e.modal.querySelector("h2").textContent = "Edit Product";
    e.form.querySelector('button[type="submit"]').textContent = "Update Product";
};

window.deleteProduct = async function(id) {
    if (!confirm("Delete this product?")) return;
    try {
        await api(`/products/${id}`, {method:"DELETE"});
        await loadProducts();
        await loadDashboard();
    } catch(e) { alert(e.message); }
};

window.editCategory = function(id) {
    const category = categories.find(c => c.categoryId === id);
    const modal = document.getElementById("categoryModal");
    if (!category || !modal) return;

    editingCategoryId = id;
    document.getElementById("categoryName").value = category.categoryName;
    document.getElementById("categoryDescription").value = category.description || "";
    modal.classList.add("active");
    modal.querySelector("h2").textContent = "Edit Category";
};

window.deleteCategory = async function(id) {
    if (!confirm("Delete this category?")) return;
    try {
        await api(`/categories/${id}`, {method:"DELETE"});
        await loadCategories();
        await loadProducts();
    } catch(e) { alert(e.message); }
};

function closeProductModal() {
    const e = productElements();
    if (!e.modal) return;
    e.modal.classList.remove("active");
    e.form?.reset();
    editingProductId = null;
    e.modal.querySelector("h2").textContent = "Add New Product";
    e.form.querySelector('button[type="submit"]').textContent = "Save Product";
}

function closeCategoryModal() {
    const modal = document.getElementById("categoryModal");
    if (!modal) return;
    modal.classList.remove("active");
    document.getElementById("categoryForm")?.reset();
    editingCategoryId = null;
    modal.querySelector("h2").textContent = "Add Category";
}

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireAdmin()) return;

    try {
        await Promise.all([
            loadProducts(), loadCategories(), loadCustomers(),
            loadOrders(), loadPayments(), loadDashboard()
        ]);
    } catch (e) {
        alert(e.message);
    }

    const e = productElements();
    document.getElementById("openAddModalBtn")?.addEventListener("click", () => {
        editingProductId = null;
        e.form?.reset();
        e.modal?.classList.add("active");
    });
    document.getElementById("closeModalBtn")?.addEventListener("click", closeProductModal);
    document.getElementById("cancelModalBtn")?.addEventListener("click", closeProductModal);

    e.form?.addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            productName: e.name.value.trim(),
            categoryId: Number(e.category.value),
            price: Number(e.price.value),
            stockQuantity: Number(e.stock.value),
            imageUrl: e.image.value.trim(),
            description: e.description.value.trim()
        };

        if (!payload.productName || !payload.categoryId || payload.price <= 0 ||
            payload.stockQuantity < 0) {
            alert("Please enter valid product details.");
            return;
        }

        try {
            const url = editingProductId ? `/products/${editingProductId}` : "/products";
            await api(url, {
                method: editingProductId ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            closeProductModal();
            await loadProducts();
            await loadDashboard();
            alert(editingProductId ? "Product updated." : "Product added.");
        } catch(e) { alert(e.message); }
    });

    document.getElementById("openCategoryModalBtn")?.addEventListener("click", () => {
        editingCategoryId = null;
        document.getElementById("categoryForm")?.reset();
        document.getElementById("categoryModal")?.classList.add("active");
    });
    document.getElementById("closeCategoryModalBtn")?.addEventListener("click", closeCategoryModal);
    document.getElementById("cancelCategoryModalBtn")?.addEventListener("click", closeCategoryModal);

    document.getElementById("categoryForm")?.addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            categoryName: document.getElementById("categoryName").value.trim(),
            description: document.getElementById("categoryDescription").value.trim()
        };

        if (!payload.categoryName) {
            alert("Category name is required.");
            return;
        }

        try {
            const url = editingCategoryId ? `/categories/${editingCategoryId}` : "/categories";
            await api(url, {
                method: editingCategoryId ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            closeCategoryModal();
            await loadCategories();
            alert(editingCategoryId ? "Category updated." : "Category added.");
        } catch(e) { alert(e.message); }
    });
});
