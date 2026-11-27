package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.historico.HistoricoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.historico.RegistradorHistorico;
import com.rafaelkamp.flowpatterns.domain.historico.TipoEventoHistorico;
import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.usuario.Usuario;
import com.rafaelkamp.flowpatterns.repository.HistoricoSolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class HistoricoService implements RegistradorHistorico {

    private final HistoricoSolicitacaoRepository historicoRepository;
    private final Clock clock;

    public HistoricoService(HistoricoSolicitacaoRepository historicoRepository, Clock clock) {
        this.historicoRepository = historicoRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void registrar(Solicitacao solicitacao, TipoEventoHistorico tipoEvento,
                          StatusSolicitacao statusAnterior, StatusSolicitacao statusNovo,
                          Usuario ator, String detalhe) {
        historicoRepository.save(new HistoricoSolicitacao(solicitacao, tipoEvento, statusAnterior,
                statusNovo, ator, detalhe, clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<HistoricoSolicitacao> listar(Long solicitacaoId) {
        return historicoRepository.findBySolicitacaoIdOrderByOcorridoEmAsc(solicitacaoId);
    }
}
