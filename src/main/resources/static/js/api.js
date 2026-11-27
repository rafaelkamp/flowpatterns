/**
 * Cliente HTTP da API do FlowPatterns.
 *
 * Todas as chamadas usam o mesmo formato de erro (Problem Details / RFC 9457),
 * convertido para uma instância de ApiError com mensagem amigável e lista de
 * erros de validação por campo.
 */

const BASE_URL = '/api/v1';

export class ApiError extends Error {
    constructor(message, status, problem) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.problem = problem;
        this.fieldErrors = Array.isArray(problem?.erros) ? problem.erros : [];
    }
}

function buildUrl(path, params) {
    const url = new URL(`${BASE_URL}${path}`, window.location.origin);
    if (params) {
        for (const [chave, valor] of Object.entries(params)) {
            if (valor === undefined || valor === null || valor === '') {
                continue;
            }
            url.searchParams.set(chave, String(valor));
        }
    }
    return url;
}

async function request(path, { method = 'GET', body, params } = {}) {
    let response;
    try {
        response = await fetch(buildUrl(path, params), {
            method,
            headers: {
                Accept: 'application/json',
                ...(body !== undefined ? { 'Content-Type': 'application/json' } : {})
            },
            body: body !== undefined ? JSON.stringify(body) : undefined
        });
    } catch (erro) {
        throw new ApiError(
            'Não foi possível conectar à API. Verifique se o servidor está em execução.',
            0,
            null
        );
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get('content-type') || '';
    let dados = null;
    if (contentType.includes('json')) {
        dados = await response.json().catch(() => null);
    } else {
        dados = await response.text().catch(() => '');
    }

    if (!response.ok) {
        const problem = dados && typeof dados === 'object' ? dados : null;
        const mensagem = problem?.detail
            || problem?.title
            || (typeof dados === 'string' && dados.trim())
            || `Erro ${response.status} ao processar a requisição.`;
        throw new ApiError(mensagem, response.status, problem);
    }

    return dados;
}

export const api = {
    // Painel
    painel: () => request('/painel'),

    // Solicitações
    listarSolicitacoes: (params) => request('/solicitacoes', { params }),
    buscarSolicitacao: (id) => request(`/solicitacoes/${id}`),
    criarSolicitacao: (dados) => request('/solicitacoes', { method: 'POST', body: dados }),
    atualizarSolicitacao: (id, dados) => request(`/solicitacoes/${id}`, { method: 'PUT', body: dados }),
    excluirSolicitacao: (id) => request(`/solicitacoes/${id}`, { method: 'DELETE' }),
    enviarSolicitacao: (id) => request(`/solicitacoes/${id}/envio`, { method: 'POST' }),
    aprovarSolicitacao: (id, atorId, observacao) => request(`/solicitacoes/${id}/aprovacao`, {
        method: 'POST',
        body: { atorId, observacao: observacao || null }
    }),
    rejeitarSolicitacao: (id, atorId, observacao) => request(`/solicitacoes/${id}/rejeicao`, {
        method: 'POST',
        body: { atorId, observacao: observacao || null }
    }),
    cancelarSolicitacao: (id, atorId, motivo) => request(`/solicitacoes/${id}/cancelamento`, {
        method: 'POST',
        body: { atorId, motivo }
    }),
    executarLote: (dados) => request('/solicitacoes/acoes-lote', { method: 'POST', body: dados }),
    historico: (id) => request(`/solicitacoes/${id}/historico`),

    // Usuários
    listarUsuarios: (params) => request('/usuarios', { params }),
    buscarUsuario: (id) => request(`/usuarios/${id}`),
    criarUsuario: (dados) => request('/usuarios', { method: 'POST', body: dados }),
    atualizarUsuario: (id, dados) => request(`/usuarios/${id}`, { method: 'PUT', body: dados }),
    ativarUsuario: (id) => request(`/usuarios/${id}/ativacao`, { method: 'POST' }),
    desativarUsuario: (id) => request(`/usuarios/${id}`, { method: 'DELETE' }),

    // Notificações
    listarNotificacoes: (destinatarioId, params = {}) => request('/notificacoes', {
        params: { destinatarioId, ...params }
    }),
    contarNaoLidas: (destinatarioId) => request('/notificacoes/nao-lidas', {
        params: { destinatarioId }
    }),
    marcarNotificacaoLida: (id, destinatarioId) => request(`/notificacoes/${id}/leitura`, {
        method: 'POST',
        params: { destinatarioId }
    })
};
