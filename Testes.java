import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class Testes {
    Usuario ana;
    Usuario bruno;
    ContaBancaria conta;
    @TempDir Path pasta;

    Usuario usuario(String nome, String cpf) {
        return new Usuario(nome, cpf, LocalDate.of(1990, 1, 1), "senha123");
    }

    @BeforeEach void preparar() {
        ana = usuario("Ana", "111.222.333-44");
        bruno = usuario("Bruno", "555.666.777-88");
        conta = new ContaBancaria(usuario("Cliente", "123.456.789-00"), new BigDecimal("50000.00"));
    }

    Transacao saque(String valor) {
        return conta.solicitarSaque(new BigDecimal(valor));
    }

    // Unidade / RF01: entradas inválidas.
    @Test void ct01NaoAceitaZeroOuNegativo() {
        assertThrows(IllegalArgumentException.class, () -> saque("0.00"));
        assertThrows(IllegalArgumentException.class, () -> saque("-0.01"));
    }

    // Unidade / RF01: exatamente 10 mil precisa de uma aprovação.
    @Test void ct02DezMilLiberaComUmaAprovacao() {
        Transacao t = saque("10000.00");
        t.aprovar(ana, "Conferido");
        assertEquals(StatusTransacao.EFETIVADA, t.getStatus());
    }

    // Unidade / RF02: um centavo acima muda a regra.
    @Test void ct03AcimaDeDezMilContinuaPendente() {
        Transacao t = saque("10000.01");
        t.aprovar(ana, "Primeira análise");
        assertEquals(StatusTransacao.PENDENTE, t.getStatus());
        assertEquals(new BigDecimal("50000.00"), conta.getSaldo());
    }

    // Integração / RF02: transação e contas atualizam os saldos juntas.
    @Test void ct04TransferenciaSoMoveSaldoDepoisDeDuasPessoas() {
        ContaBancaria destino = new ContaBancaria(bruno, new BigDecimal("100.00"));
        Transacao t = conta.solicitarTransferencia(destino, new BigDecimal("15000.00"));
        t.aprovar(ana, "Primeira análise");
        assertEquals(new BigDecimal("100.00"), destino.getSaldo());
        t.aprovar(bruno, "Segunda análise");
        assertEquals(StatusTransacao.EFETIVADA, t.getStatus());
        assertEquals(new BigDecimal("35000.00"), conta.getSaldo());
        assertEquals(new BigDecimal("15100.00"), destino.getSaldo());
    }

    // Unidade / RF03: nem outro objeto com o mesmo CPF pode repetir.
    @Test void ct05NaoAceitaAprovacaoRepetidaDoMesmoCpf() {
        Transacao t = saque("15000.00");
        t.aprovar(ana, "Primeira análise");
        Usuario mesmaPessoa = usuario("Ana", ana.getCpf());
        assertThrows(IllegalStateException.class, () -> t.aprovar(mesmaPessoa, "Repetida"));
        assertEquals(StatusTransacao.PENDENTE, t.getStatus());
        assertEquals(1, t.getHistorico().size());
    }

    // Unidade / RF04: usuário bloqueado não aprova.
    @Test void ct06UsuarioBloqueadoNaoAprova() {
        Transacao t = saque("1000.00");
        ana.setBloqueado(true);
        assertThrows(IllegalStateException.class, () -> t.aprovar(ana, "Tentativa"));
        assertEquals(StatusTransacao.PENDENTE, t.getStatus());
        assertTrue(t.getHistorico().isEmpty());
    }

    // Unidade / RF05: rejeição impede a liberação.
    @Test void ct07RejeitadaNaoPodeSerAprovada() {
        Transacao t = saque("15000.00");
        t.rejeitar(ana, "Revisar documentos");
        assertThrows(IllegalStateException.class, () -> t.aprovar(bruno, "Tentativa"));
        assertEquals(StatusTransacao.REJEITADA, t.getStatus());
    }

    // RF05: reabrir exige motivo e duas novas aprovações, preservando o passado.
    @Test void ct08ReaberturaExigeMotivoENovasAprovacoes() {
        Transacao t = saque("15000.00");
        t.aprovar(ana, "Primeira análise");
        t.rejeitar(bruno, "Revisar");
        RegistroDecisao primeira = t.getHistorico().getFirst();
        assertThrows(IllegalArgumentException.class, () -> t.solicitarNovaAnalise(" "));
        assertEquals(StatusTransacao.REJEITADA, t.getStatus());
        t.solicitarNovaAnalise(bruno, "Documentos corrigidos");
        assertEquals(StatusTransacao.PENDENTE, t.getStatus());
        assertSame(primeira, t.getHistorico().getFirst());
        assertEquals(3, t.getHistorico().size());
        t.aprovar(bruno, "Nova primeira aprovação");
        assertEquals(StatusTransacao.PENDENTE, t.getStatus());
        t.aprovar(usuario("Diego", "222.333.444-55"), "Nova segunda aprovação");
        assertEquals(StatusTransacao.EFETIVADA, t.getStatus());
    }

    // Unidade / RF06: cada decisão identifica quem decidiu e quando.
    @Test void ct09HistoricoRegistraUsuarioDataEAcao() {
        Transacao t = saque("15000.00");
        LocalDateTime antes = LocalDateTime.now();
        t.rejeitar(ana, "Revisar");
        t.solicitarNovaAnalise(bruno, "Documentos corrigidos");
        RegistroDecisao d = t.getHistorico().getLast();
        assertEquals(bruno, d.getAvaliador());
        assertEquals(RegistroDecisao.Tipo.NOVA_ANALISE, d.getTipo());
        assertEquals("Documentos corrigidos", d.getJustificativa());
        assertFalse(d.getDataHora().isBefore(antes));
        assertFalse(d.getDataHora().isAfter(LocalDateTime.now()));
    }

    // Integração: arquivo local e conta preservam estado e histórico.
    @Test void ct10SalvarECarregarConta() throws Exception {
        Transacao t = saque("15000.00");
        t.aprovar(ana, "Primeira análise");
        ArquivoSessao arquivo = new ArquivoSessao(pasta.resolve("conta.bin"));
        arquivo.salvar(conta);
        ContaBancaria carregada = arquivo.carregar().orElseThrow();
        assertEquals(new BigDecimal("50000.00"), carregada.getSaldo());
        assertEquals(StatusTransacao.PENDENTE, carregada.getExtrato().getFirst().getStatus());
        assertEquals(1, carregada.getExtrato().getFirst().getHistorico().size());
    }

    // Sistema: cadastro, transferência, confirmação, extrato e saída pela Main.
    @Test void ct11SistemaTransferenciaPreservaDoisMil() throws Exception {
        String console = executarTerminal(cadastro()
            + "3\nDestinatario\n321.654.987-00\n2000.00\nS\n4\n6\n");
        assertTrue(console.contains("Valor: R$ 2000.00"), console);
        assertTrue(console.contains("SALDO ATUAL: R$ 48000.00"), console);
    }

    // Aceitação automatizada: duas pessoas ativas e distintas autorizam a transação.
    @Test void ct12CriterioDeDuasPessoasAtivasEDistintas() throws Exception {
        String console = executarTerminal(cadastro()
            + "2\n15000.00\n"
            + "5\n0\n3\n1\nBloqueado\n"
            + "5\n0\n1\n1\nPrimeira\n"
            + "5\n0\n1\n1\nRepetida\n"
            + "5\n0\n2\n1\nSegunda\n4\n6\n");
        assertTrue(console.contains("usuário bloqueado"), console);
        assertTrue(console.contains("mesmo usuário não pode aprovar duas vezes"), console);
        assertTrue(console.contains("[EFETIVADA] SAQUE"), console);
        assertTrue(console.contains("SALDO ATUAL: R$ 35000.00"), console);
    }

    String cadastro() {
        return "Cliente\n123.456.789-00\n01/01/1990\nsenha123\n50000.00\nsenha123\n";
    }

    // Este apoio digita no terminal e captura a saída do programa automaticamente.
    String executarTerminal(String entrada) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        Path classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path saida = pasta.resolve("console.txt");
        Process processo = new ProcessBuilder(java.toString(),
            "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
            "-Dsigmapro.dados=" + pasta.resolve("sessao.bin"), "-cp", classes.toString(), "Main")
            .redirectErrorStream(true).redirectOutput(saida.toFile()).start();
        try {
            try (var teclado = processo.getOutputStream()) {
                teclado.write(entrada.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(processo.waitFor(15, TimeUnit.SECONDS), "O programa deve encerrar.");
            String console = Files.readString(saida, StandardCharsets.UTF_8);
            assertEquals(0, processo.exitValue(), console);
            return console;
        } finally {
            if (processo.isAlive()) processo.destroyForcibly();
        }
    }
}
