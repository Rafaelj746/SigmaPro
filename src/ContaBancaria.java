import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ContaBancaria {
    private Usuario titular;
    private BigDecimal saldo;
    private List<Transacao> extrato;

    public ContaBancaria(Usuario titular, BigDecimal saldoInicial) {
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
        System.out.println("\n=== EXTRATO DA CONTA: " + titular.getNome() + " ===");
        for (Transacao t : extrato) {
            System.out.println("[" + t.getStatus() + "] " + t.getTipo() + " - R$ " + t.getValor());
        }
        System.out.println("SALDO ATUAL: R$ " + this.saldo);
        System.out.println("=========================================\n");
    }

    public Usuario getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
}