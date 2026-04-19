(function () {
    const DEFAULT_DURATION = 4200;
    const ROOT_ID = "app-toast-root";

    function getRoot() {
        let root = document.getElementById(ROOT_ID);
        if (root) {
            return root;
        }
        root = document.createElement("div");
        root.id = ROOT_ID;
        root.className = "app-toast-root";
        root.setAttribute("aria-live", "polite");
        root.setAttribute("aria-atomic", "true");
        document.body.appendChild(root);
        return root;
    }

    function titleByType(type) {
        switch ((type || "info").toLowerCase()) {
            case "success":
                return "Sucesso";
            case "error":
                return "Erro";
            case "warning":
                return "Atenção";
            default:
                return "Informação";
        }
    }

    function show(message, type, durationMs) {
        if (!message) {
            return;
        }

        const safeType = (type || "info").toLowerCase();
        const root = getRoot();
        const toast = document.createElement("div");
        toast.className = "app-toast app-toast-" + safeType;

        const header = document.createElement("div");
        header.className = "app-toast-header";

        const title = document.createElement("p");
        title.className = "app-toast-title";
        title.textContent = titleByType(safeType);

        const closeBtn = document.createElement("button");
        closeBtn.className = "app-toast-close";
        closeBtn.type = "button";
        closeBtn.setAttribute("aria-label", "Fechar");
        closeBtn.textContent = "x";

        const body = document.createElement("p");
        body.className = "app-toast-message";
        body.textContent = message;

        header.appendChild(title);
        header.appendChild(closeBtn);
        toast.appendChild(header);
        toast.appendChild(body);
        root.appendChild(toast);

        window.requestAnimationFrame(function () {
            toast.classList.add("is-visible");
        });

        function removeToast() {
            toast.classList.remove("is-visible");
            window.setTimeout(function () {
                toast.remove();
            }, 180);
        }

        closeBtn.addEventListener("click", removeToast);
        window.setTimeout(removeToast, durationMs || DEFAULT_DURATION);
    }

    function parseQueryToasts() {
        const params = new URLSearchParams(window.location.search);
        const toastMessage = params.get("toast");
        const toastType = params.get("toastType");
        if (toastMessage) {
            show(toastMessage, toastType || "info");
        }
    }

    function parseDataToasts() {
        const nodes = document.querySelectorAll("[data-app-toast]");
        nodes.forEach(function (node) {
            const message = node.getAttribute("data-toast-message");
            const type = node.getAttribute("data-toast-type") || "info";
            show(message, type);
        });
    }

    window.AppToast = {
        show: show,
        success: function (message, durationMs) {
            show(message, "success", durationMs);
        },
        error: function (message, durationMs) {
            show(message, "error", durationMs);
        },
        warning: function (message, durationMs) {
            show(message, "warning", durationMs);
        },
        info: function (message, durationMs) {
            show(message, "info", durationMs);
        },
        fromDocument: function () {
            parseQueryToasts();
            parseDataToasts();
        }
    };
})();
