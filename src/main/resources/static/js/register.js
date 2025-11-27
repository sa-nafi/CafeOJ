// Get CSRF token from cookie
function getCsrfToken() {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookies = decodedCookie.split(';');
    for (let cookie of cookies) {
        cookie = cookie.trim();
        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length);
        }
    }
    return null;
}

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    const messageDiv = document.getElementById('message');

    try {
        const csrfToken = getCsrfToken();
        const headers = {
            'Content-Type': 'application/json',
        };
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ username, name, email, password })
        });

        const data = await response.json();

        if (response.ok) {
            messageDiv.className = 'message success';
            messageDiv.textContent = data.message;
            messageDiv.style.display = 'block';
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            messageDiv.className = 'message error';
            messageDiv.textContent = data.error;
            messageDiv.style.display = 'block';
        }
    } catch (error) {
        messageDiv.className = 'message error';
        messageDiv.textContent = 'An error occurred. Please try again.';
        messageDiv.style.display = 'block';
    }
});
