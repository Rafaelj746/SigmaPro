import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

void main(){
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("=========================================");
        System.out.println("      BEM-VINDO AO APROVAPAY MINI        ");
        System.out.println("=========================================");
        System.out.println("Vamos abrir a sua conta cliente.");

        Usuario cliente = null;

        while (cliente == null) {
            try {
                System.out.print("\nDigite seu Nome: ");
                String nome = scanner.nextLine();

                System.out.print("Digite seu CPF (000.000.000-00): ");
                String cpf = scanner.nextLine();

                System.out.print("Data de Nascimento (DD/MM/AAAA): ");
                String dataStr = scanner.nextLine();
                LocalDate dataNascimento = LocalDate.parse(dataStr, formatter);

                System.out.print("Crie uma Senha (mín. 6 caracteres): ");
                String senha = scanner.nextLine();

                cliente = new Usuario(nome, cpf, dataNascimento, senha);
                System.out.println("✅ Usuário criado com sucesso!");
            } catch (Exception e) {
                System.out.println("❌ Erro: " + e.getMessage());
                System.out.println("Por favor, tente novamente.");
            }
        }

        ContaBancaria conta = null;
        while (conta == null) {
            try {
                System.out.print("\nQual será o saldo inicial da sua conta? R$ ");
                String saldoStr = scanner.nextLine().replace(",", ".");
                conta = new ContaBancaria(cliente, new BigDecimal(saldoStr));
                System.out.println("✅ Conta bancária criada!");
            } catch (Exception e) {
                System.out.println("❌ Erro: Digite um valor válido.");
            }
        }

        // 3. CRIANDO GERENTES PARA TESTAR AS APROVAÇÕES
        Usuario gerente1 = new Usuario("Gerente Ana", "111.222.333-44", LocalDate.of(1985, 5, 10), "senha123");
        Usuario gerente2 = new Usuario("Gerente Carlos", "555.666.777-88", LocalDate.of(1980, 8, 20), "senha123");
        Usuario gerenteBloqueado = new Usuario("Gerente Pedro (BLOQUEADO)", "999.888.777-66", LocalDate.of(1992, 2, 2), "senha123");
        gerenteBloqueado.setBloqueado(true);

        List<Transacao> transacoesPendentes = new ArrayList<>();


        int opcao = 0;
        while (opcao != 5) {
            System.out.println("\n=========================================");
            System.out.println("Olá, " + cliente.getNome() + " | Saldo: R$ " + conta.getSaldo());
            System.out.println("1. Depositar (Aprovação Automática)");
            System.out.println("2. Solicitar Saque (Vai para Análise)");
            System.out.println("3. Ver Extrato da Conta");
            System.out.println("4. Painel de Gerência (Aprovar/Rejeitar Transações)");
            System.out.println("5. Sair");
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
                        transacoesPendentes.add(t);
                        System.out.println("⏳ Saque solicitado! Foi enviado para o painel de aprovação.");
                        break;

                    case 3:
                        conta.imprimirExtrato();
                        break;

                    case 4:
                        // PAINEL DE APROVAÇÃO
                        if (transacoesPendentes.isEmpty()) {
                            System.out.println("✅ Nenhuma transação pendente no momento.");
                            break;
                        }

                        System.out.println("\n--- TRANSAÇÕES AGUARDANDO ANÁLISE ---");
                        for (int i = 0; i < transacoesPendentes.size(); i++) {
                            Transacao pendente = transacoesPendentes.get(i);
                            System.out.println(i + " -> " + pendente.getTipo() + " de R$ " + pendente.getValor() + " | Status: " + pendente.getStatus());
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
                                    System.out.println("🎉 APROVAÇÕES SUFICIENTES! O saldo foi atualizado.");
                                    transacoesPendentes.remove(selecionada); // Tira da fila
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

                    case 5:
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