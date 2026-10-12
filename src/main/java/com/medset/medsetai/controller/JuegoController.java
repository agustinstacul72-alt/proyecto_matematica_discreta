package com.medset.medsetai.controller;

import com.medset.medsetai.logica.FormatoConjunto;
import com.medset.medsetai.modelo.CatalogoClinico;
import com.medset.medsetai.modelo.Enfermedad;
import com.medset.medsetai.modelo.JuegoDetective;
import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Módulo 3: juego "Detective de síntomas". La lógica vive en {@link JuegoDetective};
 * este controlador solo la muestra.
 */
public class JuegoController {

    @FXML private Label puntosLabel;
    @FXML private Label totalLabel;
    @FXML private Label candidatosTitulo;
    @FXML private VBox candidatosBox;
    @FXML private FlowPane preguntasPane;
    @FXML private Button pistaButton;
    @FXML private Button rendirseButton;
    @FXML private Label mensajeLabel;
    @FXML private VBox registroBox;

    private final JuegoDetective juego = new JuegoDetective();
    private int puntajeTotal = 0;
    private int rondas = 0;

    @FXML
    private void initialize() {
        nuevaRonda();
    }

    @FXML
    private void volverAlInicio(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "welcome-view.fxml");
    }

    @FXML
    private void nuevaRonda() {
        juego.nuevaPartida();
        registroBox.getChildren().clear();
        registrar("Inicio: C = D (las " + juego.candidatos().size() + " enfermedades del catálogo).");
        mensajeLabel.setText("Elegí un síntoma para preguntar.");
        mensajeLabel.getStyleClass().setAll("game-message");

        preguntasPane.getChildren().clear();
        for (String s : CatalogoClinico.UNIVERSO) {
            Button b = new Button(s);
            b.getStyleClass().add("chip");
            b.setOnAction(e -> preguntar(s, b));
            preguntasPane.getChildren().add(b);
        }
        refrescar();
    }

    private void preguntar(String sintoma, Button boton) {
        if (juego.terminado()) {
            return;
        }
        int antes = juego.candidatos().size();
        boolean si = juego.preguntar(sintoma);
        boton.setDisable(true);
        boton.getStyleClass().add(si ? "chip-si" : "chip-no");
        boton.setText(sintoma + (si ? "  ✓" : "  ✗"));

        String rel = si ? "∈" : "∉";
        registrar("¿" + sintoma + " ∈ X?  " + (si ? "Sí" : "No") + ".  C ← { E ∈ C | " + sintoma + " " + rel
                + " E }  →  |C| pasó de " + antes + " a " + juego.candidatos().size()
                + "  = " + nombres(juego.candidatos()));
        mensajeLabel.setText(si ? "Sí, la enfermedad secreta tiene «" + sintoma + "»."
                : "No, la enfermedad secreta no tiene «" + sintoma + "».");
        if (juego.candidatos().size() == 1) {
            mensajeLabel.setText(mensajeLabel.getText() + " Solo queda un candidato: ¡adiviná!");
        }
        refrescar();
    }

    private void adivinar(Enfermedad e) {
        if (juego.terminado()) {
            return;
        }
        if (juego.adivinar(e)) {
            puntajeTotal += juego.puntos();
            rondas++;
            registrar("Adivinaste X = " + e.nombre() + ".  X = " + FormatoConjunto.formatear(e.sintomas()));
            mensajeLabel.setText("¡Correcto! Era " + e.nombre() + ". Ganaste " + juego.puntos() + " puntos.");
            mensajeLabel.getStyleClass().setAll("game-message", "game-win");
        } else {
            registrar("Intentaste " + e.nombre() + ": incorrecto (−" + JuegoDetective.COSTO_ERROR
                    + "). Se quita de C.");
            if (juego.terminado()) {
                terminarPerdiendo("Te quedaste sin puntos.");
            } else {
                mensajeLabel.setText("No es " + e.nombre() + ". Seguí investigando.");
            }
        }
        refrescar();
    }

    @FXML
    private void pista() {
        if (juego.terminado()) {
            return;
        }
        String mejor = juego.mejorPregunta();
        if (mejor == null) {
            mensajeLabel.setText("Pista: ya no hace falta preguntar; solo queda "
                    + nombres(juego.candidatos()) + ".");
            return;
        }
        int con = 0;
        for (Enfermedad e : juego.candidatos()) {
            if (e.sintomas().contains(mejor)) {
                con++;
            }
        }
        mensajeLabel.setText("Pista: preguntá por «" + mejor + "». Divide a C en " + con + " candidatos que lo tienen y "
                + (juego.candidatos().size() - con) + " que no, así que en el peor caso te quedan "
                + Math.max(con, juego.candidatos().size() - con) + ".");
    }

    @FXML
    private void rendirse() {
        if (juego.terminado()) {
            return;
        }
        juego.rendirse();
        rondas++;
        terminarPerdiendo("Te rendiste.");
        refrescar();
    }

    private void terminarPerdiendo(String motivo) {
        Enfermedad x = juego.secreta();
        mensajeLabel.setText(motivo + " La enfermedad secreta era " + x.nombre() + ".");
        mensajeLabel.getStyleClass().setAll("game-message", "game-lose");
        registrar("Respuesta: X = " + x.nombre() + " = " + FormatoConjunto.formatear(x.sintomas()));
    }

    private void refrescar() {
        puntosLabel.setText("Puntos de la ronda: " + juego.puntos());
        totalLabel.setText("Total: " + puntajeTotal + " pts en " + rondas + " rondas");
        candidatosTitulo.setText("Candidatos C   (|C| = " + juego.candidatos().size() + ")");
        pistaButton.setDisable(juego.terminado());
        rendirseButton.setDisable(juego.terminado());
        if (juego.terminado()) {
            for (Node n : preguntasPane.getChildren()) {
                n.setDisable(true);
            }
        }

        candidatosBox.getChildren().clear();
        List<Enfermedad> vivos = juego.candidatos();
        for (Enfermedad e : juego.catalogo()) {
            boolean vivo = vivos.contains(e);
            Label nombre = new Label(e.nombre());
            nombre.getStyleClass().add("card-title");
            nombre.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nombre, Priority.ALWAYS);
            HBox fila = new HBox(10, nombre);
            fila.setStyle("-fx-alignment: CENTER_LEFT;");
            if (vivo && !juego.terminado()) {
                Button b = new Button("¡Es esta!");
                b.getStyleClass().add("small-button");
                b.setOnAction(ev -> adivinar(e));
                fila.getChildren().add(b);
            }
            VBox card = new VBox(4, fila);
            card.getStyleClass().add("rank-card");
            if (juego.terminado() && e.equals(juego.secreta())) {
                card.getStyleClass().add("rank-card-selected");
                Label sintomas = new Label(FormatoConjunto.formatear(e.sintomas()));
                sintomas.setWrapText(true);
                sintomas.getStyleClass().add("card-detail");
                card.getChildren().add(sintomas);
            } else if (!vivo) {
                card.getStyleClass().add("rank-card-out");
            }
            candidatosBox.getChildren().add(card);
        }
    }

    private void registrar(String linea) {
        Label l = new Label((registroBox.getChildren().size() + 1) + ". " + linea);
        l.setWrapText(true);
        l.getStyleClass().add("log-line");
        registroBox.getChildren().add(l);
    }

    private String nombres(List<Enfermedad> lista) {
        List<String> n = new ArrayList<>();
        for (Enfermedad e : lista) {
            n.add(e.nombre());
        }
        return n.isEmpty() ? "∅" : "{" + String.join(", ", n) + "}";
    }
}
