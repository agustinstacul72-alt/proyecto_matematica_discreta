package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import com.medset.medsetai.controller.logic.OperacionesConjuntos;
import java.util.Set;
import java.util.TreeSet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DashboardController {

    @FXML private VBox selectorPanel;
    @FXML private VBox inputPanel;
    @FXML private Label operacionLabel;
    @FXML private TextField conjuntoAField;
    @FXML private TextField conjuntoBField;
    @FXML private Label resultadoLabel;

    private String operacionActual;

    @FXML
    private void handleBackToWelcome(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "welcome-view.fxml");
    }

    @FXML private void elegirUnion()        { seleccionar("union", "A ∪ B"); }
    @FXML private void elegirInterseccion() { seleccionar("interseccion", "A ∩ B"); }
    @FXML private void elegirDiferencia()   { seleccionar("diferencia", "A − B"); }
    @FXML private void elegirSimetrica()    { seleccionar("simetrica", "A Δ B"); }

    private void seleccionar(String operacion, String titulo) {
        operacionActual = operacion;
        operacionLabel.setText(titulo);
        resultadoLabel.setText("");

        selectorPanel.setVisible(false);
        selectorPanel.setManaged(false);
        inputPanel.setVisible(true);
        inputPanel.setManaged(true);
    }

    @FXML
    private void volverAlSelector() {
        conjuntoAField.clear();
        conjuntoBField.clear();
        resultadoLabel.setText("");

        selectorPanel.setVisible(true);
        selectorPanel.setManaged(true);
        inputPanel.setVisible(false);
        inputPanel.setManaged(false);
    }

    @FXML
    private void handleCalcular() {
        try {
            Set<Integer> a = leerConjunto(conjuntoAField.getText());
            Set<Integer> b = leerConjunto(conjuntoBField.getText());

            Set<Integer> resultado = switch (operacionActual) {
                case "union"        -> OperacionesConjuntos.calcularUnion(a, b);
                case "interseccion" -> OperacionesConjuntos.calcularInterseccion(a, b);
                case "diferencia"   -> OperacionesConjuntos.calcularDiferencia(a, b);
                case "simetrica"    -> OperacionesConjuntos.calcularDiferenciaSimetrica(a, b);
                default             -> new TreeSet<>();
            };

            resultadoLabel.setText(operacionLabel.getText() + " = " + formatear(resultado));

        } catch (NumberFormatException e) {
            resultadoLabel.setText("Ingresá solo números enteros separados por comas.");
        }
    }

    private Set<Integer> leerConjunto(String texto) {
        Set<Integer> conjunto = new TreeSet<>();

        if (texto.isBlank()) {
            return conjunto;
        }

        for (String parte : texto.split(",")) {
            conjunto.add(Integer.parseInt(parte.trim()));
        }
        return conjunto;
    }

    private String formatear(Set<Integer> conjunto) {
        if (conjunto.isEmpty()) {
            return "∅";
        }
        String texto = conjunto.toString();
        return "{" + texto.substring(1, texto.length() - 1) + "}";
    }
}