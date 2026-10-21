package com.medset.controller;

import com.medset.logica.FormatoConjunto;
import com.medset.modelo.CatalogoClinico;
import com.medset.modelo.Enfermedad;
import com.medset.modelo.JuegoDetective;
import com.medset.modelo.TextosJuego;
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
        registrar(TextosJuego.registroInicio(juego));
        mensaje(TextosJuego.MENSAJE_INICIAL);

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
        boton.setText(TextosJuego.botonPreguntado(sintoma, si));

        List<Enfermedad> candidatos = juego.candidatos();
        registrar(TextosJuego.registroPregunta(sintoma, si, antes, candidatos));
        mensajeLabel.setText(TextosJuego.respuestaPregunta(sintoma, si, candidatos));
        refrescar();
    }

    private void adivinar(Enfermedad e) {
        if (juego.terminado()) {
            return;
        }
        if (juego.adivinar(e)) {
            puntajeTotal += juego.puntos();
            rondas++;
            registrar(TextosJuego.registroAcierto(e));
            mensaje(TextosJuego.mensajeAcierto(e, juego.puntos()), "game-win");
        } else {
            registrar(TextosJuego.registroError(e));
            if (juego.terminado()) {
                terminarPerdiendo(TextosJuego.SIN_PUNTOS);
            } else {
                mensajeLabel.setText(TextosJuego.mensajeError(e));
            }
        }
        refrescar();
    }

    @FXML
    private void pista() {
        if (juego.terminado()) {
            return;
        }
        mensajeLabel.setText(TextosJuego.pista(juego));
    }

    @FXML
    private void rendirse() {
        if (juego.terminado()) {
            return;
        }
        juego.rendirse();
        rondas++;
        terminarPerdiendo(TextosJuego.RENDICION);
        refrescar();
    }

    private void terminarPerdiendo(String motivo) {
        Enfermedad x = juego.secreta();
        mensaje(TextosJuego.mensajeDerrota(motivo, x), "game-lose");
        registrar(TextosJuego.registroRespuesta(x));
    }

    private void refrescar() {
        boolean terminado = juego.terminado();
        puntosLabel.setText(TextosJuego.puntos(juego));
        totalLabel.setText(TextosJuego.total(puntajeTotal, rondas));
        candidatosTitulo.setText(TextosJuego.tituloCandidatos(juego));
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
        Label l = new Label(TextosJuego.lineaRegistro(registroBox.getChildren().size() + 1, linea));
        l.setWrapText(true);
        l.getStyleClass().add("log-line");
        registroBox.getChildren().add(l);
    }
}
