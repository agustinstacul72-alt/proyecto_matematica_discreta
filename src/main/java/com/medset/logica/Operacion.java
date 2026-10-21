package com.medset.logica;

/** Operaciones de la calculadora, con su definición formal y un ejemplo de síntomas. */
public enum Operacion {
    UNION("A ∪ B", "A ∪ B = { x | x ∈ A ∨ x ∈ B }  — todo lo que está en A, en B o en ambos."),
    INTERSECCION("A ∩ B", "A ∩ B = { x | x ∈ A ∧ x ∈ B }  — solo lo que está en los dos."),
    DIFERENCIA("A − B", "A − B = { x | x ∈ A ∧ x ∉ B }  — lo que está en A pero no en B."),
    SIMETRICA("A Δ B", "A Δ B = (A − B) ∪ (B − A)  — lo que está en uno solo de los dos."),
    SUBCONJUNTO("A ⊆ B", "A ⊆ B ⇔ ∀x (x ∈ A → x ∈ B)  — todo elemento de A también está en B."),
    COMPLEMENTO("A'", "A' = U − A = { x ∈ U | x ∉ A }  — lo que le falta a A para ser el universo.");

    private final String simbolo;
    private final String definicion;

    Operacion(String simbolo, String definicion) {
        this.simbolo = simbolo;
        this.definicion = definicion;
    }

    public String simbolo() {
        return simbolo;
    }

    public String definicion() {
        return definicion;
    }

    /** La operación usa el universo U en lugar del conjunto B. */
    public boolean usaUniverso() {
        return this == COMPLEMENTO;
    }

    public String ejemploA() {
        return switch (this) {
            case COMPLEMENTO -> "fiebre, tos, cansancio";
            case SUBCONJUNTO -> "fiebre, tos";
            default -> "fiebre, tos, cansancio, dolor de cabeza";
        };
    }

    /** Ejemplo para B, o para U en el complemento. */
    public String ejemploSegundo() {
        return switch (this) {
            case COMPLEMENTO -> "fiebre, tos, cansancio, dolor de cabeza, estornudos, congestión nasal, náuseas";
            case SUBCONJUNTO -> "fiebre, tos, cansancio, dolor muscular";
            default -> "fiebre, tos, dificultad para respirar";
        };
    }
}
