import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Transacao {
        private String tipo; // "SAQUE", "DEPÓSITO", "TRANSFERÊNCIA"
        private BigDecimal valor;
        private ContaBancaria origem;
        private ContaBancaria destino;
        private StatusTransacao status;
        private LocalDateTime dataCriacao;

        private Set<Usuario> usuariosQueAprovaram;
        private List<RegistroDecisao> historico;

        public Transacao(String tipo, BigDecimal valor, ContaBancaria origem, ContaBancaria destino) {
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("O valor da transação deve ser positivo.");
            }
            this.tipo = tipo;
            this.valor = valor;
            this.origem = origem;
            this.destino = destino;
            this.status = StatusTransacao.PENDENTE;
            this.dataCriacao = LocalDateTime.now();
            this.usuariosQueAprovaram = new HashSet<>();
            this.historico = new ArrayList<>();
        }

        public void aprovar(Usuario avaliador, String justificativa) {
            if (status == StatusTransacao.EFETIVADA) {
                throw new IllegalStateException("Transação já foi efetivada.");
            }
            if (status == StatusTransacao.REJEITADA) {
                throw new IllegalStateException("Impedido liberação de transação rejeitada . Solicite nova análise.");
            }
            if (avaliador.isBloqueado()) {
                throw new IllegalStateException("Impedido aprovação por usuário bloqueado .");
            }
            if (usuariosQueAprovaram.contains(avaliador)) {
                throw new IllegalStateException("O mesmo usuário não pode aprovar duas vezes.");
            }

            usuariosQueAprovaram.add(avaliador);
            historico.add(new RegistroDecisao(avaliador, true, justificativa)); // RF06

            verificarEfetivacao();
        }

        public void rejeitar(Usuario avaliador, String justificativa) {
            if (status == StatusTransacao.EFETIVADA) {
                throw new IllegalStateException("Transação já efetivada não pode ser rejeitada.");
            }
            this.status = StatusTransacao.REJEITADA;
            historico.add(new RegistroDecisao(avaliador, false, justificativa)); // RF06
        }

        public void solicitarNovaAnalise(String justificativa) {
            if (status != StatusTransacao.REJEITADA) {
                throw new IllegalStateException("Apenas transações rejeitadas podem solicitar nova análise.");
            }
            this.status = StatusTransacao.PENDENTE;
            this.usuariosQueAprovaram.clear();
            this.historico.add(new RegistroDecisao(origem.getTitular(), true, "Solicitação de nova análise: " + justificativa));
        }

    private void verificarEfetivacao() {

        int aprovacoesNecessarias = 1;

        if (!tipo.equals("DEPÓSITO") && valor.compareTo(new BigDecimal("10000.00")) > 0) {
            aprovacoesNecessarias = 2;
        }

        if (usuariosQueAprovaram.size() >= aprovacoesNecessarias) {
            this.status = StatusTransacao.EFETIVADA;

            if (tipo.equals("SAQUE") || tipo.equals("TRANSFERÊNCIA")) {
                origem.executarDebito(valor);
            }
            if (tipo.equals("DEPÓSITO")) {
                origem.executarCredito(valor);
            }
            if (tipo.equals("TRANSFERÊNCIA") && destino != null) {
                destino.executarCredito(valor);
            }
        }
    }
        public StatusTransacao getStatus() { return status; }
        public String getTipo() { return tipo; }
        public BigDecimal getValor() { return valor; }
        public LocalDateTime getDataCriacao(){ return dataCriacao; }
        public List<RegistroDecisao> getHistorico() { return historico; }
        public ContaBancaria getDestino() { return destino; }
    }
