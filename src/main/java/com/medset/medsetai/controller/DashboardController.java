package com.medset.medsetai.controller;

import com.medset.medsetai.logica.FormatoConjunto;
import com.medset.medsetai.logica.OperacionesConjuntos;
import com.medset.medsetai.ui.VennDiagrama;
import com.medset.medsetai.ui.VennDiagrama.Region;
import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Módulo 1: calculadora de conjuntos con definición formal, cardinalidad y diagrama de Venn.
 */
public class DashboardController {

    /** Operaciones disponibles, con su símbolo y su definición formal. */
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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "welcome-view.fxml");
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
        if (operacionActual == Operacion.COMPLEMENTO) {
            universoField.setText("fiebre, tos, cansancio, dolor de cabeza, estornudos, congestión nasal, náuseas");
            conjuntoAField.setText("fiebre, tos, cansancio");
        } else if (operacionActual == Operacion.SUBCONJUNTO) {
            conjuntoAField.setText("fiebre, tos");
            conjuntoBField.setText("fiebre, tos, cansancio, dolor muscular");
        } else {
            conjuntoAField.setText("fiebre, tos, cansancio, dolor de cabeza");
            conjuntoBField.setText("fiebre, tos, dificultad para respirar");
        }
        handleCalcular();
    }

    @FXML
    private void handleCalcular() {
        Set<String> a = FormatoConjunto.leer(conjuntoAField.getText());
        Set<String> b = FormatoConjunto.leer(conjuntoBField.getText());
        int cardA = OperacionesConjuntos.cardinalidad(a);
        int cardB = OperacionesConjuntos.cardinalidad(b);
        String cards = "|A| = " + cardA + ",  |B| = " + cardB;

        switch (operacionActual) {
            case UNION -> {
                Set<String> r = OperacionesConjuntos.calcularUnion(a, b);
                int inter = OperacionesConjuntos.cardinalidad(OperacionesConjuntos.calcularInterseccion(a, b));
                mostrarResultado(r, cards + "\nPrincipio de inclusión-exclusión:\n|A ∪ B| = |A| + |B| − |A ∩ B| = "
                        + cardA + " + " + cardB + " − " + inter + " = " + r.size());
                venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.SOLO_A, Region.AMBOS, Region.SOLO_B), false);
            }
            case INTERSECCION -> {
                Set<String> r = OperacionesConjuntos.calcularInterseccion(a, b);
                String extra = r.isEmpty() ? "\nA ∩ B = ∅, así que A y B son disjuntos." : "";
                mostrarResultado(r, cards + ",  |A ∩ B| = " + r.size() + extra);
                venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.AMBOS), false);
            }
            case DIFERENCIA -> {
                Set<String> r = OperacionesConjuntos.calcularDiferencia(a, b);
                Set<String> inversa = OperacionesConjuntos.calcularDiferencia(b, a);
                mostrarResultado(r, cards + ",  |A − B| = " + r.size()
                        + "\nOjo: la diferencia no es conmutativa. B − A = " + FormatoConjunto.formatear(inversa));
                venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.SOLO_A), false);
            }
            case SIMETRICA -> {
                Set<String> r = OperacionesConjuntos.calcularDiferenciaSimetrica(a, b);
                mostrarResultado(r, cards + ",  |A Δ B| = " + r.size()
                        + "\nA − B = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, b))
                        + "   B − A = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(b, a)));
                venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.SOLO_A, Region.SOLO_B), false);
            }
            case SUBCONJUNTO -> {
                boolean sub = OperacionesConjuntos.esSubconjunto(a, b);
                Set<String> contraejemplos = OperacionesConjuntos.calcularDiferencia(a, b);
                if (sub) {
                    resultadoLabel.setText("A ⊆ B es VERDADERO");
                    String propio = OperacionesConjuntos.sonIguales(a, b)
                            ? "Además A = B." : "Además A ⊂ B (subconjunto propio): B tiene elementos que A no tiene.";
                    detalleLabel.setText(cards + "\nTodo elemento de A pertenece a B. " + propio);
                    venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.AMBOS), false);
                } else {
                    resultadoLabel.setText("A ⊆ B es FALSO");
                    detalleLabel.setText(cards + "\nContraejemplos (están en A pero no en B): A − B = "
                            + FormatoConjunto.formatear(contraejemplos));
                    venn.dibujar(a, b, null, "A", "B", EnumSet.of(Region.SOLO_A), true);
                }
            }
            case COMPLEMENTO -> {
                Set<String> u = FormatoConjunto.leer(universoField.getText());
                if (u.isEmpty()) {
                    resultadoLabel.setText("Definí primero el universo U.");
                    detalleLabel.setText("");
                    return;
                }
                if (!OperacionesConjuntos.esSubconjunto(a, u)) {
                    resultadoLabel.setText("A debe ser subconjunto de U.");
                    detalleLabel.setText("Estos elementos de A no están en U: "
                            + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, u)));
                    return;
                }
                Set<String> r = OperacionesConjuntos.calcularComplemento(u, a);
                resultadoLabel.setText("A' = " + FormatoConjunto.formatear(r));
                detalleLabel.setText("|U| = " + u.size() + ",  |A| = " + cardA + ",  |A'| = |U| − |A| = " + r.size()
                        + "\nSe cumple A ∪ A' = U  y  A ∩ A' = ∅.");
                venn.dibujar(a, null, u, "A", null, EnumSet.of(Region.FUERA), false);
            }
        }
    }

    private void mostrarResultado(Set<String> resultado, String detalle) {
        resultadoLabel.setText(operacionActual.simbolo + " = " + FormatoConjunto.formatear(resultado));
        detalleLabel.setText(detalle);
    }

    private void limpiarResultado() {
        resultadoLabel.setText("");
        detalleLabel.setText("");
        venn.getChildren().clear();
    }

    private static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }
}
