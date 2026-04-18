(function () {
    const form = document.getElementById('loginForm');
    if (!form) return;

    const emailEl = document.getElementById('email');
    const passEl = document.getElementById('password');
    const submitBtn = document.getElementById('submitBtn');
    const serverAlert = document.getElementById('serverAlert');

    const userField = document.getElementById('userField');
    const passField = document.getElementById('passField');
    const userError = document.getElementById('userError');
    const passError = document.getElementById('passError');
    const capsLockHint = document.getElementById('capsLockHint');

    const toggle = document.getElementById('togglePass');

    const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    function setFieldError(fieldEl, inputEl, msgEl, msg) {
        const hasError = Boolean(msg);
        fieldEl.classList.toggle('is-invalid', hasError);
        if (hasError) {
            inputEl.setAttribute('aria-invalid', 'true');
            msgEl.textContent = msg;
        } else {
            inputEl.removeAttribute('aria-invalid');
            msgEl.textContent = '';
        }
    }

    function validateEmail() {
        const v = (emailEl.value || '').trim();
        if (!v) return 'Informe o usuário.';
        if (!EMAIL_RE.test(v)) return 'Informe um e-mail válido.';
        return '';
    }

    function validatePassword() {
        const v = passEl.value || '';
        if (!v) return 'Informe a senha.';
        if (v.length < 6) return 'A senha deve ter pelo menos 6 caracteres.';
        return '';
    }

    function validateAll() {
        const eMsg = validateEmail();
        const pMsg = validatePassword();
        setFieldError(userField, emailEl, userError, eMsg);
        setFieldError(passField, passEl, passError, pMsg);
        return { eMsg, pMsg };
    }

    emailEl.addEventListener('input', () => setFieldError(userField, emailEl, userError, validateEmail()));
    passEl.addEventListener('input', () => setFieldError(passField, passEl, passError, validatePassword()));

    function updateCapsLockHint(ev) {
        if (!capsLockHint) return;
        const caps = ev.getModifierState && ev.getModifierState('CapsLock');
        capsLockHint.textContent = caps ? 'Caps Lock está ativado.' : '';
    }

    passEl.addEventListener('keydown', updateCapsLockHint);
    passEl.addEventListener('keyup', updateCapsLockHint);
    passEl.addEventListener('blur', () => {
        if (capsLockHint) capsLockHint.textContent = '';
    });

    if (toggle) {
        toggle.addEventListener('click', () => {
            const isHidden = passEl.type === 'password';
            passEl.type = isHidden ? 'text' : 'password';
            toggle.textContent = isHidden ? 'Ocultar' : 'Mostrar';
            toggle.setAttribute('aria-pressed', String(isHidden));
            toggle.setAttribute('aria-label', isHidden ? 'Ocultar senha' : 'Mostrar senha');
        });
    }

    form.addEventListener('submit', (ev) => {
        const { eMsg, pMsg } = validateAll();
        if (eMsg || pMsg) {
            ev.preventDefault();
            if (eMsg) {
                emailEl.focus();
            } else {
                passEl.focus();
            }
            return;
        }

        submitBtn.classList.add('is-loading');
        submitBtn.disabled = true;
    });

    if (serverAlert && typeof serverAlert.focus === 'function') {
        window.requestAnimationFrame(() => serverAlert.focus());
    }
})();

