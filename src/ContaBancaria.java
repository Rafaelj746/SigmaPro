import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ContaBancaria {
    private Usuario titular;
    private BigDecimal saldo;
    private List<Transacao> extrato;

    public ContaBancaria(Usuario titular, BigDecimal saldoInicial) {
        if (saldoInicial != null && saldoInicial.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Saldo inicial não pode ser negativo.");
        }
        this.titular = titular;
        this.saldo = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
        this.extrato = new ArrayList<>();
    }

    public void depositar(BigDecimal valor) {
        Transacao t = new Transacao("DEPÓSITO", valor, this, null);
        t.aprovar(titular, "Auto-aprovação de depósito"); // Efetiva imediatamente
        extrato.add(t);
    }


    public Transacao solicitarSaque(BigDecimal valor) {
        if (this.saldo.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para solicitar o saque");
        }
        Transacao t = new Transacao("SAQUE", valor, this, null);
        extrato.add(t);
        return t;
    }


    public Transacao solicitarTransferencia(ContaBancaria destino, BigDecimal valor) {
        if (destino == null) {
            throw new IllegalArgumentException("A conta de destino não pode ser nula.");
        }
        if (this.saldo.compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para a transferência.");
        }
        Transacao t = new Transacao("TRANSFERÊNCIA", valor, this, destino);
        extrato.add(t);
        destino.adicionarAoExtrato(t);
        return t;
    }

    // Métodos internos chamados pela classe Transacao quando a mesma é efetivada
    protected void executarDebito(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
    }

    protected void executarCredito(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    protected void adicionarAoExtrato(Transacao t) {
        this.extrato.add(t);
    }

    public void imprimirExtrato() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("\n==============================================================");
        System.out.println("EXTRATO DETALHADO - Titular: " + titular.getNome() + " (CPF: " + titular.getCpf() + ")");
        System.out.println("==============================================================");

        if (extrato.isEmpty()) {
            System.out.println("Nenhuma transação realizada.");
        } else {
            for (Transacao t : extrato) {
                String dataStr = (t.getDataCriacao() != null) ? t.getDataCriacao().format(fmt) : "Data N/D";
                System.out.println("\n• [" + t.getStatus() + "] " + t.getTipo() + " | Valor: R$ " + t.getValor() + " | Solicitada em: " + dataStr);

                if (t.getDestino() != null) {
                    Usuario dest = t.getDestino().getTitular();
                    System.out.println("  Destinatário: " + dest.getNome() + " (CPF: " + dest.getCpf() + ")");
                }

                System.out.println("  Fluxo de Decisões:");
                if (t.getHistorico() == null || t.getHistorico().isEmpty()) {
                    System.out.println("    - Aguardando análise.");
                } else {
                    for (RegistroDecisao registro : t.getHistorico()) {
                        System.out.println("    - " + registro.toString());
                    }
                }
                System.out.println("--------------------------------------------------------------");
            }
        }
        System.out.println("SALDO ATUAL: R$ " + this.saldo);
        System.out.println("==============================================================\n");
    }

    public Usuario getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
}