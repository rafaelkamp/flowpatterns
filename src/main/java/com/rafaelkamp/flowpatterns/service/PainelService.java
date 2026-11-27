package com.rafaelkamp.flowpatterns.service;

import com.rafaelkamp.flowpatterns.domain.solicitacao.Solicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.StatusSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TipoSolicitacao;
import com.rafaelkamp.flowpatterns.domain.solicitacao.TiposSolicitacao;
import com.rafaelkamp.flowpatterns.dto.painel.PainelResponse;
import com.rafaelkamp.flowpatterns.dto.solicitacao.SolicitacaoResumoResponse;
import com.rafaelkamp.flowpatterns.repository.SolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PainelService {

    private static final List<StatusSolicitacao> STATUS_EM_ANDAMENTO = List.of(
            StatusSolicitacao.PENDENTE, StatusSolicitacao.EM_ANALISE);

    private final SolicitacaoRepository solicitacaoRepository;
    private final Clock clock;

    public PainelService(SolicitacaoRepository solicitacaoRepository, Clock clock) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PainelResponse montar() {
        Instant agora = clock.instant();

        Map<StatusSolicitacao, Long> porStatus = new EnumMap<>(StatusSolicitacao.class);
        for (StatusSolicitacao status : StatusSolicitacao.values()) {
            porStatus.put(status, solicitacaoRepository.countByStatus(status));
        }

        Map<TipoSolicitacao, Long> porTipo = new EnumMap<>(TipoSolicitacao.class);
        for (TipoSolicitacao tipo : TipoSolicitacao.values()) {
            long total = solicitacaoRepository.count((root, query, cb) ->
                    cb.equal(root.type(), TiposSolicitacao.classeDe(tipo)));
            porTipo.put(tipo, total);
        }

        long emAndamento = solicitacaoRepository.countByStatusIn(STATUS_EM_ANDAMENTO);
        long atrasadas = solicitacaoRepository.countByStatusInAndPrazoSlaBefore(STATUS_EM_ANDAMENTO, agora);
        double tempoMedioAprovacaoHoras = tempoMedioAprovacaoHoras(agora);
        List<SolicitacaoResumoResponse> ultimas = solicitacaoRepository.findTop5ByOrderByCriadoEmDesc().stream()
                .map(solicitacao -> SolicitacaoResumoResponse.from(solicitacao, agora))
                .toList();

        return new PainelResponse(solicitacaoRepository.count(), porStatus, porTipo, emAndamento,
                atrasadas, tempoMedioAprovacaoHoras, ultimas);
    }

    private double tempoMedioAprovacaoHoras(Instant agora) {
        List<Solicitacao> aprovadas = solicitacaoRepository.findByStatusAndDataDecisaoAfter(
                StatusSolicitacao.APROVADA, agora.minus(30, ChronoUnit.DAYS));
        if (aprovadas.isEmpty()) {
            return 0d;
        }
        double totalHoras = aprovadas.stream()
                .filter(solicitacao -> solicitacao.getDataEnvio() != null && solicitacao.getDataDecisao() != null)
                .mapToDouble(solicitacao -> Duration.between(
                        solicitacao.getDataEnvio(), solicitacao.getDataDecisao()).toMinutes() / 60d)
                .sum();
        return Math.round(totalHoras / aprovadas.size() * 100d) / 100d;
    }
}
