package com.medset.controller;

import com.medset.logica.CalculadoraConjuntos;
import com.medset.logica.Operacion;
import com.medset.logica.ResultadoOperacion;
import com.medset.ui.VennDiagrama;
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

public class DashboardController {

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
        operacionLabel.setText(operacion.simbolo());
        definicionLabel.setText(operacion.definicion());
        limpiarResultado();

        mostrar(campoB, !operacion.usaUniverso());
        mostrar(campoU, operacion.usaUniverso());

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
        conjuntoAField.setText(operacionActual.ejemploA());
        campoSegundo().setText(operacionActual.ejemploSegundo());
        handleCalcular();
    }

    @FXML
    private void handleCalcular() {
        ResultadoOperacion r = CalculadoraConjuntos.calcular(
                operacionActual, conjuntoAField.getText(), campoSegundo().getText());
        resultadoLabel.setText(r.resultado());
        detalleLabel.setText(r.detalle());
        ResultadoOperacion.Diagrama d = r.diagrama();
        if (d == null) {
            return;
        }
        if (d.esComplemento()) {
            venn.dibujarComplemento(d.a(), d.universo(), "A");
        } else {
            venn.dibujar(d.a(), d.b(), "A", "B", d.resaltar(), d.alerta());
        }
    }

    /** B, o U cuando la operación es el complemento. */
    private TextField campoSegundo() {
        return operacionActual.usaUniverso() ? universoField : conjuntoBField;
    }

    private void limpiarResultado() {
        resultadoLabel.setText("");
        detalleLabel.setText("");
        venn.limpiar();
    }

    private static void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }
}
