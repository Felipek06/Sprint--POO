public class TrechoRodovia {
    private static final double nivelCriticocm = 50;

    private int quilometroInicial;
    private int quilometroFinal;
    private double nivelVegetacaoCm;
    private EquipeManutencao equipeResponsavel;

    public TrechoRodovia(int quilometroInicial, int quilometroFinal, double nivelVegetacaoCm){
        validarQuilometroInicial(quilometroInicial);
        validarQuilometroFinal(quilometroFinal, quilometroInicial);
        validarNivelVegetacao(nivelVegetacaoCm);

        this.quilometroInicial = quilometroInicial;
        this.quilometroFinal = quilometroFinal;
        this.nivelVegetacaoCm = nivelVegetacaoCm;
    }

    public void registrarCrescimento(double taxaCm) {
        if (taxaCm <= 0) {
            throw new IllegalArgumentException(
                    "A taxa de crescimento deve ser positiva. Valor recebido: " + taxaCm + " cm"
            );
        }
        nivelVegetacaoCm += taxaCm;
    }

    public boolean isCritico(){
        return nivelVegetacaoCm >= nivelCriticocm;
    }

    public void associarEquipe(EquipeManutencao equipe) {
        if (equipe == null) {
            throw new IllegalArgumentException("A equipe de manutenção não pode ser nula.");
        }
        this.equipeResponsavel = equipe;
    }

    private void validarQuilometroInicial(int km) {
        //Rodovias começam podem começar no kilometro 0
        if (km < 0) {
            throw new IllegalArgumentException(
                    "O quilômetro inicial não pode ser negativo. Valor recebido: " + km
            );
        }
    }

    private void validarQuilometroFinal(int kmFinal, int kmInicial) {
        if (kmFinal <= 0) {
            throw new IllegalArgumentException(
                    "O quilômetro final deve ser maior que zero. Valor recebido: " + kmFinal
            );
        }
        if (kmFinal <= kmInicial) {
            throw new IllegalArgumentException(
                    "O quilômetro final (" + kmFinal + ") deve ser maior que o inicial (" + kmInicial + ")."
            );
        }
    }

    private void validarNivelVegetacao(double nivel) {
        if (nivel < 0) {
            throw new IllegalArgumentException(
                    "O nível de vegetação não pode ser negativo. Valor recebido: " + nivel + " cm"
            );
        }
    }

    public int getQuilometroInicial() {
        return quilometroInicial;
    }

    public int getQuilometroFinal() {
        return quilometroFinal;
    }

    public double getNivelVegetacaoCm() {
        return nivelVegetacaoCm;
    }

    public EquipeManutencao getEquipeResponsavel() {
        return equipeResponsavel;
    }

    @Override
    public String toString() {
        String statusCritico = isCritico() ? " ESTADO CRÍTICO " : "";
        String equipeInfo = (equipeResponsavel != null)
                ? equipeResponsavel.getNome()
                : "Nenhuma equipe designada";

        return String.format(
                "[TrechoRodovia | KM %d → KM %d | Vegetação: %.1f cm%s | Equipe: %s]",
                quilometroInicial, quilometroFinal, nivelVegetacaoCm, statusCritico, equipeInfo
        );
    }
}