/**
 * Busca automática de CEP via ViaCEP.
 * Uso: adicione data-cep ao input de CEP.
 * Os demais campos são mapeados via data-cep-* no mesmo form.
 *
 * Atributos no <form> ou em qualquer ancestral:
 *   data-cep-logradouro  → seletor do campo logradouro
 *   data-cep-bairro      → seletor do campo bairro
 *   data-cep-cidade      → seletor do campo cidade
 *   data-cep-estado      → seletor do campo estado (UF)
 *
 * Exemplo:
 *   <form data-cep-logradouro="#logradouro" data-cep-bairro="#bairro"
 *         data-cep-cidade="#cidade" data-cep-estado="#estado">
 *     <input type="text" id="cep" data-cep />
 *   </form>
 */
(function () {
    'use strict';

    function onlyDigits(v) {
        return v.replace(/\D/g, '');
    }

    function setField(form, attr, value) {
        var selector = form.getAttribute(attr);
        if (!selector) return;
        var el = form.querySelector(selector);
        if (el) {
            el.value = value || '';
            el.dispatchEvent(new Event('input'));
        }
    }

    function showFeedback(input, type, msg) {
        var fb = input.parentElement.querySelector('.cep-feedback');
        if (!fb) {
            fb = document.createElement('div');
            fb.className = 'cep-feedback small mt-1';
            input.parentElement.appendChild(fb);
        }
        fb.className = 'cep-feedback small mt-1 ' + (type === 'error' ? 'text-danger' : 'text-success');
        fb.textContent = msg;
    }

    function clearFeedback(input) {
        var fb = input.parentElement.querySelector('.cep-feedback');
        if (fb) fb.textContent = '';
    }

    function buscarCep(input) {
        if (input.dataset.fetching === '1') return;

        var cep = onlyDigits(input.value);
        if (cep.length !== 8) return;

        var form = input.closest('form');
        if (!form) return;

        // Formata visualmente
        input.value = cep.substring(0, 5) + '-' + cep.substring(5);

        // Indicador de carregamento e lock para evitar múltiplas requisições
        input.dataset.fetching = '1';
        input.readOnly = true;
        showFeedback(input, 'info', 'Buscando CEP...');

        fetch('https://viacep.com.br/ws/' + cep + '/json/')
            .then(function (res) {
                if (!res.ok) throw new Error('Não foi possível conectar ao ViaCEP');
                return res.json(); 
            })
            .then(function (data) {
                input.dataset.fetching = '0';
                input.readOnly = false;
                
                if (data.erro) {
                    showFeedback(input, 'error', 'CEP não encontrado.');
                    return;
                }
                setField(form, 'data-cep-logradouro', data.logradouro);
                setField(form, 'data-cep-bairro',     data.bairro);
                setField(form, 'data-cep-cidade',      data.localidade);
                setField(form, 'data-cep-estado',      data.uf);
                showFeedback(input, 'success', 'Endereço preenchido automaticamente.');

                // Foca no campo número após preencher
                var numSelector = form.getAttribute('data-cep-numero');
                if (numSelector) {
                    var numEl = form.querySelector(numSelector);
                    if (numEl) numEl.focus();
                }
            })
            .catch(function () {
                input.dataset.fetching = '0';
                input.readOnly = false;
                showFeedback(input, 'error', 'Erro ao buscar CEP. Verifique sua conexão.');
            });
    }

    function bind() {
        document.querySelectorAll('input[data-cep]').forEach(function (input) {
            if (input.dataset.cepBound) return;
            input.dataset.cepBound = '1';

            // Dispara ao sair do campo (blur) com 8 dígitos
            input.addEventListener('blur', function () {
                buscarCep(input);
            });

            // Dispara automaticamente ao atingir 8 dígitos (ou 9 com hífen)
            input.addEventListener('input', function () {
                clearFeedback(input);
                var digits = onlyDigits(input.value);
                if (digits.length === 8) {
                    buscarCep(input);
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', bind);
    window.AppCep = { bind: bind };
})();
