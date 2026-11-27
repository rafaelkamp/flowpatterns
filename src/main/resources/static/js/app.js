import { api, ApiError } from './api.js';

/* =========================================================
   Constantes de domínio e apresentação
   ========================================================= */

const TIPOS = {
    DESPESA: { rotulo: 'Despesa', icone: 'i-dollar', descricao: 'Reembolso e despesas corporativas' },
    COMPRA: { rotulo: 'Compra', icone: 'i-shopping-bag', descricao: 'Aquisição de bens e serviços' },
    FERIAS: { rotulo: 'Férias', icone: 'i-calendar', descricao: 'Programação de férias' },
    ACESSO: { rotulo: 'Acesso', icone: 'i-key', descricao: 'Acesso a sistemas e módulos' }
};

const STATUS = {
    RASCUNHO: { rotulo: 'Rascunho', icone: 'i-edit' },
    PENDENTE: { rotulo: 'Pendente', icone: 'i-clock' },
    EM_ANALISE: { rotulo: 'Em análise', icone: 'i-eye' },
    APROVADA: { rotulo: 'Aprovada', icone: 'i-check-circle' },
    REJEITADA: { rotulo: 'Rejeitada', icone: 'i-x-circle' },
    CANCELADA: { rotulo: 'Cancelada', icone: 'i-slash' }
};

const PRIORIDADES = {
    BAIXA: 'Baixa',
    MEDIA: 'Média',
    ALTA: 'Alta',
    URGENTE: 'Urgente'
};

const PAPEIS = {
    SOLICITANTE: 'Solicitante',
    GESTOR: 'Gestor',
    DIRETOR: 'Diretor',
    ADMINISTRADOR: 'Administrador'
};

const CATEGORIAS = {
    VIAGEM: 'Viagem',
    MATERIAL: 'Material',
    SERVICO: 'Serviço',
    TREINAMENTO: 'Treinamento',
    OUTROS: 'Outros'
};

const NIVEIS_ACESSO = {
    BASICO: 'Básico',
    INTERMEDIARIO: 'Intermediário',
    ADMINISTRATIVO: 'Administrativo'
};

const NIVEIS_APROVACAO = {
    GESTOR: 'Gestão',
    DIRETOR: 'Diretoria'
};

const EVENTOS = {
    CRIACAO: { rotulo: 'Solicitação criada', icone: 'i-plus' },
    ATUALIZACAO: { rotulo: 'Solicitação atualizada', icone: 'i-edit' },
    ENVIO: { rotulo: 'Enviada para aprovação', icone: 'i-send' },
    APROVACAO: { rotulo: 'Aprovação registrada', icone: 'i-check' },
    REJEICAO: { rotulo: 'Rejeição registrada', icone: 'i-x' },
    CANCELAMENTO: { rotulo: 'Cancelamento registrado', icone: 'i-slash' }
};

const CHAVE_USUARIO = 'flowpatterns.usuarioId';
const CHAVE_TEMA = 'flowpatterns.tema';
const TAMANHO_PAGINA = 12;

const state = {
    usuarios: [],
    usuario: null,
    naoLidas: 0,
    renderSeq: 0
};

/* =========================================================
   Utilitários gerais
   ========================================================= */

const $ = (seletor, raiz = document) => raiz.querySelector(seletor);
const $$ = (seletor, raiz = document) => Array.from(raiz.querySelectorAll(seletor));

const ESCAPES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };

function esc(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, (caractere) => ESCAPES[caractere]);
}

function icon(nome, classe = '') {
    return `<svg class="icon ${classe}" aria-hidden="true"><use href="#${nome}"></use></svg>`;
}

const FMT_DATA = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
const FMT_DATA_HORA = new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
});
const FMT_MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

function data(valor) {
    if (!valor) return '—';
    if (typeof valor === 'string') {
        const apenasData = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor);
        if (apenasData) return `${apenasData[3]}/${apenasData[2]}/${apenasData[1]}`;
    }
    const convertida = new Date(valor);
    return Number.isNaN(convertida.getTime()) ? '—' : FMT_DATA.format(convertida);
}

function dataHora(valor) {
    if (!valor) return '—';
    const convertida = new Date(valor);
    return Number.isNaN(convertida.getTime()) ? '—' : FMT_DATA_HORA.format(convertida);
}

function moeda(valor) {
    if (valor === null || valor === undefined || valor === '') return '—';
    const numero = Number(valor);
    return Number.isNaN(numero) ? '—' : FMT_MOEDA.format(numero);
}

function horas(valor) {
    const numero = Number(valor);
    if (!Number.isFinite(numero) || numero <= 0) return '—';
    if (numero < 1) return `${Math.round(numero * 60)} min`;
    if (numero < 24) return `${numero.toFixed(1).replace('.', ',')} h`;
    const dias = Math.floor(numero / 24);
    const resto = Math.round(numero % 24);
    return resto ? `${dias}d ${resto}h` : `${dias}d`;
}

function diasFerias(inicio, fim) {
    if (!inicio || !fim) return null;
    const inicial = Date.parse(`${inicio}T00:00:00Z`);
    const final = Date.parse(`${fim}T00:00:00Z`);
    if (Number.isNaN(inicial) || Number.isNaN(final) || final < inicial) return null;
    return Math.round((final - inicial) / 86400000) + 1;
}

function restanteSla(prazo) {
    if (!prazo) return null;
    const alvo = new Date(prazo).getTime();
    if (Number.isNaN(alvo)) return null;
    const diferenca = alvo - Date.now();
    const absoluto = Math.abs(diferenca);
    const dias = Math.floor(absoluto / 86400000);
    const horasRestantes = Math.floor((absoluto % 86400000) / 3600000);
    const minutos = Math.floor((absoluto % 3600000) / 60000);
    let texto;
    if (dias > 0) texto = `${dias}d ${horasRestantes}h`;
    else if (horasRestantes > 0) texto = `${horasRestantes}h ${minutos}min`;
    else texto = `${Math.max(1, minutos)} min`;
    return { atrasado: diferenca < 0, texto };
}

function primeiroNome(nome) {
    return String(nome || '').trim().split(/\s+/)[0] || 'usuário';
}

function iniciaisDe(nome) {
    const partes = String(nome || '?').trim().split(/\s+/).filter(Boolean);
    if (!partes.length) return '?';
    if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase();
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
}

function matiz(nome) {
    let valor = 0;
    for (const caractere of String(nome || '?')) {
        valor = (valor * 31 + caractere.charCodeAt(0)) % 360;
    }
    return valor;
}

function avatarHtml(nome, classe = 'avatar-sm') {
    return `<span class="avatar ${classe}" style="--h:${matiz(nome)}" aria-hidden="true">${esc(iniciaisDe(nome))}</span>`;
}

function usuarioPorId(id) {
    return state.usuarios.find((usuario) => usuario.id === Number(id)) || null;
}

function mensagemErro(erro) {
    if (erro instanceof ApiError) {
        if (erro.fieldErrors.length) {
            return `${erro.message} (${erro.fieldErrors.map((item) => item.mensagem).join('; ')})`;
        }
        return erro.message;
    }
    return erro?.message || 'Ocorreu um erro inesperado.';
}

function badgeStatus(status) {
    const meta = STATUS[status] || { rotulo: status };
    return `<span class="badge status-${esc(status)}"><span class="dot"></span>${esc(meta.rotulo)}</span>`;
}

function badgePrioridade(prioridade) {
    return `<span class="prio prio-${esc(prioridade)}">${esc(PRIORIDADES[prioridade] || prioridade)}</span>`;
}

function badgePapel(papel) {
    return `<span class="role role-${esc(papel)}">${esc(PAPEIS[papel] || papel)}</span>`;
}

function badgeAtivo(ativo) {
    return ativo
        ? '<span class="badge badge-success"><span class="dot"></span>Ativo</span>'
        : '<span class="badge badge-neutral"><span class="dot"></span>Inativo</span>';
}

function tipoIcone(tipo) {
    const meta = TIPOS[tipo] || { rotulo: tipo, icone: 'i-file' };
    return `<span class="type-icon type-${esc(tipo)}">${icon(meta.icone)}</span>`;
}

function slaCelulaHtml(solicitacao, compacto = false) {
    if (!solicitacao.prazoSla) return '<span class="muted">—</span>';
    const restante = restanteSla(solicitacao.prazoSla);
    if (!restante) return '<span class="muted">—</span>';
    if (compacto) {
        return `<span class="${solicitacao.atrasada ? 'text-danger' : 'text-soft'}" style="font-size:11px;font-weight:600">${solicitacao.atrasada ? 'Atrasada' : 'Faltam'} ${esc(restante.texto)}</span>`;
    }
    return `<div class="${solicitacao.atrasada ? 'text-danger' : ''}" style="font-size:11.8px;font-weight:600">${solicitacao.atrasada ? 'Atrasada' : 'Faltam'} ${esc(restante.texto)}</div>
            <div class="cell-sub">${dataHora(solicitacao.prazoSla)}</div>`;
}

function podeAprovar(solicitacao) {
    return ['PENDENTE', 'EM_ANALISE'].includes(solicitacao.status)
        && state.usuario?.id === solicitacao.aprovadorAtualId;
}

function podeCancelar(solicitacao) {
    if (!['RASCUNHO', 'PENDENTE', 'EM_ANALISE'].includes(solicitacao.status) || !state.usuario) return false;
    return state.usuario.id === solicitacao.solicitanteId || state.usuario.papel === 'ADMINISTRADOR';
}

function podeEditar(solicitacao) {
    return solicitacao.status === 'RASCUNHO';
}

/* =========================================================
   Toasts e modais
   ========================================================= */

function toast(mensagem, opcoes = {}) {
    const { tipo = 'info', titulo } = typeof opcoes === 'string' ? { tipo: opcoes } : opcoes;
    const raiz = $('#toast-root');
    const elemento = document.createElement('div');
    elemento.className = `toast ${tipo}`;
    const icone = tipo === 'success' ? 'i-check-circle' : tipo === 'error' ? 'i-alert' : 'i-inbox';
    const tituloPadrao = tipo === 'success' ? 'Tudo certo' : tipo === 'error' ? 'Não foi possível concluir' : 'Informação';
    elemento.innerHTML = `${icon(icone)}<div><strong>${esc(titulo || tituloPadrao)}</strong><span>${esc(mensagem)}</span></div>`;
    raiz.appendChild(elemento);
    setTimeout(() => {
        elemento.style.opacity = '0';
        elemento.style.transform = 'translateY(6px)';
        elemento.style.transition = 'opacity .2s ease, transform .2s ease';
        setTimeout(() => elemento.remove(), 220);
    }, 4800);
}

let modalAtual = null;

function openModal({ titulo, subtitulo = '', conteudo = '', rodape = '', largo = false, onClose }) {
    closeModal();
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
        <div class="modal ${largo ? 'wide' : ''}" role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
            <div class="modal-header">
                <div>
                    <h3 id="modal-titulo">${esc(titulo)}</h3>
                    ${subtitulo ? `<p>${esc(subtitulo)}</p>` : ''}
                </div>
                <button class="icon-btn modal-close" type="button" aria-label="Fechar">${icon('i-x')}</button>
            </div>
            <div class="modal-body">${conteudo}</div>
            ${rodape ? `<div class="modal-footer">${rodape}</div>` : ''}
        </div>`;
    $('#modal-root').appendChild(overlay);
    document.body.style.overflow = 'hidden';

    let fechado = false;
    const fechar = () => {
        if (fechado) return;
        fechado = true;
        overlay.remove();
        if (modalAtual?.overlay === overlay) modalAtual = null;
        document.body.style.overflow = '';
        onClose?.();
    };

    overlay.addEventListener('click', (evento) => {
        if (evento.target === overlay) fechar();
    });
    overlay.querySelector('.modal-close').addEventListener('click', fechar);

    const modal = {
        overlay,
        close: fechar,
        body: overlay.querySelector('.modal-body')
    };
    modalAtual = modal;

    const focavel = overlay.querySelector('input, select, textarea, button:not(.modal-close)');
    (focavel || overlay.querySelector('.modal-close'))?.focus();
    return modal;
}

function closeModal() {
    if (modalAtual) modalAtual.close();
}

function confirmar({ titulo, mensagem, confirmar = 'Confirmar', perigo = false, detalhe = '' }) {
    return new Promise((resolver) => {
        let resolvido = false;
        const finalizar = (valor) => {
            if (resolvido) return;
            resolvido = true;
            resolver(valor);
        };
        const modal = openModal({
            titulo,
            conteudo: `<p class="muted" style="font-size:12.8px;line-height:1.55">${esc(mensagem)}</p>
                ${detalhe ? `<div class="demo-banner" style="margin:12px 0 0">${icon('i-alert')}<div>${esc(detalhe)}</div></div>` : ''}`,
            rodape: `<button class="btn btn-secondary" type="button" data-cancelar>Cancelar</button>
                     <button class="btn ${perigo ? 'btn-danger' : 'btn-primary'}" type="button" data-confirmar>${esc(confirmar)}</button>`,
            onClose: () => finalizar(false)
        });
        modal.overlay.querySelector('[data-cancelar]').addEventListener('click', () => modal.close());
        modal.overlay.querySelector('[data-confirmar]').addEventListener('click', () => {
            finalizar(true);
            modal.close();
        });
    });
}

/* =========================================================
   Tema, usuário e dropdown
   ========================================================= */

function aplicarTema() {
    const salvo = localStorage.getItem(CHAVE_TEMA);
    const preferido = window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    const tema = salvo === 'dark' || salvo === 'light' ? salvo : preferido;
    document.documentElement.dataset.theme = tema;
    atualizarIconeTema(tema);
}

function atualizarIconeTema(tema) {
    const uso = $('[data-theme-icon]');
    if (uso) uso.setAttribute('href', tema === 'dark' ? '#i-sun' : '#i-moon');
    const botao = $('#theme-btn');
    if (botao) botao.title = tema === 'dark' ? 'Usar tema claro' : 'Usar tema escuro';
}

function alternarTema() {
    const atual = document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light';
    const proximo = atual === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = proximo;
    localStorage.setItem(CHAVE_TEMA, proximo);
    atualizarIconeTema(proximo);
}

function atualizarUsuarioChrome() {
    const usuario = state.usuario;
    const nome = usuario?.nome || 'Selecionar usuário';
    const papel = usuario ? (PAPEIS[usuario.papel] || usuario.papel) : 'Demonstração';

    for (const [idAvatar, idNome, idPapel] of [
        ['sidebar-avatar', 'sidebar-user-name', 'sidebar-user-role'],
        ['topbar-avatar', 'topbar-user-name', 'topbar-user-role']
    ]) {
        const avatar = document.getElementById(idAvatar);
        if (!avatar) continue;
        avatar.textContent = iniciaisDe(nome);
        avatar.style.setProperty('--h', String(matiz(nome)));
        document.getElementById(idNome).textContent = nome;
        document.getElementById(idPapel).textContent = idPapel === 'sidebar-user-role'
            ? (usuario ? `${papel} · ${usuario.departamento}` : papel)
            : papel;
    }
}

function renderizarListaUsuarios(filtro = '') {
    const lista = $('#user-list');
    if (!lista) return;
    const termo = String(filtro || '').toLowerCase().trim();
    const ativos = state.usuarios.filter((usuario) => usuario.ativo);
    const filtrados = ativos.filter((usuario) => {
        if (!termo) return true;
        return [usuario.nome, usuario.email, usuario.departamento]
            .some((valor) => String(valor || '').toLowerCase().includes(termo));
    });

    if (!filtrados.length) {
        lista.innerHTML = `<div class="empty" style="padding:24px 12px"><p style="margin:0;font-size:12px">Nenhum usuário ativo encontrado.</p></div>`;
        return;
    }

    lista.innerHTML = filtrados.map((usuario) => `
        <button class="dropdown-user ${usuario.id === state.usuario?.id ? 'current' : ''}"
                type="button" data-usuario-id="${usuario.id}"
                role="option" aria-selected="${usuario.id === state.usuario?.id}">
            ${avatarHtml(usuario.nome)}
            <span class="dropdown-user-info">
                <strong>${esc(usuario.nome)}</strong>
                <small>${esc(usuario.departamento)} · ${esc(PAPEIS[usuario.papel] || usuario.papel)}</small>
            </span>
            ${usuario.id === state.usuario?.id ? icon('i-check', 'icon-sm') : ''}
        </button>`).join('');

    lista.querySelectorAll('[data-usuario-id]').forEach((botao) => {
        botao.addEventListener('click', () => selecionarUsuario(Number(botao.dataset.usuarioId)));
    });
}

function alternarDropdown(forcar) {
    const dropdown = $('#user-dropdown');
    if (!dropdown) return;
    const abrir = typeof forcar === 'boolean' ? forcar : dropdown.hidden;
    dropdown.hidden = !abrir;
    $('#user-chip')?.setAttribute('aria-expanded', String(abrir));
    if (abrir) {
        $('#user-search').value = '';
        renderizarListaUsuarios('');
        $('#user-search').focus();
    }
}

function fecharDropdown() {
    const dropdown = $('#user-dropdown');
    if (!dropdown || dropdown.hidden) return;
    dropdown.hidden = true;
    $('#user-chip')?.setAttribute('aria-expanded', 'false');
}

function fecharSidebarMobile() {
    $('.app-shell')?.classList.remove('sidebar-open');
    const backdrop = $('#sidebar-backdrop');
    if (backdrop) backdrop.hidden = true;
}

function abrirSidebarMobile() {
    $('.app-shell')?.classList.add('sidebar-open');
    const backdrop = $('#sidebar-backdrop');
    if (backdrop) backdrop.hidden = false;
}

function selecionarUsuario(id) {
    const usuario = state.usuarios.find((item) => item.id === Number(id));
    if (!usuario) return;
    if (!usuario.ativo) {
        toast('Este usuário está inativo e não pode assumir o fluxo.', { tipo: 'error' });
        return;
    }
    const mudou = state.usuario?.id !== usuario.id;
    state.usuario = usuario;
    localStorage.setItem(CHAVE_USUARIO, String(usuario.id));
    atualizarUsuarioChrome();
    renderizarListaUsuarios('');
    fecharDropdown();
    atualizarBadgeNotificacoes();
    if (mudou) {
        toast(`Agora você está simulando ${usuario.nome}.`, { tipo: 'success', titulo: 'Usuário alterado' });
        renderRoute();
    }
}

async function carregarUsuarios() {
    const pagina = await api.listarUsuarios({ size: 200, sort: 'nome,asc' });
    state.usuarios = Array.isArray(pagina?.conteudo) ? pagina.conteudo : [];
}

function restaurarUsuario() {
    const salvo = Number(localStorage.getItem(CHAVE_USUARIO));
    let usuario = state.usuarios.find((item) => item.id === salvo && item.ativo);
    if (!usuario) usuario = state.usuarios.find((item) => item.email === 'ana@flowpatterns.local' && item.ativo);
    if (!usuario) usuario = state.usuarios.find((item) => item.ativo) || null;
    state.usuario = usuario;
    if (usuario) localStorage.setItem(CHAVE_USUARIO, String(usuario.id));
    atualizarUsuarioChrome();
    renderizarListaUsuarios('');
}

async function atualizarBadgeNotificacoes() {
    if (!state.usuario) {
        state.naoLidas = 0;
        aplicarBadgeNotificacoes();
        return;
    }
    try {
        state.naoLidas = Number(await api.contarNaoLidas(state.usuario.id)) || 0;
    } catch (erro) {
        state.naoLidas = 0;
    }
    aplicarBadgeNotificacoes();
}

function aplicarBadgeNotificacoes() {
    const texto = state.naoLidas > 99 ? '99+' : String(state.naoLidas);
    for (const id of ['topbar-notif-badge', 'nav-notif-badge']) {
        const badge = document.getElementById(id);
        if (!badge) continue;
        badge.textContent = texto;
        badge.hidden = state.naoLidas === 0;
    }
}

/* =========================================================
   Rotas e renderização
   ========================================================= */

function parseRoute() {
    const bruto = (location.hash || '#/painel').replace(/^#/, '');
    const [caminho, queryString] = bruto.split('?');
    const params = new URLSearchParams(queryString || '');
    const partes = caminho.split('/').filter(Boolean);

    if (partes[0] === 'solicitacoes') {
        if (partes.length === 1) return { name: 'solicitacoes', params };
        if (partes[1] === 'nova') return { name: 'nova', params };
        if (partes[2] === 'editar') return { name: 'editar', id: partes[1], params };
        return { name: 'detalhe', id: partes[1], params };
    }
    if (partes[0] === 'usuarios') return { name: 'usuarios', params };
    if (partes[0] === 'notificacoes') return { name: 'notificacoes', params };
    return { name: 'painel', params };
}

function navegar(hash) {
    if (location.hash === hash) {
        renderRoute();
    } else {
        location.hash = hash;
    }
}

function atualizarChrome(route) {
    const titulos = {
        painel: ['Painel', 'Visão geral das solicitações e aprovações'],
        solicitacoes: ['Solicitações', 'Acompanhe, filtre e gerencie as solicitações'],
        nova: ['Nova solicitação', 'Crie um rascunho e envie para aprovação'],
        editar: ['Editar solicitação', 'Atualize os dados do rascunho'],
        detalhe: ['Detalhe da solicitação', 'Dados, histórico e ações do fluxo'],
        usuarios: ['Usuários', 'Quem participa do fluxo de aprovação'],
        notificacoes: ['Notificações', 'Sua caixa de entrada interna']
    };
    const [titulo, subtitulo] = titulos[route.name] || titulos.painel;
    $('#page-title').textContent = titulo;
    $('#page-subtitle').textContent = subtitulo;
    document.title = `${titulo} · FlowPatterns`;

    const navAtivo = route.name === 'nova' ? 'nova'
        : ['solicitacoes', 'editar', 'detalhe'].includes(route.name) ? 'solicitacoes'
        : route.name;
    $$('.nav-item').forEach((item) => {
        item.classList.toggle('active', item.dataset.nav === navAtivo);
    });
}

function loadingHtml(texto = 'Carregando...') {
    return `<div class="page-loading"><span class="spinner" aria-hidden="true"></span><p>${esc(texto)}</p></div>`;
}

function erroHtml(erro) {
    return `<div class="card error-panel">
        <div class="empty-icon">${icon('i-alert')}</div>
        <h3>Não foi possível carregar</h3>
        <p>${esc(mensagemErro(erro))}</p>
        <button class="btn btn-primary" type="button" data-recarregar>${icon('i-refresh')}Tentar novamente</button>
    </div>`;
}

function podeRenderizar(seq) {
    return seq === state.renderSeq;
}

async function renderRoute() {
    const seq = ++state.renderSeq;
    const route = parseRoute();
    atualizarChrome(route);
    fecharSidebarMobile();
    const view = $('#view');
    view.innerHTML = loadingHtml();

    try {
        switch (route.name) {
            case 'solicitacoes':
                await viewSolicitacoes(route, seq);
                break;
            case 'nova':
                await viewFormulario(route, seq, 'criar');
                break;
            case 'editar':
                await viewFormulario(route, seq, 'editar');
                break;
            case 'detalhe':
                await viewDetalhe(route, seq);
                break;
            case 'usuarios':
                await viewUsuarios(route, seq);
                break;
            case 'notificacoes':
                await viewNotificacoes(route, seq);
                break;
            default:
                await viewPainel(route, seq);
        }
    } catch (erro) {
        if (!podeRenderizar(seq)) return;
        console.error(erro);
        view.innerHTML = erroHtml(erro);
    }
}

/* =========================================================
   Painel
   ========================================================= */

async function viewPainel(route, seq) {
    const [painel, pendentes] = await Promise.all([
        api.painel(),
        state.usuario
            ? api.listarSolicitacoes({
                aprovadorId: state.usuario.id,
                size: 5,
                sort: 'prazoSla,asc'
            }).catch(() => ({ conteudo: [] }))
            : Promise.resolve({ conteudo: [] })
    ]);
    if (!podeRenderizar(seq)) return;

    const total = Number(painel.total) || 0;
    const emAndamento = Number(painel.emAndamento) || 0;
    const atrasadas = Number(painel.atrasadas) || 0;
    const tempo = Number(painel.tempoMedioAprovacaoHoras) || 0;
    const porStatus = painel.porStatus || {};
    const porTipo = painel.porTipo || {};
    const ultimas = painel.ultimasSolicitacoes || [];
    const minhasPendentes = pendentes.conteudo || [];

    const coresStatus = {
        RASCUNHO: '#94a3b8',
        PENDENTE: '#f59e0b',
        EM_ANALISE: '#3b82f6',
        APROVADA: '#10b981',
        REJEITADA: '#ef4444',
        CANCELADA: '#6b7280'
    };
    const ordemStatus = Object.keys(STATUS);
    const segmentos = [];
    let acumulado = 0;
    if (total > 0) {
        for (const status of ordemStatus) {
            const quantidade = Number(porStatus[status]) || 0;
            if (quantidade <= 0) continue;
            const inicio = (acumulado / total) * 100;
            acumulado += quantidade;
            const fim = (acumulado / total) * 100;
            segmentos.push(`${coresStatus[status]} ${inicio.toFixed(2)}% ${fim.toFixed(2)}%`);
        }
    }
    const gradiente = segmentos.length
        ? `conic-gradient(${segmentos.join(', ')})`
        : 'conic-gradient(var(--border) 0 100%)';

    const legenda = ordemStatus.map((status) => {
        const quantidade = Number(porStatus[status]) || 0;
        return `<div class="legend-item">
            <span class="legend-dot" style="background:${coresStatus[status]}"></span>
            ${STATUS[status].rotulo}
            <span class="legend-value">${quantidade}</span>
        </div>`;
    }).join('');

    const ordemTipos = Object.keys(TIPOS);
    const maxTipo = Math.max(1, ...ordemTipos.map((tipo) => Number(porTipo[tipo]) || 0));
    const coresTipo = { DESPESA: '#6366f1', COMPRA: '#f59e0b', FERIAS: '#10b981', ACESSO: '#3b82f6' };
    const barras = ordemTipos.map((tipo) => {
        const quantidade = Number(porTipo[tipo]) || 0;
        return `<div class="bar-row">
            <span class="bar-label">${TIPOS[tipo].rotulo}</span>
            <span class="bar-track"><span class="bar-fill" style="width:${Math.round((quantidade / maxTipo) * 100)}%;--bar-color:${coresTipo[tipo]}"></span></span>
            <span class="bar-value">${quantidade}</span>
        </div>`;
    }).join('');

    const bannerDemo = state.usuario ? `<div class="demo-banner">
        ${icon('i-layers')}
        <div><strong>Modo demonstração.</strong> Você está simulando
        <strong>${esc(state.usuario.nome)}</strong>
        (${esc(PAPEIS[state.usuario.papel] || state.usuario.papel)}).
        Troque de usuário no topo para testar diferentes permissões de aprovação.
        <a href="#/notificacoes">Abrir notificações</a>.</div>
    </div>` : '';

    view.innerHTML = `
        ${bannerDemo}
        <div class="page-header">
            <div>
                <h2>Visão geral</h2>
                <p>Indicadores consolidados do fluxo de solicitações.</p>
            </div>
            <div class="page-actions">
                <a class="btn btn-secondary" href="#/solicitacoes?escopo=aprovador">${icon('i-inbox')}Minha fila</a>
                <a class="btn btn-primary" href="#/solicitacoes/nova">${icon('i-plus')}Nova solicitação</a>
            </div>
        </div>

        <div class="kpi-grid">
            <article class="card kpi">
                <div class="kpi-top"><span class="kpi-icon">${icon('i-layers')}</span>Total de solicitações</div>
                <div class="kpi-value">${total}</div>
                <div class="kpi-sub"><strong>${emAndamento}</strong> em andamento</div>
            </article>
            <article class="card kpi warning">
                <div class="kpi-top"><span class="kpi-icon">${icon('i-clock')}</span>Em andamento</div>
                <div class="kpi-value">${emAndamento}</div>
                <div class="kpi-sub">Aguardando decisão de aprovação</div>
            </article>
            <article class="card kpi ${atrasadas > 0 ? 'danger' : 'success'}">
                <div class="kpi-top"><span class="kpi-icon">${icon('i-alert')}</span>Fora do prazo</div>
                <div class="kpi-value">${atrasadas}</div>
                <div class="kpi-sub">${atrasadas > 0 ? 'Solicitações com SLA vencido' : 'Nenhum SLA vencido'}</div>
            </article>
            <article class="card kpi success">
                <div class="kpi-top"><span class="kpi-icon">${icon('i-trending-up')}</span>Tempo médio</div>
                <div class="kpi-value">${horas(tempo)}</div>
                <div class="kpi-sub">Da criação até a decisão</div>
            </article>
        </div>

        <div class="grid-2">
            <section class="card">
                <div class="card-header">
                    <div><h2>Distribuição por status</h2><p class="card-subtitle">Como as solicitações estão hoje</p></div>
                </div>
                <div class="card-body">
                    <div class="donut-wrap">
                        <div class="donut" style="background:${gradiente}">
                            <div class="donut-center"><strong>${total}</strong><span>solicitações</span></div>
                        </div>
                        <div class="legend">${legenda}</div>
                    </div>
                </div>
            </section>
            <section class="card">
                <div class="card-header">
                    <div><h2>Distribuição por tipo</h2><p class="card-subtitle">Volume por categoria de solicitação</p></div>
                </div>
                <div class="card-body"><div class="bar-list">${barras}</div></div>
            </section>
        </div>

        <div class="grid-2">
            <section class="card">
                <div class="card-header">
                    <div><h2>Aguardando sua aprovação</h2><p class="card-subtitle">Fila de decisões para ${esc(primeiroNome(state.usuario?.nome || 'o usuário atual'))}</p></div>
                    <a class="btn btn-sm btn-ghost card-action" href="#/solicitacoes?escopo=aprovador">Ver fila</a>
                </div>
                ${minhasPendentes.length ? `<div class="table-wrap"><table class="table"><tbody>
                    ${minhasPendentes.map((solicitacao) => linhaSolicitacaoHtml(solicitacao, { compacta: true })).join('')}
                </tbody></table></div>` : `
                    <div class="empty">
                        <div class="empty-icon">${icon('i-check-circle')}</div>
                        <h3>Nada pendente para você</h3>
                        <p>Não há solicitações aguardando sua decisão agora.</p>
                        <a class="btn btn-secondary btn-sm" href="#/solicitacoes">Explorar solicitações</a>
                    </div>`}
            </section>
            <section class="card">
                <div class="card-header">
                    <div><h2>Últimas solicitações</h2><p class="card-subtitle">Movimentações mais recentes</p></div>
                    <a class="btn btn-sm btn-ghost card-action" href="#/solicitacoes">Ver todas</a>
                </div>
                ${ultimas.length ? `<div class="table-wrap"><table class="table"><tbody>
                    ${ultimas.map((solicitacao) => linhaSolicitacaoHtml(solicitacao, { compacta: true, mostrarSolicitante: true })).join('')}
                </tbody></table></div>` : `
                    <div class="empty">
                        <div class="empty-icon">${icon('i-inbox')}</div>
                        <h3>Nenhuma solicitação ainda</h3>
                        <p>Crie a primeira solicitação para iniciar o fluxo.</p>
                        <a class="btn btn-primary btn-sm" href="#/solicitacoes/nova">Criar solicitação</a>
                    </div>`}
            </section>
        </div>`;

    view.querySelectorAll('[data-solicitacao]').forEach((linha) => {
        linha.addEventListener('click', (evento) => {
            if (evento.target.closest('a, button, input')) return;
            navegar(`#/solicitacoes/${linha.dataset.solicitacao}`);
        });
    });
}

function linhaSolicitacaoHtml(solicitacao, opcoes = {}) {
    const tipo = TIPOS[solicitacao.tipo] || { rotulo: solicitacao.tipo, icone: 'i-file' };
    const complemento = opcoes.mostrarSolicitante && solicitacao.solicitanteNome
        ? ` · ${esc(solicitacao.solicitanteNome)}`
        : '';
    return `<tr class="clickable" data-solicitacao="${solicitacao.id}">
        <td>
            <div class="type-chip">
                <span class="type-icon type-${esc(solicitacao.tipo)}">${icon(tipo.icone)}</span>
                <div style="min-width:0">
                    <div class="cell-title">${esc(solicitacao.titulo)}</div>
                    <div class="cell-sub">${esc(tipo.rotulo)} · #${solicitacao.id}${complemento}</div>
                </div>
            </div>
        </td>
        <td>${badgeStatus(solicitacao.status)}</td>
        <td>${slaCelulaHtml(solicitacao, true)}</td>
    </tr>`;
}

function paginacaoHtml(paginaAtual, totalPaginas) {
    if (!totalPaginas || totalPaginas <= 1) return '';
    const botoes = [];
    botoes.push(`<button type="button" data-pagina="${paginaAtual - 1}" ${paginaAtual <= 0 ? 'disabled' : ''} aria-label="Página anterior">${icon('i-chevron-left', 'icon-sm')}</button>`);

    const paginas = [];
    for (let indice = 0; indice < totalPaginas; indice += 1) {
        if (indice === 0 || indice === totalPaginas - 1 || Math.abs(indice - paginaAtual) <= 1) {
            paginas.push(indice);
        } else if (paginas[paginas.length - 1] !== 'intervalo') {
            paginas.push('intervalo');
        }
    }

    for (const pagina of paginas) {
        if (pagina === 'intervalo') {
            botoes.push('<span class="pager-gap">…</span>');
        } else {
            botoes.push(`<button type="button" data-pagina="${pagina}" class="${pagina === paginaAtual ? 'active' : ''}" ${pagina === paginaAtual ? 'aria-current="page"' : ''}>${pagina + 1}</button>`);
        }
    }

    botoes.push(`<button type="button" data-pagina="${paginaAtual + 1}" ${paginaAtual >= totalPaginas - 1 ? 'disabled' : ''} aria-label="Próxima página">${icon('i-chevron-right', 'icon-sm')}</button>`);
    return `<div class="pager">${botoes.join('')}</div>`;
}

function hashSolicitacoes(filtros, pagina = filtros.pagina || 0) {
    const params = new URLSearchParams();
    if (filtros.texto) params.set('texto', filtros.texto);
    if (filtros.status) params.set('status', filtros.status);
    if (filtros.tipo) params.set('tipo', filtros.tipo);
    if (filtros.prioridade) params.set('prioridade', filtros.prioridade);
    if (filtros.escopo) params.set('escopo', filtros.escopo);
    if (pagina > 0) params.set('pagina', String(pagina));
    const query = params.toString();
    return `#/solicitacoes${query ? `?${query}` : ''}`;
}

/* =========================================================
   Lista de solicitações
   ========================================================= */

async function viewSolicitacoes(route, seq) {
    const filtros = {
        texto: route.params.get('texto') || '',
        status: route.params.get('status') || '',
        tipo: route.params.get('tipo') || '',
        prioridade: route.params.get('prioridade') || '',
        escopo: route.params.get('escopo') || '',
        pagina: Math.max(0, Number(route.params.get('pagina')) || 0)
    };

    const params = {
        page: filtros.pagina,
        size: TAMANHO_PAGINA,
        sort: 'criadoEm,desc'
    };
    if (filtros.texto) params.texto = filtros.texto;
    if (filtros.status) params.status = filtros.status;
    if (filtros.tipo) params.tipo = filtros.tipo;
    if (filtros.prioridade) params.prioridade = filtros.prioridade;
    if (filtros.escopo === 'minhas' && state.usuario) params.solicitanteId = state.usuario.id;
    if (filtros.escopo === 'aprovador' && state.usuario) params.aprovadorId = state.usuario.id;

    const pagina = await api.listarSolicitacoes(params);
    if (!podeRenderizar(seq)) return;

    const conteudo = pagina.conteudo || [];
    const totalPaginas = Number(pagina.totalPaginas) || 0;
    const totalElementos = Number(pagina.totalElementos) || 0;
    const inicio = totalElementos === 0 ? 0 : filtros.pagina * TAMANHO_PAGINA + 1;
    const fim = filtros.pagina * TAMANHO_PAGINA + conteudo.length;

    const opcoesStatus = Object.entries(STATUS)
        .map(([valor, meta]) => `<option value="${valor}" ${valor === filtros.status ? 'selected' : ''}>${meta.rotulo}</option>`)
        .join('');
    const opcoesTipo = Object.entries(TIPOS)
        .map(([valor, meta]) => `<option value="${valor}" ${valor === filtros.tipo ? 'selected' : ''}>${meta.rotulo}</option>`)
        .join('');
    const opcoesPrioridade = Object.entries(PRIORIDADES)
        .map(([valor, rotulo]) => `<option value="${valor}" ${valor === filtros.prioridade ? 'selected' : ''}>${rotulo}</option>`)
        .join('');

    const linhas = conteudo.map((solicitacao) => {
        const tipo = TIPOS[solicitacao.tipo] || { rotulo: solicitacao.tipo, icone: 'i-file' };
        return `<tr class="clickable" data-id="${solicitacao.id}">
            <td class="check-col" data-no-nav>
                <input type="checkbox" class="row-check" value="${solicitacao.id}" aria-label="Selecionar ${esc(solicitacao.titulo)}">
            </td>
            <td>
                <div class="type-chip">
                    <span class="type-icon type-${esc(solicitacao.tipo)}">${icon(tipo.icone)}</span>
                    <div style="min-width:0">
                        <div class="cell-title">${esc(solicitacao.titulo)}</div>
                        <div class="cell-sub">${esc(tipo.rotulo)} · #${solicitacao.id}</div>
                    </div>
                </div>
            </td>
            <td>${badgeStatus(solicitacao.status)}</td>
            <td>${badgePrioridade(solicitacao.prioridade)}</td>
            <td>${esc(solicitacao.solicitanteNome || '—')}</td>
            <td class="col-hide-sm">${esc(solicitacao.aprovadorAtualNome || '—')}</td>
            <td class="col-hide-md">${slaCelulaHtml(solicitacao)}</td>
            <td class="cell-actions">
                <button class="btn btn-sm btn-ghost" type="button" data-ver="${solicitacao.id}">${icon('i-eye', 'icon-sm')}Ver</button>
            </td>
        </tr>`;
    }).join('');

    const tabela = conteudo.length ? `
        <div class="table-wrap">
            <table class="table">
                <thead>
                    <tr>
                        <th class="check-col"><input type="checkbox" id="check-all" aria-label="Selecionar todas as solicitações desta página"></th>
                        <th>Solicitação</th>
                        <th>Status</th>
                        <th>Prioridade</th>
                        <th>Solicitante</th>
                        <th class="col-hide-sm">Aprovador atual</th>
                        <th class="col-hide-md">Prazo SLA</th>
                        <th class="cell-actions">Ações</th>
                    </tr>
                </thead>
                <tbody>${linhas}</tbody>
            </table>
        </div>` : `
        <div class="empty">
            <div class="empty-icon">${icon('i-search')}</div>
            <h3>Nenhuma solicitação encontrada</h3>
            <p>${filtros.texto || filtros.status || filtros.tipo || filtros.prioridade || filtros.escopo
                ? 'Tente ajustar ou limpar os filtros aplicados.'
                : 'Crie a primeira solicitação para iniciar o fluxo.'}</p>
            ${filtros.texto || filtros.status || filtros.tipo || filtros.prioridade || filtros.escopo
                ? `<button class="btn btn-secondary btn-sm" type="button" data-limpar>Limpar filtros</button>`
                : `<a class="btn btn-primary btn-sm" href="#/solicitacoes/nova">Criar solicitação</a>`}
        </div>`;

    view.innerHTML = `
        <div class="page-header">
            <div>
                <h2>Solicitações</h2>
                <p>${totalElementos} ${totalElementos === 1 ? 'registro encontrado' : 'registros encontrados'}${filtros.escopo === 'aprovador' ? ' na sua fila de aprovação' : filtros.escopo === 'minhas' ? ' criados por você' : ''}.</p>
            </div>
            <div class="page-actions">
                <a class="btn btn-primary" href="#/solicitacoes/nova">${icon('i-plus')}Nova solicitação</a>
            </div>
        </div>

        <section class="card" style="margin-bottom:16px">
            <form class="filters" id="filtros-solicitacoes">
                <div class="field">
                    <label for="filtro-texto">Buscar</label>
                    <div class="input-icon">
                        ${icon('i-search')}
                        <input class="input" id="filtro-texto" name="texto" type="search" placeholder="Título ou descrição..." value="${esc(filtros.texto)}">
                    </div>
                </div>
                <div class="field">
                    <label for="filtro-status">Status</label>
                    <select class="select" id="filtro-status" name="status">
                        <option value="">Todos</option>
                        ${opcoesStatus}
                    </select>
                </div>
                <div class="field">
                    <label for="filtro-tipo">Tipo</label>
                    <select class="select" id="filtro-tipo" name="tipo">
                        <option value="">Todos</option>
                        ${opcoesTipo}
                    </select>
                </div>
                <div class="field">
                    <label for="filtro-prioridade">Prioridade</label>
                    <select class="select" id="filtro-prioridade" name="prioridade">
                        <option value="">Todas</option>
                        ${opcoesPrioridade}
                    </select>
                </div>
                <div class="field">
                    <label for="filtro-escopo">Escopo</label>
                    <select class="select" id="filtro-escopo" name="escopo">
                        <option value="" ${filtros.escopo === '' ? 'selected' : ''}>Todas</option>
                        <option value="minhas" ${filtros.escopo === 'minhas' ? 'selected' : ''}>Minhas solicitações</option>
                        <option value="aprovador" ${filtros.escopo === 'aprovador' ? 'selected' : ''}>Aguardando minha aprovação</option>
                    </select>
                </div>
                <div class="filter-actions">
                    <button class="btn btn-primary" type="submit">${icon('i-filter', 'icon-sm')}Filtrar</button>
                    <button class="btn btn-ghost" type="button" data-limpar>Limpar</button>
                </div>
            </form>
        </section>

        <section class="card">
            <div class="batch-bar" id="batch-bar" hidden>
                <strong id="batch-count">0</strong> selecionadas
                <div class="batch-actions">
                    <button class="btn btn-sm btn-success" type="button" data-lote="APROVAR">${icon('i-check', 'icon-sm')}Aprovar</button>
                    <button class="btn btn-sm btn-danger-soft" type="button" data-lote="REJEITAR">${icon('i-x', 'icon-sm')}Rejeitar</button>
                    <button class="btn btn-sm btn-ghost" type="button" data-lote="CANCELAR">${icon('i-slash', 'icon-sm')}Cancelar</button>
                    <button class="btn btn-sm btn-ghost" type="button" data-limpar-selecao>Limpar seleção</button>
                </div>
            </div>
            ${tabela}
            ${conteudo.length ? `<div class="table-footer">
                <span>Mostrando <strong>${inicio}–${fim}</strong> de <strong>${totalElementos}</strong></span>
                ${paginacaoHtml(filtros.pagina, totalPaginas)}
            </div>` : ''}
        </section>`;

    const form = $('#filtros-solicitacoes');
    const selecionados = new Set();

    form.addEventListener('submit', (evento) => {
        evento.preventDefault();
        const dados = new FormData(form);
        navegar(hashSolicitacoes({
            texto: String(dados.get('texto') || '').trim(),
            status: dados.get('status') || '',
            tipo: dados.get('tipo') || '',
            prioridade: dados.get('prioridade') || '',
            escopo: dados.get('escopo') || ''
        }, 0));
    });

    form.querySelectorAll('select').forEach((select) => {
        select.addEventListener('change', () => form.requestSubmit());
    });

    view.querySelectorAll('[data-limpar]').forEach((botao) => {
        botao.addEventListener('click', () => navegar('#/solicitacoes'));
    });

    const barra = $('#batch-bar');
    const contador = $('#batch-count');
    const checkAll = $('#check-all');

    function atualizarSelecao() {
        view.querySelectorAll('.row-check').forEach((checkbox) => {
            checkbox.closest('tr')?.classList.toggle('selected', checkbox.checked);
        });
        if (barra) barra.hidden = selecionados.size === 0;
        if (contador) contador.textContent = String(selecionados.size);
        if (checkAll) {
            const checks = $$('.row-check', view);
            checkAll.checked = checks.length > 0 && checks.every((check) => check.checked);
            checkAll.indeterminate = checks.some((check) => check.checked) && !checks.every((check) => check.checked);
        }
    }

    view.querySelectorAll('.row-check').forEach((checkbox) => {
        checkbox.addEventListener('change', () => {
            const id = Number(checkbox.value);
            if (checkbox.checked) selecionados.add(id);
            else selecionados.delete(id);
            atualizarSelecao();
        });
    });

    checkAll?.addEventListener('change', () => {
        view.querySelectorAll('.row-check').forEach((checkbox) => {
            checkbox.checked = checkAll.checked;
            const id = Number(checkbox.value);
            if (checkbox.checked) selecionados.add(id);
            else selecionados.delete(id);
        });
        atualizarSelecao();
    });

    view.querySelectorAll('[data-limpar-selecao]').forEach((botao) => {
        botao.addEventListener('click', () => {
            selecionados.clear();
            view.querySelectorAll('.row-check').forEach((checkbox) => { checkbox.checked = false; });
            atualizarSelecao();
        });
    });

    view.querySelectorAll('[data-lote]').forEach((botao) => {
        botao.addEventListener('click', () => {
            if (!selecionados.size) return;
            abrirLote(botao.dataset.lote, Array.from(selecionados));
        });
    });

    view.querySelectorAll('[data-ver]').forEach((botao) => {
        botao.addEventListener('click', () => navegar(`#/solicitacoes/${botao.dataset.ver}`));
    });

    view.querySelectorAll('tbody tr[data-id]').forEach((linha) => {
        linha.addEventListener('click', (evento) => {
            if (evento.target.closest('button, a, input, label, select')) return;
            navegar(`#/solicitacoes/${linha.dataset.id}`);
        });
    });

    view.querySelectorAll('[data-pagina]').forEach((botao) => {
        botao.addEventListener('click', () => {
            const destino = Number(botao.dataset.pagina);
            if (Number.isNaN(destino) || destino < 0 || destino >= totalPaginas) return;
            navegar(hashSolicitacoes(filtros, destino));
        });
    });
}

/* =========================================================
   Ações em lote
   ========================================================= */

async function abrirLote(acao, ids) {
    if (!state.usuario) {
        toast('Selecione um usuário para executar ações.', { tipo: 'error' });
        return;
    }
    if (!ids.length) return;

    const info = {
        APROVAR: {
            titulo: 'Aprovar em lote',
            rotulo: 'Observação (opcional)',
            placeholder: 'Ex.: Aprovado em lote conforme planejamento.',
            botao: 'Aprovar solicitações',
            classe: 'btn-success',
            exige: false
        },
        REJEITAR: {
            titulo: 'Rejeitar em lote',
            rotulo: 'Justificativa',
            placeholder: 'Ex.: Solicitações fora do orçamento disponível.',
            botao: 'Rejeitar solicitações',
            classe: 'btn-danger',
            exige: true
        },
        CANCELAR: {
            titulo: 'Cancelar em lote',
            rotulo: 'Motivo do cancelamento',
            placeholder: 'Ex.: Solicitações duplicadas.',
            botao: 'Cancelar solicitações',
            classe: 'btn-danger',
            exige: true
        }
    }[acao];

    if (!info) return;

    const modal = openModal({
        titulo: info.titulo,
        subtitulo: `${ids.length} ${ids.length === 1 ? 'solicitação selecionada' : 'solicitações selecionadas'}`,
        conteudo: `<p class="muted" style="font-size:12.8px;line-height:1.55">
                A ação será executada em nome de <strong>${esc(state.usuario.nome)}</strong>.
                O resultado individual de cada solicitação será exibido ao final.
            </p>
            <div class="field" style="margin-top:14px">
                <label for="lote-observacao">${info.rotulo}</label>
                <textarea id="lote-observacao" class="textarea" maxlength="500" placeholder="${esc(info.placeholder)}"></textarea>
                <span class="field-error" id="lote-erro" hidden></span>
            </div>`,
        rodape: `<button class="btn btn-secondary" type="button" data-cancelar>Cancelar</button>
                 <button class="btn ${info.classe}" type="button" data-confirmar>${esc(info.botao)}</button>`
    });

    const confirmarBotao = modal.overlay.querySelector('[data-confirmar]');
    const cancelarBotao = modal.overlay.querySelector('[data-cancelar]');
    const observacao = modal.overlay.querySelector('#lote-observacao');
    const erro = modal.overlay.querySelector('#lote-erro');

    cancelarBotao.addEventListener('click', () => modal.close());

    confirmarBotao.addEventListener('click', async () => {
        const texto = observacao.value.trim();
        if (info.exige && !texto) {
            erro.textContent = 'Informe o texto solicitado para continuar.';
            erro.hidden = false;
            observacao.focus();
            return;
        }
        erro.hidden = true;
        confirmarBotao.disabled = true;
        const htmlOriginal = confirmarBotao.innerHTML;
        confirmarBotao.innerHTML = '<span class="spinner"></span>Executando...';
        try {
            const resultado = await api.executarLote({
                acao,
                ids,
                atorId: state.usuario.id,
                observacao: texto || null
            });
            modal.close();
            mostrarResultadoLote(resultado);
            toast(
                `${resultado.sucesso} de ${resultado.total} solicitações concluídas.`,
                { tipo: resultado.falha > 0 ? 'info' : 'success', titulo: 'Ação em lote executada' }
            );
            atualizarBadgeNotificacoes();
            renderRoute();
        } catch (falha) {
            toast(mensagemErro(falha), { tipo: 'error' });
            confirmarBotao.disabled = false;
            confirmarBotao.innerHTML = htmlOriginal;
        }
    });
}

function mostrarResultadoLote(resultado) {
    const itens = (resultado.resultados || []).map((item) => `
        <div class="batch-result ${item.sucesso ? 'ok' : 'fail'}">
            ${icon(item.sucesso ? 'i-check-circle' : 'i-x-circle', 'icon-sm')}
            <strong>#${item.solicitacaoId}</strong>
            <span class="batch-msg">${esc(item.mensagem || (STATUS[item.status]?.rotulo ?? item.status ?? '—'))}</span>
        </div>`).join('');

    const modal = openModal({
        titulo: 'Resultado da ação em lote',
        subtitulo: `${resultado.sucesso} de ${resultado.total} concluídas${resultado.falha ? ` · ${resultado.falha} falha(s)` : ''}`,
        conteudo: `<div class="batch-results">${itens || '<p class="muted">Nenhum resultado retornado.</p>'}</div>`,
        rodape: '<button class="btn btn-primary" type="button" data-fechar>Fechar</button>'
    });
    modal.overlay.querySelector('[data-fechar]').addEventListener('click', () => modal.close());
}

/* =========================================================
   Criação e edição de solicitações
   ========================================================= */

async function viewFormulario(route, seq, modo) {
    let solicitacao = null;

    if (modo === 'editar') {
        solicitacao = await api.buscarSolicitacao(route.id);
        if (solicitacao.status !== 'RASCUNHO') {
            toast('Somente solicitações em rascunho podem ser editadas.', { tipo: 'error' });
            navegar(`#/solicitacoes/${solicitacao.id}`);
            return;
        }
    }

    if (!podeRenderizar(seq)) return;

    const tipoDaQuery = route.params.get('tipo');
    const tipoInicial = solicitacao?.tipo || (TIPOS[tipoDaQuery] ? tipoDaQuery : 'DESPESA');
    view.innerHTML = formularioHtml(solicitacao, modo, tipoInicial);
    bindFormulario(solicitacao, modo, tipoInicial);
}

function formularioHtml(solicitacao, modo, tipoInicial) {
    const editando = modo === 'editar';
    const tipo = tipoInicial;
    const voltar = editando ? `#/solicitacoes/${solicitacao.id}` : '#/solicitacoes';
    const solicitante = editando
        ? usuarioPorId(solicitacao.solicitanteId)
        : (usuarioPorId(state.usuario?.id) || null);
    const solicitanteId = solicitacao?.solicitanteId ?? state.usuario?.id ?? '';

    const opcoesPrioridade = Object.entries(PRIORIDADES).map(([valor, rotulo]) => {
        const selecionado = (solicitacao?.prioridade || 'MEDIA') === valor ? 'selected' : '';
        return `<option value="${valor}" ${selecionado}>${rotulo}</option>`;
    }).join('');

    const opcoesSolicitante = state.usuarios
        .filter((usuario) => usuario.ativo)
        .map((usuario) => `<option value="${usuario.id}" ${usuario.id === Number(solicitanteId) ? 'selected' : ''}>
            ${esc(usuario.nome)} — ${esc(usuario.departamento)} (${esc(PAPEIS[usuario.papel] || usuario.papel)})
        </option>`).join('');

    const campoPrioridade = `<div class="field">
        <label for="prioridade">Prioridade *</label>
        <select class="select" id="prioridade" name="prioridade" required>${opcoesPrioridade}</select>
    </div>`;

    const campoSolicitante = editando ? `<div class="field">
        <label>Solicitante</label>
        <div class="input" style="display:flex;align-items:center;gap:9px;background:var(--surface-2)">
            ${solicitante ? avatarHtml(solicitante.nome) : ''}
            <div style="min-width:0">
                <div class="strong" style="font-size:12.2px">${esc(solicitante?.nome || 'Usuário não encontrado')}</div>
                <div class="muted" style="font-size:10.5px">${esc(solicitante?.departamento || '—')}</div>
            </div>
        </div>
        <span class="field-hint">O solicitante não pode ser alterado.</span>
    </div>` : `<div class="field">
        <label for="solicitanteId">Solicitante *</label>
        <select class="select" id="solicitanteId" name="solicitanteId" required>${opcoesSolicitante}</select>
    </div>`;

    const cartaoTipo = editando ? `<section class="card">
        <div class="card-body" style="display:flex;align-items:center;gap:12px">
            ${tipoIcone(tipo)}
            <div>
                <strong style="font-size:13px">${esc(TIPOS[tipo]?.rotulo || tipo)}</strong>
                <div class="muted" style="font-size:11px">O tipo da solicitação não pode ser alterado após a criação.</div>
            </div>
        </div>
    </section>` : `<section class="card">
        <div class="card-header">
            <div><h2>Tipo de solicitação</h2><p class="card-subtitle">Cada tipo possui campos e regras de aprovação próprios.</p></div>
        </div>
        <div class="card-body">
            <div class="type-picker">${Object.entries(TIPOS).map(([valor, meta]) => `
                <label class="type-option ${valor === tipo ? 'selected' : ''}">
                    <input type="radio" name="tipo" value="${valor}" ${valor === tipo ? 'checked' : ''}>
                    ${icon(meta.icone)}
                    <strong>${meta.rotulo}</strong>
                    <small>${meta.descricao}</small>
                </label>`).join('')}
            </div>
        </div>
    </section>`;

    return `
        <div class="page-header">
            <div>
                <h2>${editando ? 'Editar rascunho' : 'Nova solicitação'}</h2>
                <p>${editando ? `Atualize os dados da solicitação #${solicitacao.id}.` : 'Preencha os dados, salve como rascunho ou envie diretamente para aprovação.'}</p>
            </div>
            <div class="page-actions">
                <a class="btn btn-ghost" href="${voltar}">${icon('i-arrow-left')}Voltar</a>
            </div>
        </div>

        <form id="solicitacao-form">
            <div class="stack">
                ${cartaoTipo}

                <section class="card">
                    <div class="card-header">
                        <div><h2>Informações gerais</h2><p class="card-subtitle">Dados comuns a todos os tipos de solicitação.</p></div>
                    </div>
                    <div class="card-body">
                        <div class="form-grid">
                            <div class="field span-2">
                                <label for="titulo">Título *</label>
                                <input class="input" id="titulo" name="titulo" type="text" required maxlength="140"
                                       placeholder="Ex.: Compra de notebooks para o time" value="${esc(solicitacao?.titulo || '')}">
                            </div>
                            <div class="field span-2">
                                <label for="descricao">Descrição *</label>
                                <textarea class="textarea" id="descricao" name="descricao" required maxlength="2000"
                                          placeholder="Detalhe o contexto, a justificativa e o que está sendo solicitado.">${esc(solicitacao?.descricao || '')}</textarea>
                            </div>
                            ${campoPrioridade}
                            ${campoSolicitante}
                        </div>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header">
                        <div><h2>Dados específicos</h2><p class="card-subtitle" id="specific-hint">${esc(TIPOS[tipo]?.descricao || '')}</p></div>
                    </div>
                    <div class="card-body">
                        ${gruposHtml(tipo, solicitacao)}
                    </div>
                </section>

                <div class="form-actions">
                    <a class="btn btn-ghost left" href="${voltar}">Cancelar</a>
                    <button class="btn btn-secondary" type="submit" name="acao" value="rascunho">
                        ${icon('i-edit')}Salvar rascunho
                    </button>
                    <button class="btn btn-primary" type="submit" name="acao" value="enviar">
                        ${icon('i-send')}Salvar e enviar
                    </button>
                </div>
            </div>
        </form>`;
}

function gruposHtml(tipoAtual, solicitacao) {
    const despesa = solicitacao?.tipo === 'DESPESA' ? solicitacao : null;
    const compra = solicitacao?.tipo === 'COMPRA' ? solicitacao : null;
    const ferias = solicitacao?.tipo === 'FERIAS' ? solicitacao : null;
    const acesso = solicitacao?.tipo === 'ACESSO' ? solicitacao : null;
    const oculto = (tipo) => tipoAtual === tipo ? '' : 'hidden';

    const opcoesCategoria = Object.entries(CATEGORIAS).map(([valor, rotulo]) =>
        `<option value="${valor}" ${despesa?.categoriaDespesa === valor ? 'selected' : ''}>${rotulo}</option>`
    ).join('');

    const opcoesNivel = Object.entries(NIVEIS_ACESSO).map(([valor, rotulo]) =>
        `<option value="${valor}" ${acesso?.nivelAcesso === valor ? 'selected' : ''}>${rotulo}</option>`
    ).join('');

    const dias = diasFerias(ferias?.dataInicio, ferias?.dataFim);

    return `
        <div class="form-grid" data-group="DESPESA" ${oculto('DESPESA')}>
            <div class="field">
                <label for="despesa-valor">Valor (R$) *</label>
                <input class="input" id="despesa-valor" name="valor" type="number" min="0.01" step="0.01"
                       placeholder="0,00" value="${despesa?.valor ?? ''}" required>
            </div>
            <div class="field">
                <label for="despesa-categoria">Categoria *</label>
                <select class="select" id="despesa-categoria" name="categoriaDespesa" required>
                    <option value="">Selecione</option>
                    ${opcoesCategoria}
                </select>
            </div>
            <div class="field">
                <label for="despesa-centro">Centro de custo</label>
                <input class="input" id="despesa-centro" name="centroCusto" type="text" maxlength="80"
                       placeholder="Ex.: CC-1001" value="${esc(despesa?.centroCusto || '')}">
            </div>
        </div>

        <div class="form-grid" data-group="COMPRA" ${oculto('COMPRA')}>
            <div class="field">
                <label for="compra-valor">Valor (R$) *</label>
                <input class="input" id="compra-valor" name="valor" type="number" min="0.01" step="0.01"
                       placeholder="0,00" value="${compra?.valor ?? ''}" required>
            </div>
            <div class="field">
                <label for="compra-fornecedor">Fornecedor *</label>
                <input class="input" id="compra-fornecedor" name="fornecedor" type="text" maxlength="120"
                       placeholder="Ex.: Tech Distribuidora" value="${esc(compra?.fornecedor || '')}" required>
            </div>
            <div class="field">
                <label for="compra-quantidade">Quantidade de itens *</label>
                <input class="input" id="compra-quantidade" name="quantidadeItens" type="number" min="1" step="1"
                       placeholder="1" value="${compra?.quantidadeItens ?? ''}" required>
            </div>
        </div>

        <div class="form-grid" data-group="FERIAS" ${oculto('FERIAS')}>
            <div class="field">
                <label for="ferias-inicio">Data de início *</label>
                <input class="input" id="ferias-inicio" name="dataInicio" type="date"
                       value="${esc(ferias?.dataInicio || '')}" required>
            </div>
            <div class="field">
                <label for="ferias-fim">Data de fim *</label>
                <input class="input" id="ferias-fim" name="dataFim" type="date"
                       value="${esc(ferias?.dataFim || '')}" required>
            </div>
            <div class="field span-2">
                <span class="field-hint" id="ferias-dias">${dias ? `${dias} dia(s) de férias, contando o dia inicial e o final.` : 'Informe as datas para calcular a duração.'}</span>
            </div>
        </div>

        <div class="form-grid" data-group="ACESSO" ${oculto('ACESSO')}>
            <div class="field">
                <label for="acesso-sistema">Sistema de destino *</label>
                <input class="input" id="acesso-sistema" name="sistema" type="text" maxlength="100"
                       placeholder="Ex.: ERP, CRM, VPN" value="${esc(acesso?.sistema || '')}" required>
            </div>
            <div class="field">
                <label for="acesso-nivel">Nível de acesso *</label>
                <select class="select" id="acesso-nivel" name="nivelAcesso" required>
                    <option value="">Selecione</option>
                    ${opcoesNivel}
                </select>
            </div>
        </div>`;
}

function bindFormulario(solicitacao, modo, tipoInicial) {
    const form = $('#solicitacao-form');
    const editando = modo === 'editar';
    const tipoFixo = solicitacao?.tipo;

    function tipoAtual() {
        if (editando) return tipoFixo;
        return form.querySelector('input[name="tipo"]:checked')?.value || 'DESPESA';
    }

    function atualizarGrupos() {
        const tipo = tipoAtual();
        form.querySelectorAll('[data-group]').forEach((grupo) => {
            const ativo = grupo.dataset.group === tipo;
            grupo.hidden = !ativo;
            grupo.querySelectorAll('input, select, textarea').forEach((campo) => {
                campo.disabled = !ativo;
            });
        });
        const dica = form.querySelector('#specific-hint');
        if (dica) dica.textContent = TIPOS[tipo]?.descricao || '';
        if (!editando) {
            form.querySelectorAll('.type-option').forEach((opcao) => {
                const radio = opcao.querySelector('input[name="tipo"]');
                opcao.classList.toggle('selected', radio?.checked === true);
            });
        }
        atualizarDiasFerias(form);
    }

    form.querySelectorAll('input[name="tipo"]').forEach((radio) => {
        radio.addEventListener('change', atualizarGrupos);
    });

    form.querySelectorAll('#ferias-inicio, #ferias-fim').forEach((campo) => {
        campo.addEventListener('input', () => atualizarDiasFerias(form));
    });

    form.addEventListener('input', (evento) => {
        const field = evento.target.closest('.field');
        if (!field) return;
        field.classList.remove('invalid');
        field.querySelector('.field-error')?.remove();
    });

    atualizarGrupos();

    form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        limparErrosFormulario(form);
        if (!form.reportValidity()) return;

        const tipo = tipoAtual();

        if (tipo === 'FERIAS') {
            const inicio = form.querySelector('#ferias-inicio').value;
            const fim = form.querySelector('#ferias-fim').value;
            if (!inicio || !fim || diasFerias(inicio, fim) === null) {
                toast('A data final deve ser posterior à data inicial.', { tipo: 'error', titulo: 'Datas inválidas' });
                return;
            }
        }

        const acao = evento.submitter?.value === 'enviar' ? 'enviar' : 'rascunho';
        const payload = montarPayload(form, modo, tipo);
        definirCarregandoFormulario(form, true, acao);

        try {
            const salva = editando
                ? await api.atualizarSolicitacao(solicitacao.id, payload)
                : await api.criarSolicitacao(payload);

            if (acao === 'enviar') {
                try {
                    await api.enviarSolicitacao(salva.id);
                    toast('Solicitação enviada para aprovação.', {
                        tipo: 'success',
                        titulo: editando ? 'Rascunho atualizado e enviado' : 'Solicitação criada e enviada'
                    });
                } catch (falhaEnvio) {
                    toast(`O rascunho foi salvo, mas o envio falhou: ${mensagemErro(falhaEnvio)}`, {
                        tipo: 'error',
                        titulo: 'Envio não realizado'
                    });
                }
            } else {
                toast(editando ? 'Rascunho atualizado.' : 'Rascunho criado.', { tipo: 'success' });
            }

            atualizarBadgeNotificacoes();
            navegar(`#/solicitacoes/${salva.id}`);
        } catch (falha) {
            marcarErrosFormulario(form, falha);
            toast(mensagemErro(falha), { tipo: 'error' });
            definirCarregandoFormulario(form, false, acao);
        }
    });
}

function atualizarDiasFerias(form = $('#solicitacao-form')) {
    if (!form) return;
    const inicio = form.querySelector('#ferias-inicio')?.value;
    const fim = form.querySelector('#ferias-fim')?.value;
    const dica = form.querySelector('#ferias-dias');
    if (!dica) return;
    const dias = diasFerias(inicio, fim);
    dica.textContent = dias
        ? `${dias} dia(s) de férias, contando o dia inicial e o final.`
        : 'Informe as datas para calcular a duração.';
}

function montarPayload(form, modo, tipo) {
    const valorTexto = (seletor) => {
        const valor = form.querySelector(seletor)?.value?.trim();
        return valor ? valor : null;
    };
    const valorNumero = (seletor) => {
        const valor = form.querySelector(seletor)?.value;
        return valor === '' || valor === null || valor === undefined ? null : Number(valor);
    };

    const payload = {
        titulo: form.querySelector('#titulo').value.trim(),
        descricao: form.querySelector('#descricao').value.trim(),
        prioridade: form.querySelector('#prioridade').value
    };

    if (modo === 'criar') {
        payload.tipo = tipo;
        payload.solicitanteId = Number(form.querySelector('#solicitanteId').value);
    }

    if (tipo === 'DESPESA') {
        payload.valor = valorNumero('#despesa-valor');
        payload.categoriaDespesa = valorTexto('#despesa-categoria');
        payload.centroCusto = valorTexto('#despesa-centro');
    } else if (tipo === 'COMPRA') {
        payload.valor = valorNumero('#compra-valor');
        payload.fornecedor = valorTexto('#compra-fornecedor');
        payload.quantidadeItens = valorNumero('#compra-quantidade');
    } else if (tipo === 'FERIAS') {
        payload.dataInicio = valorTexto('#ferias-inicio');
        payload.dataFim = valorTexto('#ferias-fim');
    } else if (tipo === 'ACESSO') {
        payload.sistema = valorTexto('#acesso-sistema');
        payload.nivelAcesso = valorTexto('#acesso-nivel');
    }

    return payload;
}

function definirCarregandoFormulario(form, carregando, acao) {
    form.querySelectorAll('button[type="submit"]').forEach((botao) => {
        botao.disabled = carregando;
        if (carregando) {
            if (botao.value === acao) {
                botao.dataset.htmlOriginal = botao.innerHTML;
                botao.innerHTML = `<span class="spinner"></span>${acao === 'enviar' ? 'Enviando...' : 'Salvando...'}`;
            }
        } else if (botao.dataset.htmlOriginal) {
            botao.innerHTML = botao.dataset.htmlOriginal;
            delete botao.dataset.htmlOriginal;
        }
    });
}

function limparErrosFormulario(form) {
    form.querySelectorAll('.field-error').forEach((erro) => erro.remove());
    form.querySelectorAll('.field.invalid').forEach((field) => field.classList.remove('invalid'));
}

function marcarErrosFormulario(form, erro) {
    if (!(erro instanceof ApiError)) return;
    for (const item of erro.fieldErrors) {
        const campo = String(item.campo || '');
        if (!campo) continue;
        const input = form.querySelector(`[name="${campo}"]:not(:disabled)`);
        if (!input) continue;
        const field = input.closest('.field');
        if (!field) continue;
        field.classList.add('invalid');
        if (!field.querySelector('.field-error')) {
            const mensagem = document.createElement('span');
            mensagem.className = 'field-error';
            mensagem.textContent = item.mensagem || 'Valor inválido';
            field.appendChild(mensagem);
        }
    }
}

/* =========================================================
   Detalhe da solicitação
   ========================================================= */

async function viewDetalhe(route, seq) {
    const [solicitacao, historico] = await Promise.all([
        api.buscarSolicitacao(route.id),
        api.historico(route.id).catch(() => [])
    ]);
    if (!podeRenderizar(seq)) return;
    view.innerHTML = detalheHtml(solicitacao, historico);
    bindDetalhe(solicitacao);
}

function detalheHtml(solicitacao, historico) {
    const tipo = TIPOS[solicitacao.tipo] || { rotulo: solicitacao.tipo, icone: 'i-file' };
    const solicitante = usuarioPorId(solicitacao.solicitanteId);
    const aprovador = usuarioPorId(solicitacao.aprovadorAtualId);
    const sla = restanteSla(solicitacao.prazoSla);

    const cartaoSla = solicitacao.prazoSla ? `
        <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:10px">
            <div>
                <div class="muted" style="font-size:10px;font-weight:700;letter-spacing:.06em;text-transform:uppercase">Prazo limite</div>
                <div class="strong" style="font-size:13px;margin-top:2px">${dataHora(solicitacao.prazoSla)}</div>
            </div>
            <span class="badge" style="background:${solicitacao.atrasada ? 'var(--danger-soft)' : 'var(--success-soft)'};color:${solicitacao.atrasada ? 'var(--danger)' : 'var(--success)'}">
                ${solicitacao.atrasada ? 'Atrasada' : 'No prazo'}
            </span>
        </div>
        <div class="field-hint" style="margin-top:9px">
            ${sla ? (solicitacao.atrasada ? `Vencido há ${esc(sla.texto)}.` : `Faltam ${esc(sla.texto)} para o prazo.`) : 'Prazo não calculado.'}
        </div>` : '<p class="muted" style="font-size:12px;margin:0">O prazo de SLA é definido no envio para aprovação.</p>';

    return `
        <div class="page-header">
            <div>
                <a href="#/solicitacoes" class="muted" style="font-size:11.5px;font-weight:600;display:inline-flex;align-items:center;gap:5px">
                    ${icon('i-arrow-left', 'icon-xs')}Voltar para solicitações
                </a>
                <h2 style="margin-top:5px">Detalhe da solicitação</h2>
                <p>#${solicitacao.id} · ${esc(tipo.rotulo)} · criada em ${dataHora(solicitacao.criadoEm)}</p>
            </div>
        </div>

        ${bannerAprovadorHtml(solicitacao)}

        <div class="detail-grid">
            <div class="stack">
                <section class="card detail-head">
                    <div class="detail-title-row">
                        <span class="type-icon type-${esc(solicitacao.tipo)}" style="width:46px;height:46px;border-radius:13px">
                            ${icon(tipo.icone, 'icon-lg')}
                        </span>
                        <div style="min-width:0;flex:1">
                            <h2 class="detail-title">${esc(solicitacao.titulo)}</h2>
                            <div class="detail-meta">
                                ${badgeStatus(solicitacao.status)}
                                ${badgePrioridade(solicitacao.prioridade)}
                                <span class="muted" style="font-size:11.5px">${esc(tipo.rotulo)}</span>
                            </div>
                        </div>
                    </div>
                    <p class="detail-desc">${esc(solicitacao.descricao)}</p>
                    ${acoesDetalheHtml(solicitacao)}
                </section>

                <section class="card">
                    <div class="card-header">
                        <div><h2>Dados específicos</h2><p class="card-subtitle">Informações do tipo ${esc(tipo.rotulo)}</p></div>
                    </div>
                    <div class="card-body">${dadosEspecificosHtml(solicitacao)}</div>
                </section>

                <section class="card">
                    <div class="card-header">
                        <div><h2>Histórico de auditoria</h2><p class="card-subtitle">${historico.length} ${historico.length === 1 ? 'evento registrado' : 'eventos registrados'}</p></div>
                    </div>
                    <div class="card-body">${timelineHtml(historico)}</div>
                </section>
            </div>

            <aside class="stack">
                <section class="card">
                    <div class="card-header"><h2>Fluxo de aprovação</h2></div>
                    <div class="card-body">
                        ${fluxoHtml(solicitacao)}
                        <div class="meta-grid" style="margin-top:16px">
                            <div class="meta-item">
                                <label>Etapa atual</label>
                                <span>${esc(NIVEIS_APROVACAO[solicitacao.nivelAprovacaoAtual] || '—')}</span>
                            </div>
                            <div class="meta-item">
                                <label>Alçada final</label>
                                <span>${esc(NIVEIS_APROVACAO[solicitacao.nivelAprovacaoFinal] || '—')}</span>
                            </div>
                        </div>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header"><h2>Prazo e SLA</h2></div>
                    <div class="card-body">${cartaoSla}</div>
                </section>

                <section class="card">
                    <div class="card-header"><h2>Envolvidos</h2></div>
                    <div class="card-body" style="display:flex;flex-direction:column;gap:15px">
                        <div>
                            <div class="muted" style="font-size:10px;font-weight:700;letter-spacing:.06em;text-transform:uppercase;margin-bottom:6px">Solicitante</div>
                            ${pessoaHtml(solicitante, 'Solicitante não encontrado')}
                        </div>
                        <div>
                            <div class="muted" style="font-size:10px;font-weight:700;letter-spacing:.06em;text-transform:uppercase;margin-bottom:6px">Aprovador atual</div>
                            ${pessoaHtml(aprovador, 'Nenhum aprovador definido')}
                        </div>
                    </div>
                </section>

                <section class="card">
                    <div class="card-header"><h2>Registro</h2></div>
                    <div class="card-body">
                        <div class="meta-grid" style="grid-template-columns:1fr;gap:12px">
                            <div class="meta-item"><label>Criada em</label><span>${dataHora(solicitacao.criadoEm)}</span></div>
                            <div class="meta-item"><label>Última atualização</label><span>${dataHora(solicitacao.atualizadoEm)}</span></div>
                            <div class="meta-item"><label>Enviada em</label><span>${dataHora(solicitacao.dataEnvio)}</span></div>
                            <div class="meta-item"><label>Decidida em</label><span>${dataHora(solicitacao.dataDecisao)}</span></div>
                            ${solicitacao.observacaoDecisao ? `<div class="meta-item"><label>Observação da decisão</label><span>${esc(solicitacao.observacaoDecisao)}</span></div>` : ''}
                        </div>
                    </div>
                </section>
            </aside>
        </div>`;
}

function acoesDetalheHtml(solicitacao) {
    const acoes = [];
    if (solicitacao.status === 'RASCUNHO') {
        acoes.push(`<a class="btn btn-secondary" href="#/solicitacoes/${solicitacao.id}/editar">${icon('i-edit')}Editar</a>`);
        acoes.push(`<button class="btn btn-primary" type="button" data-acao="enviar">${icon('i-send')}Enviar para aprovação</button>`);
        acoes.push(`<button class="btn btn-danger-soft" type="button" data-acao="excluir">${icon('i-trash')}Excluir</button>`);
    } else if (podeAprovar(solicitacao)) {
        acoes.push(`<button class="btn btn-success" type="button" data-acao="aprovar">${icon('i-check')}Aprovar</button>`);
        acoes.push(`<button class="btn btn-danger-soft" type="button" data-acao="rejeitar">${icon('i-x')}Rejeitar</button>`);
    }

    if (podeCancelar(solicitacao)) {
        acoes.push(`<button class="btn btn-ghost" type="button" data-acao="cancelar">${icon('i-slash')}Cancelar solicitação</button>`);
    }

    return acoes.length ? `<div class="detail-actions">${acoes.join('')}</div>` : '';
}

function bannerAprovadorHtml(solicitacao) {
    if (!['PENDENTE', 'EM_ANALISE'].includes(solicitacao.status) || podeAprovar(solicitacao)) return '';
    const aprovador = usuarioPorId(solicitacao.aprovadorAtualId);
    if (!aprovador) return '';
    return `<div class="demo-banner">
        ${icon('i-user')}
        <div>
            <strong>Aguardando decisão.</strong> A solicitação está com <strong>${esc(aprovador.nome)}</strong>
            (${esc(NIVEIS_APROVACAO[solicitacao.nivelAprovacaoAtual] || '—')}).
            ${aprovador.ativo ? `<button class="btn btn-sm btn-secondary" type="button" style="margin-left:6px" data-assumir-usuario="${aprovador.id}">
                Simular como ${esc(primeiroNome(aprovador.nome))}
            </button>` : ''}
        </div>
    </div>`;
}

function fluxoHtml(solicitacao) {
    const emAprovacao = ['PENDENTE', 'EM_ANALISE'].includes(solicitacao.status);
    const finalizado = ['APROVADA', 'REJEITADA', 'CANCELADA'].includes(solicitacao.status);
    const etapa1 = solicitacao.status === 'RASCUNHO' ? 'current' : 'done';
    const etapa2 = emAprovacao ? 'current' : finalizado ? 'done' : '';
    const etapa3 = finalizado ? 'current' : '';
    const rotuloEtapa2 = solicitacao.status === 'EM_ANALISE' ? 'Análise da diretoria' : 'Aprovação';
    const rotuloEtapa3 = solicitacao.status === 'REJEITADA' ? 'Rejeitada'
        : solicitacao.status === 'CANCELADA' ? 'Cancelada' : 'Concluída';

    return `<div class="steps">
        <div class="step ${etapa1}">
            <span class="step-dot">${etapa1 === 'done' ? '✓' : '1'}</span>Rascunho
        </div>
        <span class="step-line"></span>
        <div class="step ${etapa2}">
            <span class="step-dot">${etapa2 === 'done' ? '✓' : '2'}</span>${rotuloEtapa2}
        </div>
        <span class="step-line"></span>
        <div class="step ${etapa3}">
            <span class="step-dot">${etapa3 === 'done' ? '✓' : '3'}</span>${rotuloEtapa3}
        </div>
    </div>`;
}

function dadosEspecificosHtml(solicitacao) {
    const itens = [];
    if (solicitacao.tipo === 'DESPESA') {
        itens.push(['Valor', moeda(solicitacao.valor)]);
        itens.push(['Categoria', CATEGORIAS[solicitacao.categoriaDespesa] || '—']);
        itens.push(['Centro de custo', solicitacao.centroCusto || '—']);
    } else if (solicitacao.tipo === 'COMPRA') {
        itens.push(['Valor', moeda(solicitacao.valor)]);
        itens.push(['Fornecedor', solicitacao.fornecedor || '—']);
        itens.push(['Quantidade de itens', solicitacao.quantidadeItens ?? '—']);
    } else if (solicitacao.tipo === 'FERIAS') {
        itens.push(['Início', data(solicitacao.dataInicio)]);
        itens.push(['Fim', data(solicitacao.dataFim)]);
        itens.push(['Duração', solicitacao.diasFerias ? `${solicitacao.diasFerias} dias` : '—']);
    } else if (solicitacao.tipo === 'ACESSO') {
        itens.push(['Sistema', solicitacao.sistema || '—']);
        itens.push(['Nível de acesso', NIVEIS_ACESSO[solicitacao.nivelAcesso] || '—']);
    }

    return `<div class="data-list">${itens.map(([rotulo, valor]) => `
        <div class="data-item"><label>${esc(rotulo)}</label><span>${esc(valor)}</span></div>
    `).join('')}</div>`;
}

function timelineHtml(historico) {
    if (!historico?.length) {
        return `<div class="empty" style="padding:28px 12px">
            <div class="empty-icon">${icon('i-clock')}</div>
            <h3>Nenhum evento registrado</h3>
            <p>O histórico será exibido conforme o fluxo avançar.</p>
        </div>`;
    }

    return `<ul class="timeline">${historico.map((evento) => {
        const meta = EVENTOS[evento.tipoEvento] || { rotulo: evento.tipoEvento, icone: 'i-clock' };
        const transicao = evento.statusAnterior && evento.statusNovo
            ? `${STATUS[evento.statusAnterior]?.rotulo || evento.statusAnterior} → ${STATUS[evento.statusNovo]?.rotulo || evento.statusNovo}`
            : (evento.statusNovo ? STATUS[evento.statusNovo]?.rotulo || evento.statusNovo : '');
        return `<li class="timeline-item">
            <span class="timeline-icon ev-${esc(evento.tipoEvento)}">${icon(meta.icone, 'icon-xs')}</span>
            <div class="timeline-head">
                <strong>${esc(meta.rotulo)}</strong>
                <span class="timeline-time">${dataHora(evento.ocorridoEm)}</span>
            </div>
            ${evento.atorNome ? `<div class="timeline-detail">por ${esc(evento.atorNome)}</div>` : ''}
            ${evento.detalhe ? `<div class="timeline-detail">${esc(evento.detalhe)}</div>` : ''}
            ${transicao ? `<div class="timeline-transition">${esc(transicao)}</div>` : ''}
        </li>`;
    }).join('')}</ul>`;
}

function pessoaHtml(usuario, fallback) {
    if (!usuario) return `<div class="muted" style="font-size:12px">${esc(fallback)}</div>`;
    return `<div style="display:flex;align-items:center;gap:10px">
        ${avatarHtml(usuario.nome)}
        <div style="min-width:0">
            <div class="strong" style="font-size:12.5px">${esc(usuario.nome)}</div>
            <div class="muted" style="font-size:10.8px">${esc(usuario.departamento)} · ${esc(PAPEIS[usuario.papel] || usuario.papel)}</div>
        </div>
    </div>`;
}

function bindDetalhe(solicitacao) {
    view.querySelectorAll('[data-acao]').forEach((botao) => {
        botao.addEventListener('click', async () => {
            const acao = botao.dataset.acao;
            if (acao === 'enviar') await enviarSolicitacao(solicitacao);
            else if (acao === 'excluir') await excluirSolicitacao(solicitacao);
            else if (acao === 'aprovar' || acao === 'rejeitar') abrirDecisao(solicitacao, acao);
            else if (acao === 'cancelar') abrirCancelamento(solicitacao);
        });
    });
}

async function enviarSolicitacao(solicitacao) {
    const confirmado = await confirmar({
        titulo: 'Enviar para aprovação',
        mensagem: `A solicitação #${solicitacao.id} será enviada para a fila de aprovação. Deseja continuar?`,
        confirmar: 'Enviar'
    });
    if (!confirmado) return;

    try {
        await api.enviarSolicitacao(solicitacao.id);
        toast('Solicitação enviada para aprovação.', { tipo: 'success' });
        await atualizarBadgeNotificacoes();
        renderRoute();
    } catch (erro) {
        toast(mensagemErro(erro), { tipo: 'error' });
    }
}

async function excluirSolicitacao(solicitacao) {
    const confirmado = await confirmar({
        titulo: 'Excluir rascunho',
        mensagem: `O rascunho #${solicitacao.id} será excluído definitivamente, junto com seu histórico. Deseja continuar?`,
        confirmar: 'Excluir rascunho',
        perigo: true
    });
    if (!confirmado) return;

    try {
        await api.excluirSolicitacao(solicitacao.id);
        toast('Rascunho excluído.', { tipo: 'success' });
        navegar('#/solicitacoes');
    } catch (erro) {
        toast(mensagemErro(erro), { tipo: 'error' });
    }
}

function abrirDecisao(solicitacao, tipo) {
    if (!state.usuario) {
        toast('Selecione um usuário para executar a decisão.', { tipo: 'error' });
        return;
    }

    const aprovando = tipo === 'aprovar';
    const avancaraParaDiretoria = aprovando
        && solicitacao.nivelAprovacaoAtual === 'GESTOR'
        && solicitacao.nivelAprovacaoFinal === 'DIRETOR';

    const modal = openModal({
        titulo: aprovando ? 'Aprovar solicitação' : 'Rejeitar solicitação',
        subtitulo: `#${solicitacao.id} · ${solicitacao.titulo}`,
        conteudo: `<p class="muted" style="font-size:12.8px;line-height:1.55">
                ${aprovando
                    ? (avancaraParaDiretoria
                        ? 'Esta aprovação encaminhará a solicitação para a etapa da diretoria.'
                        : 'A solicitação será marcada como aprovada.')
                    : 'A solicitação será marcada como rejeitada. Explique o motivo para manter o histórico claro.'}
                A decisão será registrada em nome de <strong>${esc(state.usuario.nome)}</strong>.
            </p>
            <div class="field" style="margin-top:14px">
                <label for="decisao-observacao">${aprovando ? 'Observação (opcional)' : 'Justificativa'}</label>
                <textarea id="decisao-observacao" class="textarea" maxlength="500"
                          placeholder="${aprovando ? 'Ex.: Aprovado conforme planejamento.' : 'Ex.: Valores acima do orçamento disponível.'}"></textarea>
                <span class="field-error" id="decisao-erro" hidden></span>
            </div>`,
        rodape: `<button class="btn btn-secondary" type="button" data-cancelar>Cancelar</button>
                 <button class="btn ${aprovando ? 'btn-success' : 'btn-danger'}" type="button" data-confirmar>
                    ${aprovando ? 'Aprovar' : 'Rejeitar'}
                 </button>`
    });

    const observacao = modal.overlay.querySelector('#decisao-observacao');
    const erro = modal.overlay.querySelector('#decisao-erro');
    const confirmarBotao = modal.overlay.querySelector('[data-confirmar]');
    const cancelarBotao = modal.overlay.querySelector('[data-cancelar]');

    cancelarBotao.addEventListener('click', () => modal.close());

    confirmarBotao.addEventListener('click', async () => {
        const texto = observacao.value.trim();
        if (!aprovando && !texto) {
            erro.textContent = 'Informe a justificativa da rejeição.';
            erro.hidden = false;
            observacao.focus();
            return;
        }
        erro.hidden = true;
        confirmarBotao.disabled = true;
        const htmlOriginal = confirmarBotao.innerHTML;
        confirmarBotao.innerHTML = '<span class="spinner"></span>Registrando...';

        try {
            const resposta = aprovando
                ? await api.aprovarSolicitacao(solicitacao.id, state.usuario.id, texto)
                : await api.rejeitarSolicitacao(solicitacao.id, state.usuario.id, texto);
            modal.close();
            toast(
                aprovando
                    ? (resposta.status === 'EM_ANALISE'
                        ? 'Aprovação registrada. A solicitação foi encaminhada à diretoria.'
                        : 'Solicitação aprovada.')
                    : 'Solicitação rejeitada.',
                { tipo: aprovando ? 'success' : 'info' }
            );
            await atualizarBadgeNotificacoes();
            renderRoute();
        } catch (falha) {
            toast(mensagemErro(falha), { tipo: 'error' });
            confirmarBotao.disabled = false;
            confirmarBotao.innerHTML = htmlOriginal;
        }
    });
}

function abrirCancelamento(solicitacao) {
    if (!state.usuario) {
        toast('Selecione um usuário para cancelar a solicitação.', { tipo: 'error' });
        return;
    }

    const modal = openModal({
        titulo: 'Cancelar solicitação',
        subtitulo: `#${solicitacao.id} · ${solicitacao.titulo}`,
        conteudo: `<p class="muted" style="font-size:12.8px;line-height:1.55">
                O cancelamento encerra o fluxo da solicitação e será registrado no histórico.
                A ação será executada em nome de <strong>${esc(state.usuario.nome)}</strong>.
            </p>
            <div class="field" style="margin-top:14px">
                <label for="cancelamento-motivo">Motivo do cancelamento *</label>
                <textarea id="cancelamento-motivo" class="textarea" maxlength="500"
                          placeholder="Ex.: Solicitação criada em duplicidade."></textarea>
                <span class="field-error" id="cancelamento-erro" hidden></span>
            </div>`,
        rodape: `<button class="btn btn-secondary" type="button" data-cancelar>Voltar</button>
                 <button class="btn btn-danger" type="button" data-confirmar>Cancelar solicitação</button>`
    });

    const motivo = modal.overlay.querySelector('#cancelamento-motivo');
    const erro = modal.overlay.querySelector('#cancelamento-erro');
    const confirmarBotao = modal.overlay.querySelector('[data-confirmar]');
    const cancelarBotao = modal.overlay.querySelector('[data-cancelar]');

    cancelarBotao.addEventListener('click', () => modal.close());

    confirmarBotao.addEventListener('click', async () => {
        const texto = motivo.value.trim();
        if (!texto) {
            erro.textContent = 'Informe o motivo do cancelamento.';
            erro.hidden = false;
            motivo.focus();
            return;
        }
        erro.hidden = true;
        confirmarBotao.disabled = true;
        const htmlOriginal = confirmarBotao.innerHTML;
        confirmarBotao.innerHTML = '<span class="spinner"></span>Cancelando...';

        try {
            await api.cancelarSolicitacao(solicitacao.id, state.usuario.id, texto);
            modal.close();
            toast('Solicitação cancelada.', { tipo: 'info' });
            await atualizarBadgeNotificacoes();
            renderRoute();
        } catch (falha) {
            toast(mensagemErro(falha), { tipo: 'error' });
            confirmarBotao.disabled = false;
            confirmarBotao.innerHTML = htmlOriginal;
        }
    });
}

/* =========================================================
   Usuários
   ========================================================= */

async function viewUsuarios(route, seq) {
    const filtros = {
        texto: route.params.get('texto') || '',
        papel: route.params.get('papel') || '',
        ativo: route.params.get('ativo') || ''
    };

    const params = { size: 100, sort: 'nome,asc' };
    if (filtros.texto) params.texto = filtros.texto;
    if (filtros.papel) params.papel = filtros.papel;
    if (filtros.ativo) params.ativo = filtros.ativo;

    const pagina = await api.listarUsuarios(params);
    if (!podeRenderizar(seq)) return;

    const usuarios = pagina.conteudo || [];
    const total = Number(pagina.totalElementos) || usuarios.length;

    const opcoesPapel = Object.entries(PAPEIS).map(([valor, rotulo]) =>
        `<option value="${valor}" ${valor === filtros.papel ? 'selected' : ''}>${rotulo}</option>`
    ).join('');

    const linhas = usuarios.map((usuario) => `
        <tr data-id="${usuario.id}">
            <td>
                <div class="user-cell">
                    ${avatarHtml(usuario.nome)}
                    <div style="min-width:0">
                        <div class="cell-title">${esc(usuario.nome)}
                            ${usuario.id === state.usuario?.id ? '<span class="badge badge-info" style="margin-left:6px">você</span>' : ''}
                        </div>
                        <div class="cell-sub">${esc(usuario.email)}</div>
                    </div>
                </div>
            </td>
            <td>${esc(usuario.departamento)}</td>
            <td>${badgePapel(usuario.papel)}</td>
            <td>${badgeAtivo(usuario.ativo)}</td>
            <td class="cell-actions">
                <button class="btn btn-sm btn-ghost" type="button" data-editar-usuario="${usuario.id}">
                    ${icon('i-edit', 'icon-sm')}Editar
                </button>
                ${usuario.id === state.usuario?.id ? '' : `<button class="btn btn-sm ${usuario.ativo ? 'btn-danger-soft' : 'btn-secondary'}" type="button" data-alternar-usuario="${usuario.id}">
                    ${icon(usuario.ativo ? 'i-slash' : 'i-check-circle', 'icon-sm')}${usuario.ativo ? 'Desativar' : 'Ativar'}
                </button>`}
            </td>
        </tr>`).join('');

    view.innerHTML = `
        <div class="page-header">
            <div>
                <h2>Usuários</h2>
                <p>${total} ${total === 1 ? 'usuário cadastrado' : 'usuários cadastrados'} na plataforma.</p>
            </div>
            <div class="page-actions">
                <button class="btn btn-primary" type="button" data-novo-usuario>${icon('i-plus')}Novo usuário</button>
            </div>
        </div>

        <section class="card" style="margin-bottom:16px">
            <form class="filters compact" id="filtros-usuarios">
                <div class="field">
                    <label for="filtro-usuario-texto">Buscar</label>
                    <div class="input-icon">
                        ${icon('i-search')}
                        <input class="input" id="filtro-usuario-texto" name="texto" type="search"
                               placeholder="Nome ou e-mail..." value="${esc(filtros.texto)}">
                    </div>
                </div>
                <div class="field">
                    <label for="filtro-usuario-papel">Papel</label>
                    <select class="select" id="filtro-usuario-papel" name="papel">
                        <option value="">Todos</option>
                        ${opcoesPapel}
                    </select>
                </div>
                <div class="field">
                    <label for="filtro-usuario-ativo">Situação</label>
                    <select class="select" id="filtro-usuario-ativo" name="ativo">
                        <option value="">Todas</option>
                        <option value="true" ${filtros.ativo === 'true' ? 'selected' : ''}>Ativos</option>
                        <option value="false" ${filtros.ativo === 'false' ? 'selected' : ''}>Inativos</option>
                    </select>
                </div>
                <div class="filter-actions">
                    <button class="btn btn-primary" type="submit">${icon('i-filter', 'icon-sm')}Filtrar</button>
                    <button class="btn btn-ghost" type="button" data-limpar>Limpar</button>
                </div>
            </form>
        </section>

        <section class="card">
            ${usuarios.length ? `<div class="table-wrap">
                <table class="table">
                    <thead><tr>
                        <th>Usuário</th>
                        <th>Departamento</th>
                        <th>Papel</th>
                        <th>Situação</th>
                        <th class="cell-actions">Ações</th>
                    </tr></thead>
                    <tbody>${linhas}</tbody>
                </table>
            </div>` : `<div class="empty">
                <div class="empty-icon">${icon('i-users')}</div>
                <h3>Nenhum usuário encontrado</h3>
                <p>Ajuste os filtros ou cadastre um novo participante.</p>
                <button class="btn btn-primary btn-sm" type="button" data-novo-usuario>Novo usuário</button>
            </div>`}
        </section>`;

    const form = $('#filtros-usuarios');

    form.addEventListener('submit', (evento) => {
        evento.preventDefault();
        const dados = new FormData(form);
        const paramsHash = new URLSearchParams();
        const texto = String(dados.get('texto') || '').trim();
        const papel = dados.get('papel') || '';
        const ativo = dados.get('ativo') || '';
        if (texto) paramsHash.set('texto', texto);
        if (papel) paramsHash.set('papel', papel);
        if (ativo) paramsHash.set('ativo', ativo);
        const query = paramsHash.toString();
        navegar(`#/usuarios${query ? `?${query}` : ''}`);
    });

    form.querySelectorAll('select').forEach((select) => {
        select.addEventListener('change', () => form.requestSubmit());
    });

    view.querySelectorAll('[data-limpar]').forEach((botao) => {
        botao.addEventListener('click', () => navegar('#/usuarios'));
    });

    view.querySelectorAll('[data-novo-usuario]').forEach((botao) => {
        botao.addEventListener('click', () => abrirFormularioUsuario(null));
    });

    view.querySelectorAll('[data-editar-usuario]').forEach((botao) => {
        botao.addEventListener('click', () => {
            const usuario = state.usuarios.find((item) => item.id === Number(botao.dataset.editarUsuario));
            if (usuario) abrirFormularioUsuario(usuario);
        });
    });

    view.querySelectorAll('[data-alternar-usuario]').forEach((botao) => {
        botao.addEventListener('click', () => {
            const usuario = state.usuarios.find((item) => item.id === Number(botao.dataset.alternarUsuario));
            if (usuario) alternarUsuario(usuario);
        });
    });
}

function abrirFormularioUsuario(usuario) {
    const editando = Boolean(usuario);
    const opcoesPapel = Object.entries(PAPEIS).map(([valor, rotulo]) =>
        `<option value="${valor}" ${usuario?.papel === valor ? 'selected' : ''}>${rotulo}</option>`
    ).join('');

    const modal = openModal({
        titulo: editando ? 'Editar usuário' : 'Novo usuário',
        subtitulo: editando ? `#${usuario.id} · ${usuario.email}` : 'Cadastre um participante do fluxo de aprovação',
        conteudo: `<form id="usuario-form" style="display:flex;flex-direction:column;gap:13px">
            <div class="field">
                <label for="usuario-nome">Nome *</label>
                <input class="input" id="usuario-nome" name="nome" type="text" required maxlength="120"
                       placeholder="Ex.: Ana Souza" value="${esc(usuario?.nome || '')}">
            </div>
            <div class="field">
                <label for="usuario-email">E-mail *</label>
                <input class="input" id="usuario-email" name="email" type="email" required maxlength="160"
                       placeholder="nome@empresa.com" value="${esc(usuario?.email || '')}">
            </div>
            <div class="field">
                <label for="usuario-departamento">Departamento *</label>
                <input class="input" id="usuario-departamento" name="departamento" type="text" required maxlength="80"
                       placeholder="Ex.: Financeiro" value="${esc(usuario?.departamento || '')}">
            </div>
            <div class="field">
                <label for="usuario-papel">Papel *</label>
                <select class="select" id="usuario-papel" name="papel" required>${opcoesPapel}</select>
                <span class="field-hint">Gestores e diretores podem receber solicitações para aprovação.</span>
            </div>
        </form>`,
        rodape: `<button class="btn btn-secondary" type="button" data-cancelar>Cancelar</button>
                 <button class="btn btn-primary" type="submit" form="usuario-form" data-salvar>
                    ${editando ? 'Salvar alterações' : 'Cadastrar usuário'}
                 </button>`
    });

    const form = modal.overlay.querySelector('#usuario-form');
    const botaoSalvar = modal.overlay.querySelector('[data-salvar]');
    const botaoCancelar = modal.overlay.querySelector('[data-cancelar]');

    botaoCancelar.addEventListener('click', () => modal.close());

    form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        if (!form.reportValidity()) return;

        const payload = {
            nome: form.querySelector('#usuario-nome').value.trim(),
            email: form.querySelector('#usuario-email').value.trim(),
            departamento: form.querySelector('#usuario-departamento').value.trim(),
            papel: form.querySelector('#usuario-papel').value
        };

        botaoSalvar.disabled = true;
        const htmlOriginal = botaoSalvar.innerHTML;
        botaoSalvar.innerHTML = '<span class="spinner"></span>Salvando...';

        try {
            if (editando) await api.atualizarUsuario(usuario.id, payload);
            else await api.criarUsuario(payload);

            modal.close();
            toast(editando ? 'Usuário atualizado.' : 'Usuário cadastrado.', { tipo: 'success' });
            await carregarUsuarios();
            sincronizarUsuarioAtual();
            atualizarUsuarioChrome();
            renderizarListaUsuarios('');
            atualizarBadgeNotificacoes();
            renderRoute();
        } catch (falha) {
            toast(mensagemErro(falha), { tipo: 'error' });
            botaoSalvar.disabled = false;
            botaoSalvar.innerHTML = htmlOriginal;
        }
    });
}

async function alternarUsuario(usuario) {
    if (usuario.id === state.usuario?.id && usuario.ativo) {
        toast('Você não pode desativar o usuário que está simulando no momento.', { tipo: 'error' });
        return;
    }

    const desativando = usuario.ativo;
    const confirmado = await confirmar({
        titulo: desativando ? 'Desativar usuário' : 'Reativar usuário',
        mensagem: desativando
            ? `${usuario.nome} será desativado e não aparecerá como opção para novas ações. O histórico existente será preservado.`
            : `${usuario.nome} voltará a ficar disponível para participar do fluxo.`,
        confirmar: desativando ? 'Desativar' : 'Reativar',
        perigo: desativando
    });
    if (!confirmado) return;

    try {
        if (desativando) await api.desativarUsuario(usuario.id);
        else await api.ativarUsuario(usuario.id);
        toast(desativando ? 'Usuário desativado.' : 'Usuário reativado.', { tipo: 'success' });
        await carregarUsuarios();
        sincronizarUsuarioAtual();
        atualizarUsuarioChrome();
        renderizarListaUsuarios('');
        renderRoute();
    } catch (falha) {
        toast(mensagemErro(falha), { tipo: 'error' });
    }
}

function sincronizarUsuarioAtual() {
    if (!state.usuario) return;
    const atualizado = state.usuarios.find((usuario) => usuario.id === state.usuario.id);
    if (atualizado) state.usuario = atualizado;
}

/* =========================================================
   Notificações
   ========================================================= */

async function viewNotificacoes(route, seq) {
    if (!state.usuario) {
        view.innerHTML = `<div class="card error-panel">
            <div class="empty-icon">${icon('i-user')}</div>
            <h3>Selecione um usuário</h3>
            <p>As notificações são individuais. Escolha um usuário no topo para abrir a caixa de entrada.</p>
        </div>`;
        return;
    }

    const [pagina, naoLidas] = await Promise.all([
        api.listarNotificacoes(state.usuario.id, { page: 0, size: 50, sort: 'criadaEm,desc' }),
        api.contarNaoLidas(state.usuario.id).catch(() => 0)
    ]);
    if (!podeRenderizar(seq)) return;

    state.naoLidas = Number(naoLidas) || 0;
    aplicarBadgeNotificacoes();

    const notificacoes = pagina.conteudo || [];
    const total = Number(pagina.totalElementos) || notificacoes.length;
    const naoLidasNaPagina = notificacoes.filter((notificacao) => !notificacao.lida).length;

    const itens = notificacoes.map((notificacao) => `
        <article class="notif-item ${notificacao.lida ? '' : 'unread'}">
            <span class="notif-dot" aria-hidden="true"></span>
            <div class="notif-content">
                <strong>${esc(notificacao.titulo)}</strong>
                <p>${esc(notificacao.mensagem)}</p>
                <div class="notif-meta">
                    <span>${dataHora(notificacao.criadaEm)}</span>
                    ${notificacao.solicitacaoId ? `<a href="#/solicitacoes/${notificacao.solicitacaoId}">Solicitação #${notificacao.solicitacaoId}</a>` : ''}
                    ${notificacao.lida && notificacao.lidaEm ? `<span>Lida em ${dataHora(notificacao.lidaEm)}</span>` : ''}
                </div>
            </div>
            <div class="notif-actions">
                ${notificacao.lida
                    ? '<span class="badge badge-neutral">Lida</span>'
                    : `<button class="btn btn-sm btn-secondary" type="button" data-marcar-lida="${notificacao.id}">Marcar como lida</button>`}
            </div>
        </article>`).join('');

    view.innerHTML = `
        <div class="page-header">
            <div>
                <h2>Notificações</h2>
                <p>${total} ${total === 1 ? 'notificação' : 'notificações'} na caixa de entrada de ${esc(state.usuario.nome)}${state.naoLidas ? ` · ${state.naoLidas} não lida(s)` : ''}.</p>
            </div>
            <div class="page-actions">
                ${state.naoLidas > 0 ? `<button class="btn btn-secondary" type="button" data-marcar-todas>${icon('i-check', 'icon-sm')}Marcar todas como lidas</button>` : ''}
                <a class="btn btn-primary" href="#/solicitacoes">${icon('i-file')}Ver solicitações</a>
            </div>
        </div>

        <section class="card">
            <div class="card-header">
                <div><h2>Caixa de entrada</h2><p class="card-subtitle">${naoLidasNaPagina} não lida(s) nesta página</p></div>
            </div>
            ${notificacoes.length ? `<div class="notif-list">${itens}</div>` : `<div class="empty">
                <div class="empty-icon">${icon('i-bell')}</div>
                <h3>Nenhuma notificação</h3>
                <p>Quando houver movimentações no fluxo, os avisos aparecerão aqui.</p>
                <a class="btn btn-secondary btn-sm" href="#/solicitacoes">Explorar solicitações</a>
            </div>`}
        </section>`;

    view.querySelectorAll('[data-marcar-lida]').forEach((botao) => {
        botao.addEventListener('click', async () => {
            botao.disabled = true;
            try {
                await api.marcarNotificacaoLida(Number(botao.dataset.marcarLida), state.usuario.id);
                toast('Notificação marcada como lida.', { tipo: 'success' });
                await atualizarBadgeNotificacoes();
                renderRoute();
            } catch (falha) {
                toast(mensagemErro(falha), { tipo: 'error' });
                botao.disabled = false;
            }
        });
    });

    const marcarTodas = view.querySelector('[data-marcar-todas]');
    marcarTodas?.addEventListener('click', async () => {
        const ids = notificacoes.filter((notificacao) => !notificacao.lida).map((notificacao) => notificacao.id);
        if (!ids.length) return;
        marcarTodas.disabled = true;
        marcarTodas.innerHTML = '<span class="spinner"></span>Marcando...';
        const resultados = await Promise.allSettled(
            ids.map((id) => api.marcarNotificacaoLida(id, state.usuario.id))
        );
        const sucesso = resultados.filter((resultado) => resultado.status === 'fulfilled').length;
        toast(`${sucesso} de ${ids.length} notificações marcadas como lidas.`, { tipo: 'success' });
        await atualizarBadgeNotificacoes();
        renderRoute();
    });
}

/* =========================================================
   Eventos globais e inicialização
   ========================================================= */

function configurarEventosGlobais() {
    window.addEventListener('hashchange', renderRoute);

    $('#menu-btn')?.addEventListener('click', abrirSidebarMobile);
    $('#sidebar-backdrop')?.addEventListener('click', fecharSidebarMobile);

    $('#theme-btn')?.addEventListener('click', alternarTema);
    $('#refresh-btn')?.addEventListener('click', () => {
        renderRoute();
        atualizarBadgeNotificacoes();
    });
    $('#notif-btn')?.addEventListener('click', () => navegar('#/notificacoes'));

    $('#user-chip')?.addEventListener('click', (evento) => {
        evento.stopPropagation();
        alternarDropdown();
    });

    $('#sidebar-user')?.addEventListener('click', (evento) => {
        evento.stopPropagation();
        fecharSidebarMobile();
        alternarDropdown(true);
    });

    $('#user-search')?.addEventListener('input', (evento) => {
        renderizarListaUsuarios(evento.target.value);
    });

    document.addEventListener('click', (evento) => {
        if (!evento.target.closest('#user-chip, #sidebar-user, #user-dropdown')) {
            fecharDropdown();
        }
        if (evento.target.closest('.nav-item')) {
            fecharSidebarMobile();
        }
    });

    document.addEventListener('keydown', (evento) => {
        if (evento.key !== 'Escape') return;
        if (modalAtual) {
            closeModal();
            return;
        }
        fecharDropdown();
        fecharSidebarMobile();
    });

    document.addEventListener('click', (evento) => {
        const recarregar = evento.target.closest('[data-recarregar]');
        if (recarregar) {
            iniciar();
            return;
        }
        const assumir = evento.target.closest('[data-assumir-usuario]');
        if (assumir) {
            selecionarUsuario(Number(assumir.dataset.assumirUsuario));
        }
    });

    setInterval(() => {
        if (state.usuario && !document.hidden) atualizarBadgeNotificacoes();
    }, 60000);
}

async function iniciar() {
    const view = $('#view');
    view.innerHTML = loadingHtml('Conectando à API...');

    try {
        await carregarUsuarios();
        restaurarUsuario();
        await atualizarBadgeNotificacoes();

        if (!location.hash) {
            history.replaceState(null, '', '#/painel');
        }
        await renderRoute();
    } catch (erro) {
        console.error(erro);
        view.innerHTML = erroHtml(erro);
    }
}

function iniciarAplicacao() {
    configurarEventosGlobais();
    aplicarTema();
    iniciar();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', iniciarAplicacao);
} else {
    iniciarAplicacao();
}
