(function () {
    const form = document.getElementById('loginForm');
    if (!form) return;

    const emailEl = document.getElementById('email')
        || document.getElementById('username')
        || form.querySelector('input[name="email"]')
        || form.querySelector('input[name="username"]')
        || form.querySelector('input[type="email"]');

    const passEl = document.getElementById('password')
        || form.querySelector('input[name="password"]')
        || form.querySelector('input[type="password"]');

    const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');

    const toggleBtn = document.getElementById('togglePassword') || document.getElementById('togglePass');
    const toggleIcon = document.getElementById('toggleIcon');

    if (toggleBtn && passEl) {
        toggleBtn.addEventListener('click', () => {
            const isPassword = passEl.type === 'password';
            passEl.type = isPassword ? 'text' : 'password';
            toggleBtn.setAttribute('aria-label', isPassword ? 'Ocultar senha' : 'Mostrar senha');
            if (toggleIcon) {
                toggleIcon.className = isPassword ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
            }
        });
    }

    form.addEventListener('submit', () => {
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.classList.add('is-loading');
        }

        if (emailEl) {
            emailEl.readOnly = true;
        }

        if (passEl) {
            passEl.readOnly = true;
        }
    });
})();

