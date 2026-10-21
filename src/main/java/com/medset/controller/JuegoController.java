package com.medset.controller;

import com.medset.logica.FormatoConjunto;
import com.medset.modelo.CatalogoClinico;
import com.medset.modelo.Enfermedad;
import com.medset.modelo.JuegoDetective;
import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
        SceneManager.irA(event, Vista.INICIO);
    }

    @FXML
    private void nuevaRonda() {
        juego.nuevaPartida();
        registroBox.getChildren().clear();
        registrar("Inicio: C = D (las " + juego.candidatos().size() + " enfermedades del catálogo).");
        mensaje("Elegí un síntoma para preguntar.");

        preguntasPane.getChildren().clear();
        for (String sintoma : CatalogoClinico.UNIVERSO) {
            Button boton = new Button(sintoma);
            boton.getStyleClass().add("chip");
            boton.setOnAction(e -> preguntar(sintoma, boton));
            preguntasPane.getChildren().add(boton);
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

        List<Enfermedad> candidatos = juego.candidatos();
        String relacion = si ? "∈" : "∉";
        registrar("¿" + sintoma + " ∈ X?  " + (si ? "Sí" : "No") + ".  C ← { E ∈ C | " + sintoma + " " + relacion
                + " E }  →  |C| pasó de " + antes + " a " + candidatos.size()
                + "  = " + nombres(candidatos));

        String respuesta = si ? "Sí, la enfermedad secreta tiene «" + sintoma + "»."
                : "No, la enfermedad secreta no tiene «" + sintoma + "».";
        if (candidatos.size() == 1) {
            respuesta += " Solo queda un candidato: ¡adiviná!";
        }
        mensajeLabel.setText(respuesta);
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
            mensaje("¡Correcto! Era " + e.nombre() + ". Ganaste " + juego.puntos() + " puntos.", "game-win");
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
        int con = juego.candidatosCon(mejor);
        int sin = juego.candidatos().size() - con;
        mensajeLabel.setText("Pista: preguntá por «" + mejor + "». Divide a C en " + con + " candidatos que lo tienen y "
                + sin + " que no, así que en el peor caso te quedan " + Math.max(con, sin) + ".");
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
        mensaje(motivo + " La enfermedad secreta era " + x.nombre() + ".", "game-lose");
        registrar("Respuesta: X = " + x.nombre() + " = " + FormatoConjunto.formatear(x.sintomas()));
    }

    private void refrescar() {
        boolean terminado = juego.terminado();
        puntosLabel.setText("Puntos de la ronda: " + juego.puntos());
        totalLabel.setText("Total: " + puntajeTotal + " pts en " + rondas + " rondas");
        candidatosTitulo.setText("Candidatos C   (|C| = " + juego.candidatos().size() + ")");
        pistaButton.setDisable(terminado);
        rendirseButton.setDisable(terminado);
        if (terminado) {
            preguntasPane.getChildren().forEach(n -> n.setDisable(true));
        }

        List<Enfermedad> vivos = juego.candidatos();
        candidatosBox.getChildren().clear();
        for (Enfermedad e : juego.catalogo()) {
            candidatosBox.getChildren().add(tarjetaCandidato(e, vivos.contains(e), terminado));
        }
    }

    private VBox tarjetaCandidato(Enfermedad e, boolean vivo, boolean terminado) {
        Label nombre = new Label(e.nombre());
        nombre.getStyleClass().add("card-title");
        nombre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nombre, Priority.ALWAYS);
        HBox fila = new HBox(10, nombre);
        fila.setAlignment(Pos.CENTER_LEFT);
        if (vivo && !terminado) {
            Button adivinar = new Button("¡Es esta!");
            adivinar.getStyleClass().add("small-button");
            adivinar.setOnAction(ev -> adivinar(e));
            fila.getChildren().add(adivinar);
        }

        VBox card = new VBox(4, fila);
        card.getStyleClass().add("rank-card");
        if (terminado && e.equals(juego.secreta())) {
            card.getStyleClass().add("rank-card-selected");
            Label sintomas = new Label(FormatoConjunto.formatear(e.sintomas()));
            sintomas.setWrapText(true);
            sintomas.getStyleClass().add("card-detail");
            card.getChildren().add(sintomas);
        } else if (!vivo) {
            card.getStyleClass().add("rank-card-out");
        }
        return card;
    }

    private void mensaje(String texto, String... estilos) {
        mensajeLabel.setText(texto);
        mensajeLabel.getStyleClass().setAll("game-message");
        mensajeLabel.getStyleClass().addAll(estilos);
    }

    private void registrar(String linea) {
        Label l = new Label((registroBox.getChildren().size() + 1) + ". " + linea);
        l.setWrapText(true);
        l.getStyleClass().add("log-line");
        registroBox.getChildren().add(l);
    }

    private static String nombres(List<Enfermedad> lista) {
        if (lista.isEmpty()) {
            return "∅";
        }
        return lista.stream().map(Enfermedad::nombre).collect(Collectors.joining(", ", "{", "}"));
    }
}
