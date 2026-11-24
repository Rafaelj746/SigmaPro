import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeParseException;

void main(){
        Scanner scanner = new Scanner(System.in);
        Usuario cliente = new Usuario();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("=========================================");
        System.out.println("      BEM-VINDO AO APROVAPAY MINI        ");
        System.out.println("=========================================");
        System.out.println("Vamos abrir a sua conta cliente.");

    while (true) {
        try {
            System.out.print("\nDigite seu Nome: ");
            cliente.setNome(scanner.nextLine());
            break;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
    while (true) {
        try {
            System.out.print("Digite seu CPF (000.000.000-00): ");
            cliente.setCpf(scanner.nextLine());
            break;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
    while (true) {
        try {
            System.out.print("Data de Nascimento (DD/MM/AAAA): ");
            String dataStr = scanner.nextLine();

            LocalDate dataNascimento = null;
            if (!dataStr.isBlank()) {
                try {
                    dataNascimento = LocalDate.parse(dataStr.trim(), formatter);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Formato de data inválido. Use DD/MM/AAAA.");
                }
            }

            cliente.setDataNascimento(dataNascimento);
            break;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    while (true) {
        try {
            System.out.print("Crie uma Senha (mín. 6 caracteres): ");
            cliente.setSenha(scanner.nextLine());
            break;
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
    System.out.println("✅ Usuário criado com sucesso!");

        ContaBancaria conta = null;
        while (conta == null) {
            try {
                System.out.print("\nQual será o saldo inicial da sua conta? R$ ");
                String saldoStr = scanner.nextLine().replace(",", ".");
                System.out.print("\nDigite sua senha: ");
                String senha = scanner.nextLine();
                if(senha.equals(cliente.getSenha())){
                conta = new ContaBancaria(cliente, new BigDecimal(saldoStr));
                System.out.println("✅ Conta bancária criada!");}
                else{
                    System.out.println("Senha incorreta tente de novo");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Digite um valor válido.");
            }
            catch (IllegalArgumentException e){
                System.out.println("❌ Erro: " + e.getMessage());
            }
        }

        // GERENTES PARA TESTAR AS APROVAÇÕES
        Usuario gerente1 = new Usuario("Gerente Ana", "111.222.333-44", LocalDate.of(1985, 5, 10), "senha123");
        Usuario gerente2 = new Usuario("Gerente Carlos", "555.666.777-88", LocalDate.of(1980, 8, 20), "senha123");
        Usuario gerenteBloqueado = new Usuario("Gerente Pedro BLOQUEADO", "999.888.777-66", LocalDate.of(1992, 2, 2), "senha123");
        gerenteBloqueado.setBloqueado(true);

        List<Transacao> transacoesPendentes = new ArrayList<>();

    // 4. MENU INTERATIVO
    int opcao = 0;
    while (opcao != 6) {
        System.out.println("\n=========================================");
        System.out.println("Olá, " + cliente.getNome() + " | Saldo: R$ " + conta.getSaldo());
        System.out.println("1. Depositar (Aprovação Automática)");
        System.out.println("2. Solicitar Saque (Vai para Análise)");
        System.out.println("3. Solicitar Transferência (Vai para Análise)");
        System.out.println("4. Ver Extrato da Conta");
        System.out.println("5. Painel de Gerência (Aprovar/Rejeitar Transações)");
        System.out.println("6. Sair");
        System.out.print("Escolha uma opção: ");

        try {
            opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1:
                    System.out.print("Qual valor deseja depositar? R$ ");
                    BigDecimal valorDep = new BigDecimal(scanner.nextLine().replace(",", "."));
                    conta.depositar(valorDep);
                    System.out.println("✅ Depósito efetivado na hora!");
                    break;

                case 2:
                    System.out.print("Qual valor deseja sacar? R$ ");
                    BigDecimal valorSaq = new BigDecimal(scanner.nextLine().replace(",", "."));
                    Transacao t = conta.solicitarSaque(valorSaq);

                    // Verifica se é até R$ 10.000,00 inclusive
                    if (valorSaq.compareTo(new BigDecimal("10000.00")) <= 0) {
                        System.out.print("Confirma o SAQUE no valor de R$ " + valorSaq + "? (S/N): ");
                        String confirmacao = scanner.nextLine().trim();

                        if (confirmacao.equalsIgnoreCase("S") || confirmacao.equalsIgnoreCase("SIM")) {
                            t.aprovar(cliente, "Confirmado pelo próprio usuário");
                            System.out.println("✅ Saque confirmado e efetivado!");
                        } else {
                            t.rejeitar(cliente, "Cancelado pelo usuário na confirmação");
                            System.out.println("❌ Saque cancelado.");
                        }
                    } else {
                        transacoesPendentes.add(t);
                        System.out.println("⏳ Valor acima de R$ 10.000,00. Saque enviado para o painel de aprovação da gerência!");
                    }
                    break;

                case 3:
                    Usuario usuarioDestino = new Usuario();

                    // 1. Lê e valida o Nome do destinatário
                    while (true) {
                        try {
                            System.out.print("Nome de quem vai receber a transferência: ");
                            usuarioDestino.setNome(scanner.nextLine());
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println("❌ Erro: " + e.getMessage());
                        }
                    }

                    // 2. Lê e valida o CPF do destinatário
                    while (true) {
                        try {
                            System.out.print("CPF do destinatário (000.000.000-00): ");
                            usuarioDestino.setCpf(scanner.nextLine());
                            break;
                        } catch (IllegalArgumentException e) {
                            System.out.println("❌ Erro: " + e.getMessage());
                        }
                    }

                    ContaBancaria contaDestino = new ContaBancaria(usuarioDestino, BigDecimal.ZERO);

                    System.out.print("Qual valor deseja transferir? R$ ");
                    BigDecimal valorDigitado = new BigDecimal(scanner.nextLine().replace(",", "."));
                    BigDecimal valorTransf = aplicarAjusteTarifario(valorDigitado);

                    Transacao transf = conta.solicitarTransferencia(contaDestino, valorTransf);

                    // Verifica se é até R$ 10.000,00 inclusive
                    if (valorTransf.compareTo(new BigDecimal("10000.00")) <= 0) {
                        System.out.print("Confirma a TRANSFERÊNCIA de R$ " + valorTransf + " para " + usuarioDestino.getNome() + " (CPF: " + usuarioDestino.getCpf() + ")? (S/N): ");
                        String confirmacao = scanner.nextLine().trim();

                        if (confirmacao.equalsIgnoreCase("S") || confirmacao.equalsIgnoreCase("SIM")) {
                            transf.aprovar(cliente, "Confirmado pelo próprio usuário");
                            System.out.println("✅ Transferência para " + usuarioDestino.getNome() + " confirmada e efetivada!");
                        } else {
                            transf.rejeitar(cliente, "Cancelado pelo usuário na confirmação");
                            System.out.println("❌ Transferência cancelada.");
                        }
                    } else {
                        transacoesPendentes.add(transf);
                        System.out.println("⏳ Valor acima de R$ 10.000,00. Transferência enviada para análise da gerência!");
                    }
                    break;

                case 4:
                    conta.imprimirExtrato();
                    break;

                case 5:
                    // PAINEL DE APROVAÇÃO
                    if (transacoesPendentes.isEmpty()) {
                        System.out.println("✅ Nenhuma transação pendente no momento.");
                        break;
                    }

                    System.out.println("\n--- TRANSAÇÕES AGUARDANDO ANÁLISE ---");
                    for (int i = 0; i < transacoesPendentes.size(); i++) {
                        Transacao pendente = transacoesPendentes.get(i);
                        String destinoStr = (pendente.getDestino() != null) ? " para " + pendente.getDestino().getTitular().getNome() : "";
                        System.out.println(i + " -> " + pendente.getTipo() + destinoStr + " de R$ " + pendente.getValor() + " | Status: " + pendente.getStatus());
                    }

                    System.out.print("Digite o número da transação para analisar: ");
                    int indice = Integer.parseInt(scanner.nextLine());
                    Transacao selecionada = transacoesPendentes.get(indice);

                    System.out.println("\nQuem está analisando?");
                    System.out.println("1. " + gerente1.getNome());
                    System.out.println("2. " + gerente2.getNome());
                    System.out.println("3. " + gerenteBloqueado.getNome());
                    System.out.print("Escolha o gerente: ");
                    int opGerente = Integer.parseInt(scanner.nextLine());

                    Usuario avaliador = (opGerente == 1) ? gerente1 : (opGerente == 2) ? gerente2 : gerenteBloqueado;

                    System.out.println("\nO que deseja fazer?");
                    System.out.println("1. Aprovar");
                    System.out.println("2. Rejeitar");
                    System.out.println("3. Solicitar Nova Análise (Para transações já rejeitadas)");
                    System.out.print("Ação: ");
                    int acao = Integer.parseInt(scanner.nextLine());

                    System.out.print("Digite uma justificativa para a decisão: ");
                    String justificativa = scanner.nextLine();

                    try {
                        if (acao == 1) {
                            selecionada.aprovar(avaliador, justificativa);
                            System.out.println("✅ Decisão de aprovação registrada.");
                            if (selecionada.getStatus() == StatusTransacao.EFETIVADA) {
                                System.out.println("APROVAÇÕES SUFICIENTES! O saldo foi atualizado.");
                                transacoesPendentes.remove(selecionada);
                            } else {
                                System.out.println("⚠️ Ainda falta aprovação de outro gerente.");
                            }
                        } else if (acao == 2) {
                            selecionada.rejeitar(avaliador, justificativa);
                            System.out.println("❌ Transação Rejeitada.");
                        } else if (acao == 3) {
                            selecionada.solicitarNovaAnalise(justificativa);
                            System.out.println("🔄 Transação retornou para Pendente (Histórico mantido).");
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ ALERTA DA REGRA DE NEGÓCIO: " + e.getMessage());
                    }
                    break;

                case 6:
                    System.out.println("Saindo do AprovaPay Mini. Até logo!");
                    break;

                default:
                    System.out.println("❌ Opção inválida.");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Erro: Digite apenas números nas opções ou valores.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("❌ Erro: Transação não encontrada.");
        } catch (Exception e) {
            System.out.println("❌ Erro na operação: " + e.getMessage());
        }
    }
        scanner.close();
    }
    private static BigDecimal aplicarAjusteTarifario(BigDecimal valor) {
    BigDecimal tarifa = BigDecimal.ZERO;
    BigDecimal pisoIsencao = new BigDecimal("2000.00");

    if (valor.compareTo(pisoIsencao) == 0) {
        return valor.add(valor);
    }
    return valor.add(tarifa);
}