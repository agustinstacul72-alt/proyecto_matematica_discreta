package com.medset.controller;

import com.medset.logica.ZonaVenn;
import com.medset.modelo.CatalogoClinico;
import com.medset.modelo.Enfermedad;
import com.medset.modelo.EvaluacionTriage;
import com.medset.modelo.MotorTriage;
import com.medset.modelo.NivelUrgencia;
import com.medset.modelo.ResultadoAnalisis;
import com.medset.modelo.TextosTriage;
import com.medset.modelo.TipoPaciente;
import com.medset.ui.VennDiagrama;
import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LaboratorioController {

    @FXML private FlowPane edadPane;
    @FXML private FlowPane condicionesPane;
    @FXML private Label tipoLabel;
    @FXML private FlowPane sintomasPane;
    @FXML private Label pLabel;
    @FXML private Label complementoLabel;
    @FXML private Label alarmaLabel;
    @FXML private Label urgenciaBadge;
    @FXML private Label urgenciaTexto;
    @FXML private Label reglaLabel;
    @FXML private Label vennTitulo;
    @FXML private VennDiagrama venn;
    @FXML private VBox rankingBox;

    private final MotorTriage motor = new MotorTriage();
    private final Map<String, ToggleButton> botones = new LinkedHashMap<>();
    private final ToggleGroup grupoEdad = new ToggleGroup();
    private TipoPaciente edad = TipoPaciente.ADULTO;
    private final Set<TipoPaciente> condiciones = EnumSet.noneOf(TipoPaciente.class);
    private Enfermedad enfoque;
    private int ejemplo = 0;
    /** Evita recalcular una vez por cada botón cuando se cargan varios síntomas juntos. */
    private boolean cargando = false;

    @FXML
    private void initialize() {
        crearSelectorDeEdad();
        crearSelectorDeCondiciones();
        crearBotonesDeSintomas();
        actualizar();
    }

    private void crearSelectorDeEdad() {
        for (TipoPaciente t : TipoPaciente.gruposDeEdad()) {
            ToggleButton boton = new ToggleButton(TextosTriage.botonEdad(t));
            boton.getStyleClass().add("chip");
            boton.setUserData(t);
            boton.setToggleGroup(grupoEdad);
            boton.setSelected(t == edad);
            edadPane.getChildren().add(boton);
        }
        grupoEdad.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                anterior.setSelected(true); // siempre debe haber un grupo de edad elegido
                return;
            }
            edad = (TipoPaciente) actual.getUserData();
            actualizar();
        });
    }

    private void crearSelectorDeCondiciones() {
        for (TipoPaciente t : TipoPaciente.condiciones()) {
            ToggleButton boton = new ToggleButton(t.nombre());
            boton.getStyleClass().add("chip");
            boton.selectedProperty().addListener((obs, antes, marcada) -> {
                if (marcada) {
                    condiciones.add(t);
                } else {
                    condiciones.remove(t);
                }
                actualizar();
            });
            condicionesPane.getChildren().add(boton);
        }
    }

    /** Perfil del paciente: su grupo de edad y sus condiciones. */
    private Set<TipoPaciente> perfil() {
        Set<TipoPaciente> perfil = EnumSet.of(edad);
        perfil.addAll(condiciones);
        return perfil;
    }

    private void crearBotonesDeSintomas() {
        for (String sintoma : CatalogoClinico.UNIVERSO) {
            ToggleButton boton = new ToggleButton(sintoma);
            boton.getStyleClass().add("chip");
            if (CatalogoClinico.SINTOMAS_DE_ALARMA.contains(sintoma)) {
                boton.getStyleClass().add("chip-alarma");
            }
            boton.selectedProperty().addListener((obs, antes, ahora) -> {
                if (!cargando) {
                    enfoque = null;
                    actualizar();
                }
            });
            botones.put(sintoma, boton);
            sintomasPane.getChildren().add(boton);
        }
    }

    @FXML
    private void volverAlInicio(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.INICIO);
    }

    @FXML
    private void pacienteEjemplo() {
        List<Set<String>> ejemplos = TextosTriage.PACIENTES_DE_EJEMPLO;
        cargar(ejemplos.get(ejemplo));
        ejemplo = (ejemplo + 1) % ejemplos.size();
    }

    @FXML
    private void limpiar() {
        cargar(Set.of());
    }

    private void cargar(Set<String> sintomas) {
        cargando = true;
        botones.forEach((sintoma, boton) -> boton.setSelected(sintomas.contains(sintoma)));
        cargando = false;
        enfoque = null;
        actualizar();
    }

    /** P: los síntomas seleccionados. */
    private Set<String> paciente() {
        Set<String> p = new LinkedHashSet<>();
        botones.forEach((sintoma, boton) -> {
            if (boton.isSelected()) {
                p.add(sintoma);
            }
        });
        return p;
    }

    private void actualizar() {
        Set<String> p = paciente();
        EvaluacionTriage evaluacion = motor.evaluar(p, perfil());

        tipoLabel.setText(TextosTriage.riesgos(evaluacion));
        pLabel.setText(TextosTriage.paciente(p));
        complementoLabel.setText(TextosTriage.complemento(p));
        alarmaLabel.setText(TextosTriage.alarmas(evaluacion));

        rankingBox.getChildren().clear();
        if (p.isEmpty()) {
            mostrarSinDatos();
            return;
        }

        NivelUrgencia nivel = evaluacion.nivel();
        urgenciaBadge.setText(TextosTriage.urgencia(nivel));
        urgenciaBadge.getStyleClass().setAll("badge", TextosTriage.estiloUrgencia(nivel));
        urgenciaTexto.setText(nivel.indicacion());
        reglaLabel.setText(evaluacion.regla());

        if (enfoque == null) {
            enfoque = evaluacion.mejorCoincidencia().enfermedad();
        }
        for (ResultadoAnalisis r : evaluacion.ranking()) {
            rankingBox.getChildren().add(tarjeta(r, r.enfermedad().equals(enfoque)));
        }

        vennTitulo.setText(TextosTriage.tituloVenn(enfoque));
        venn.dibujar(p, enfoque.sintomas(), "P", "E", EnumSet.of(ZonaVenn.AMBOS), false);
    }

    private void mostrarSinDatos() {
        urgenciaBadge.setText(TextosTriage.SIN_DATOS);
        urgenciaBadge.getStyleClass().setAll("badge", "badge-neutral");
        urgenciaTexto.setText(TextosTriage.SIN_DATOS_INDICACION);
        reglaLabel.setText("");
        vennTitulo.setText("");
        venn.limpiar();
    }

    private VBox tarjeta(ResultadoAnalisis r, boolean seleccionada) {
        Label nombre = new Label(r.enfermedad().nombre());
        nombre.getStyleClass().add("card-title");
        nombre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nombre, Priority.ALWAYS);
        ProgressBar barra = new ProgressBar(r.similitud());
        barra.setPrefWidth(220);
        barra.getStyleClass().add("jaccard-bar");
        Label pct = new Label(TextosTriage.porcentaje(r));
        pct.getStyleClass().add("card-pct");
        HBox cabecera = new HBox(12, nombre, barra, pct);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label detalle = new Label(TextosTriage.detalle(r));
        detalle.setWrapText(true);
        detalle.getStyleClass().add("card-detail");

        VBox card = new VBox(6, cabecera, detalle, etiquetas(r));
        card.getStyleClass().add("rank-card");
        if (seleccionada) {
            card.getStyleClass().add("rank-card-selected");
        }
        if (TextosTriage.atenuada(r)) {
            card.setOpacity(0.6);
        }
        card.setOnMouseClicked(e -> {
            enfoque = r.enfermedad();
            actualizar();
        });
        return card;
    }

    private HBox etiquetas(ResultadoAnalisis r) {
        HBox etiquetas = new HBox(8);
        for (TextosTriage.Etiqueta e : TextosTriage.etiquetas(r)) {
            etiquetas.getChildren().add(tag(e.texto(), e.ok() ? "tag-ok" : "tag-muted"));
        }
        return etiquetas;
    }

    private Label tag(String texto, String estilo) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("tag", estilo);
        return l;
    }
}
