package com.medset.controller;

import com.medset.logica.FormatoConjunto;
import com.medset.logica.OperacionesConjuntos;
import com.medset.ui.VennDiagrama;
import com.medset.ui.VennDiagrama.Zona;
import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class DashboardController {

    private enum Operacion {
        UNION("A ∪ B", "A ∪ B = { x | x ∈ A ∨ x ∈ B }  — todo lo que está en A, en B o en ambos."),
        INTERSECCION("A ∩ B", "A ∩ B = { x | x ∈ A ∧ x ∈ B }  — solo lo que está en los dos."),
        DIFERENCIA("A − B", "A − B = { x | x ∈ A ∧ x ∉ B }  — lo que está en A pero no en B."),
        SIMETRICA("A Δ B", "A Δ B = (A − B) ∪ (B − A)  — lo que está en uno solo de los dos."),
        SUBCONJUNTO("A ⊆ B", "A ⊆ B ⇔ ∀x (x ∈ A → x ∈ B)  — todo elemento de A también está en B."),
        COMPLEMENTO("A'", "A' = U − A = { x ∈ U | x ∉ A }  — lo que le falta a A para ser el universo.");

        final String simbolo;
        final String definicion;

        Operacion(String simbolo, String definicion) {
            this.simbolo = simbolo;
            this.definicion = definicion;
        }
    }

    @FXML private VBox selectorPanel;
    @FXML private HBox inputPanel;
    @FXML private VBox campoB;
    @FXML private VBox campoU;
    @FXML private Label operacionLabel;
    @FXML private Label definicionLabel;
    @FXML private TextField conjuntoAField;
    @FXML private TextField conjuntoBField;
    @FXML private TextField universoField;
    @FXML private Label resultadoLabel;
    @FXML private Label detalleLabel;
    @FXML private VennDiagrama venn;

    private Operacion operacionActual = Operacion.UNION;

    @FXML
    private void handleBackToWelcome(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.INICIO);
    }

    @FXML private void elegirUnion()        { seleccionar(Operacion.UNION); }
    @FXML private void elegirInterseccion() { seleccionar(Operacion.INTERSECCION); }
    @FXML private void elegirDiferencia()   { seleccionar(Operacion.DIFERENCIA); }
    @FXML private void elegirSimetrica()    { seleccionar(Operacion.SIMETRICA); }
    @FXML private void elegirSubconjunto()  { seleccionar(Operacion.SUBCONJUNTO); }
    @FXML private void elegirComplemento()  { seleccionar(Operacion.COMPLEMENTO); }

    private void seleccionar(Operacion operacion) {
        operacionActual = operacion;
        operacionLabel.setText(operacion.simbolo);
        definicionLabel.setText(operacion.definicion);
        limpiarResultado();

        boolean esComplemento = operacion == Operacion.COMPLEMENTO;
        mostrar(campoB, !esComplemento);
        mostrar(campoU, esComplemento);

        mostrar(selectorPanel, false);
        mostrar(inputPanel, true);
        cargarEjemplo();
    }

    @FXML
    private void volverAlSelector() {
        conjuntoAField.clear();
        conjuntoBField.clear();
        universoField.clear();
        limpiarResultado();
        mostrar(selectorPanel, true);
        mostrar(inputPanel, false);
    }

    /** Llena los campos con un ejemplo de síntomas para la demostración. */
    @FXML
    private void cargarEjemplo() {
        switch (operacionActual) {
            case COMPLEMENTO -> {
                universoField.setText("fiebre, tos, cansancio, dolor de cabeza, estornudos, congestión nasal, náuseas");
                conjuntoAField.setText("fiebre, tos, cansancio");
            }
            case SUBCONJUNTO -> {
                conjuntoAField.setText("fiebre, tos");
                conjuntoBField.setText("fiebre, tos, cansancio, dolor muscular");
            }
            default -> {
                conjuntoAField.setText("fiebre, tos, cansancio, dolor de cabeza");
                conjuntoBField.setText("fiebre, tos, dificultad para respirar");
            }
        }
        handleCalcular();
    }

    @FXML
    private void handleCalcular() {
        Set<String> a = FormatoConjunto.leer(conjuntoAField.getText());
        Set<String> b = FormatoConjunto.leer(conjuntoBField.getText());
        switch (operacionActual) {
            case UNION -> calcularUnion(a, b);
            case INTERSECCION -> calcularInterseccion(a, b);
            case DIFERENCIA -> calcularDiferencia(a, b);
            case SIMETRICA -> calcularSimetrica(a, b);
            case SUBCONJUNTO -> calcularSubconjunto(a, b);
            case COMPLEMENTO -> calcularComplemento(a, FormatoConjunto.leer(universoField.getText()));
        }
    }

    private void calcularUnion(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularUnion(a, b);
        int interseccion = OperacionesConjuntos.cardinalidad(OperacionesConjuntos.calcularInterseccion(a, b));
        mostrarResultado(r, cardinales(a, b) + "\nPrincipio de inclusión-exclusión:\n|A ∪ B| = |A| + |B| − |A ∩ B| = "
                + a.size() + " + " + b.size() + " − " + interseccion + " = " + r.size());
        dibujar(a, b, EnumSet.of(Zona.SOLO_A, Zona.AMBOS, Zona.SOLO_B), false);
    }

    private void calcularInterseccion(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularInterseccion(a, b);
        String disjuntos = r.isEmpty() ? "\nA ∩ B = ∅, así que A y B son disjuntos." : "";
        mostrarResultado(r, cardinales(a, b) + ",  |A ∩ B| = " + r.size() + disjuntos);
        dibujar(a, b, EnumSet.of(Zona.AMBOS), false);
    }

    private void calcularDiferencia(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularDiferencia(a, b);
        Set<String> inversa = OperacionesConjuntos.calcularDiferencia(b, a);
        mostrarResultado(r, cardinales(a, b) + ",  |A − B| = " + r.size()
                + "\nOjo: la diferencia no es conmutativa. B − A = " + FormatoConjunto.formatear(inversa));
        dibujar(a, b, EnumSet.of(Zona.SOLO_A), false);
    }

    private void calcularSimetrica(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularDiferenciaSimetrica(a, b);
        mostrarResultado(r, cardinales(a, b) + ",  |A Δ B| = " + r.size()
                + "\nA − B = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, b))
                + "   B − A = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(b, a)));
        dibujar(a, b, EnumSet.of(Zona.SOLO_A, Zona.SOLO_B), false);
    }

    private void calcularSubconjunto(Set<String> a, Set<String> b) {
        if (OperacionesConjuntos.esSubconjunto(a, b)) {
            String propio = OperacionesConjuntos.sonIguales(a, b)
                    ? "Además A = B." : "Además A ⊂ B (subconjunto propio): B tiene elementos que A no tiene.";
            mostrar("A ⊆ B es VERDADERO", cardinales(a, b) + "\nTodo elemento de A pertenece a B. " + propio);
            dibujar(a, b, EnumSet.of(Zona.AMBOS), false);
        } else {
            Set<String> contraejemplos = OperacionesConjuntos.calcularDiferencia(a, b);
            mostrar("A ⊆ B es FALSO", cardinales(a, b) + "\nContraejemplos (están en A pero no en B): A − B = "
                    + FormatoConjunto.formatear(contraejemplos));
            dibujar(a, b, EnumSet.of(Zona.SOLO_A), true);
        }
    }

    private void calcularComplemento(Set<String> a, Set<String> u) {
        if (u.isEmpty()) {
            mostrar("Definí primero el universo U.", "");
            return;
        }
        if (!OperacionesConjuntos.esSubconjunto(a, u)) {
            mostrar("A debe ser subconjunto de U.", "Estos elementos de A no están en U: "
                    + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, u)));
            return;
        }
        Set<String> r = OperacionesConjuntos.calcularComplemento(u, a);
        mostrar("A' = " + FormatoConjunto.formatear(r),
                "|U| = " + u.size() + ",  |A| = " + a.size() + ",  |A'| = |U| − |A| = " + r.size()
                        + "\nSe cumple A ∪ A' = U  y  A ∩ A' = ∅.");
        venn.dibujarComplemento(a, u, "A");
    }

    private static String cardinales(Set<String> a, Set<String> b) {
        return "|A| = " + OperacionesConjuntos.cardinalidad(a) + ",  |B| = " + OperacionesConjuntos.cardinalidad(b);
    }

    private void dibujar(Set<String> a, Set<String> b, Set<Zona> resaltar, boolean alerta) {
        venn.dibujar(a, b, "A", "B", resaltar, alerta);
    }

    private void mostrarResultado(Set<String> resultado, String detalle) {
        mostrar(operacionActual.simbolo + " = " + FormatoConjunto.formatear(resultado), detalle);
    }

    private void mostrar(String resultado, String detalle) {
        resultadoLabel.setText(resultado);
        detalleLabel.setText(detalle);
    }

    private void limpiarResultado() {
        mostrar("", "");
        venn.limpiar();
    }

    private static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }
}
