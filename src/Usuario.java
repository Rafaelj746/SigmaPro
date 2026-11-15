import java.time.LocalDate;

public class Usuario {
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String senha;
    private boolean bloqueado;

    public Usuario(String nome, String cpf, LocalDate dataNascimento, String senha) {
        setNome(nome);
        setCpf(cpf);
        setDataNascimento(dataNascimento);
        setSenha(senha);
        this.bloqueado = false;
    }
    public Usuario(){
        this.bloqueado = false;
    }
    public boolean isBloqueado(){return bloqueado;}
    public void setBloqueado(boolean bloqueado)
    {this.bloqueado = bloqueado;}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome não pode ser vazio ou conter apenas espaços."
            );
        }
        this.nome = nome.trim();
        if (!nome.matches("^[a-zA-ZÀ-ÿ ]+$")) {
            throw new IllegalArgumentException(
                    "Nome inválido. Utilize apenas letras"
            );
        }
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException(
                    "CPF não pode ser vazio ou conter apenas espaços."
            );
        }
        String cpfFormatado = cpf.trim();
        String regexFormatacao = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$";
        if(!cpfFormatado.matches(regexFormatacao)) {
            throw new IllegalArgumentException(
                    "CPF inválido. Digite somente números. e no formato esperado: 000.000.000-00"
            );
        }
        this.cpf = cpfFormatado;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new IllegalArgumentException(
                    "Data de nascimento é obrigatória."
            );
        } if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Data de nascimento não pode ser futura."
            );
        }
        this.dataNascimento = dataNascimento;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException(
                    "Senha não pode ser vazia ou conter apenas espaços."
            );
        }
        if (senha.length() < 6) {
            throw new IllegalArgumentException(
                    "Senha deve ter pelo menos 6 caracteres."
            );
        }
        this.senha = senha;
    }
}