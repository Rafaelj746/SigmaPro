import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroDecisao {
    private Usuario avaliador;
    private LocalDateTime dataHora;
    private boolean aprovado;
    private String justificativa;

    public RegistroDecisao(Usuario avaliador, boolean aprovado, String justificativa) {
        this.avaliador = avaliador;
        this.aprovado = aprovado;
        this.justificativa = justificativa;
        this.dataHora = LocalDateTime.now();
    }

    @Override
    public String toString() {
        String acao = aprovado ? "APROVOU" : "REJEITOU";
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                " | " + avaliador.getNome() + " " + acao + " | Motivo: " + justificativa;
    }
}