/**
 * Modal de confirmação padrão do sistema.
 *
 * Uso no HTML:
 *   <form data-confirm data-confirm-title="Excluir contrato" data-confirm-message="Esta ação não pode ser desfeita." ...>
 *
 * Atributos disponíveis no form:
 *   data-confirm                → ativa o modal (obrigatório)
 *   data-confirm-title          → título do modal (padrão: "Confirmar ação")
 *   data-confirm-message        → mensagem do modal (padrão: "Tem certeza que deseja continuar?")
 *   data-confirm-btn            → texto do botão de confirmação (padrão: "Confirmar")
 *   data-confirm-danger         → se presente, botão fica vermelho (padrão: azul)
 */
(function () {
    'use strict';

    const MODAL_ID = 'appConfirmModal';

    function getOrCreateModal() {
        let modal = document.getElementById(MODAL_ID);
        if (modal) return modal;

        modal = document.createElement('div');
        modal.id = MODAL_ID;
        modal.className = 'modal fade';
        modal.tabIndex = -1;
        modal.setAttribute('aria-hidden', 'true');
        modal.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content" style="border-radius:12px; border:none; box-shadow:0 8px 32px rgba(0,0,0,0.15);">
                    <div class="modal-header border-0 pb-0">
                        <h5 class="modal-title fw-semibold" id="${MODAL_ID}Label"></h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Fechar"></button>
                    </div>
                    <div class="modal-body pt-2 text-muted" id="${MODAL_ID}Body"></div>
                    <div class="modal-footer border-0 pt-0">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn" id="${MODAL_ID}Confirm">Confirmar</button>
                    </div>
                </div>
            </div>`;
        document.body.appendChild(modal);
        return modal;
    }

    function bindForms() {
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            if (form.dataset.confirmBound) return;
            form.dataset.confirmBound = '1';

            form.addEventListener('submit', function (e) {
                e.preventDefault();

                const title   = form.dataset.confirmTitle   || 'Confirmar ação';
                const message = form.dataset.confirmMessage || 'Tem certeza que deseja continuar?';
                const btnText = form.dataset.confirmBtn     || 'Confirmar';
                const danger  = 'confirmDanger' in form.dataset;
                const hasBootstrapModal = typeof window.bootstrap !== 'undefined'
                    && window.bootstrap.Modal
                    && typeof window.bootstrap.Modal.getOrCreateInstance === 'function';

                if (!hasBootstrapModal) {
                    const confirmed = window.confirm(message);
                    if (confirmed) {
                        form.submit();
                    }
                    return;
                }

                const modalEl  = getOrCreateModal();
                const bsModal  = window.bootstrap.Modal.getOrCreateInstance(modalEl);
                const confirmBtn = document.getElementById(MODAL_ID + 'Confirm');

                modalEl.querySelector('.modal-title').textContent = title;
                document.getElementById(MODAL_ID + 'Body').textContent = message;
                confirmBtn.textContent = btnText;
                confirmBtn.className = 'btn ' + (danger ? 'btn-danger' : 'btn-primary');

                // Remove listener anterior para evitar múltiplos disparos
                const newBtn = confirmBtn.cloneNode(true);
                confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
                newBtn.addEventListener('click', function () {
                    bsModal.hide();
                    form.submit();
                });

                bsModal.show();
            });
        });
    }

    // Bind inicial e re-bind para conteúdo dinâmico
    document.addEventListener('DOMContentLoaded', bindForms);

    // Expõe para uso manual se necessário
    window.AppConfirm = { bind: bindForms };
})();
