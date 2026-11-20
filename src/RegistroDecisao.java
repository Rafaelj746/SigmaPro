import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroDecisao {
    private Usuario avaliador;
    private boolean aprovada;
    private String justificativa;
    private LocalDateTime dataHora;

    public RegistroDecisao(Usuario avaliador, boolean aprovada, String justificativa) {
        this.avaliador = avaliador;
        this.aprovada = aprovada;
        this.justificativa = (justificativa == null || justificativa.isBlank()) ? "Sem justificativa" : justificativa;
        this.dataHora = LocalDateTime.now();
    }

    public Usuario getAvaliador() { return avaliador; }
    public boolean isAprovada() { return aprovada; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getDataHora() { return dataHora; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String acao = aprovada ? "APROVADA" : "REJEITADA";
        return String.format("[%s] %s por %s (CPF: %s) | Justificativa: \"%s\"",
                dataHora.format(fmt),
                acao,
                avaliador.getNome(),
                avaliador.getCpf(),
                justificativa);
    }
}