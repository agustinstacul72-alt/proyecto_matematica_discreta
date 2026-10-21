package com.medset.medsetai.controller;

import com.medset.medsetai.logica.FormatoConjunto;
import com.medset.medsetai.logica.OperacionesConjuntos;
import com.medset.medsetai.modelo.CatalogoClinico;
import com.medset.medsetai.modelo.Enfermedad;
import com.medset.medsetai.modelo.MotorTriage;
import com.medset.medsetai.modelo.NivelUrgencia;
import com.medset.medsetai.modelo.ResultadoAnalisis;
import com.medset.medsetai.ui.VennDiagrama;
import com.medset.medsetai.ui.VennDiagrama.Region;
import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LaboratorioController {


    private static final List<Set<String>> EJEMPLOS = List.of(
            Set.of("fiebre", "tos", "dolor de cabeza", "cansancio"),
            Set.of("fiebre", "tos", "dolor de garganta", "cansancio"),
            Set.of("estornudos", "congestión nasal", "picazón en los ojos"),
            Set.of("fiebre", "tos", "dificultad para respirar", "dolor de pecho"),
            Set.of("fiebre", "dolor detrás de los ojos", "sarpullido", "dolor muscular"));

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
    private Enfermedad enfoque;
    private int ejemplo = 0;

    @FXML
    private void initialize() {
        for (String sintoma : CatalogoClinico.UNIVERSO) {
            ToggleButton b = new ToggleButton(sintoma);
            b.getStyleClass().add("chip");
            if (CatalogoClinico.SINTOMAS_DE_ALARMA.contains(sintoma)) {
                b.getStyleClass().add("chip-alarma");
            }
            b.selectedProperty().addListener((obs, antes, ahora) -> {
                enfoque = null;
                actualizar();
            });
            botones.put(sintoma, b);
            sintomasPane.getChildren().add(b);
        }
        actualizar();
    }

    @FXML
    private void volverAlInicio(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "welcome-view.fxml");
    }

    @FXML
    private void pacienteEjemplo() {
        cargar(EJEMPLOS.get(ejemplo));
        ejemplo = (ejemplo + 1) % EJEMPLOS.size();
    }

    @FXML
    private void limpiar() {
        cargar(Set.of());
    }

    private void cargar(Set<String> sintomas) {
        for (Map.Entry<String, ToggleButton> e : botones.entrySet()) {
            e.getValue().setSelected(sintomas.contains(e.getKey()));
        }
        enfoque = null;
        actualizar();
    }

    /** P: los síntomas seleccionados. */
    private Set<String> paciente() {
        Set<String> p = new LinkedHashSet<>();
        for (Map.Entry<String, ToggleButton> e : botones.entrySet()) {
            if (e.getValue().isSelected()) {
                p.add(e.getKey());
            }
        }
        return p;
    }

    private void actualizar() {
        Set<String> p = paciente();
        Set<String> u = CatalogoClinico.UNIVERSO;

        pLabel.setText("P = " + FormatoConjunto.formatear(p) + "\n|P| = " + p.size() + " de |U| = " + u.size());
        complementoLabel.setText("Síntomas descartados: P' = U − P tiene " + (u.size() - p.size()) + " elementos.");
        Set<String> alarmas = motor.alarmasPresentes(p);
        alarmaLabel.setText("R = síntomas de alarma = " + FormatoConjunto.formatear(CatalogoClinico.SINTOMAS_DE_ALARMA)
                + "\nP ∩ R = " + FormatoConjunto.formatear(alarmas));

        rankingBox.getChildren().clear();
        if (p.isEmpty()) {
            urgenciaBadge.setText("SIN DATOS");
            urgenciaBadge.getStyleClass().setAll("badge", "badge-neutral");
            urgenciaTexto.setText("Seleccioná al menos un síntoma. P = ∅ no se compara con ninguna enfermedad.");
            reglaLabel.setText("");
            vennTitulo.setText("");
            venn.getChildren().clear();
            return;
        }

        NivelUrgencia nivel = motor.nivelDeUrgencia(p);
        urgenciaBadge.setText("URGENCIA " + nivel.etiqueta().toUpperCase());
        urgenciaBadge.getStyleClass().setAll("badge", "badge-" + nivel.name().toLowerCase());
        urgenciaTexto.setText(nivel.indicacion());
        reglaLabel.setText(motor.explicarUrgencia(p));

        List<ResultadoAnalisis> ranking = motor.clasificar(p);
        if (enfoque == null) {
            enfoque = ranking.get(0).enfermedad();
        }
        for (ResultadoAnalisis r : ranking) {
            rankingBox.getChildren().add(tarjeta(r, r.enfermedad().equals(enfoque)));
        }

        vennTitulo.setText("Diagrama de Venn: P (paciente) frente a E = " + enfoque.nombre());
        venn.dibujar(p, enfoque.sintomas(), null, "P", "E", EnumSet.of(Region.AMBOS), false);
    }

    private VBox tarjeta(ResultadoAnalisis r, boolean seleccionada) {
        Label nombre = new Label(r.enfermedad().nombre());
        nombre.getStyleClass().add("card-title");
        ProgressBar barra = new ProgressBar(r.similitud());
        barra.setPrefWidth(220);
        barra.getStyleClass().add("jaccard-bar");
        Label pct = new Label("J = " + r.porcentaje() + "%");
        pct.getStyleClass().add("card-pct");
        HBox cabecera = new HBox(12, nombre, barra, pct);
        HBox.setHgrow(nombre, Priority.ALWAYS);
        nombre.setMaxWidth(Double.MAX_VALUE);
        cabecera.setStyle("-fx-alignment: CENTER_LEFT;");

        Label detalle = new Label(
                "P ∩ E (coinciden) = " + FormatoConjunto.formatear(r.coincidencias())
                        + "\nE − P (le faltan al paciente) = " + FormatoConjunto.formatear(r.faltantes())
                        + "\nP − E (no los explica) = " + FormatoConjunto.formatear(r.noExplicados()));
        detalle.setWrapText(true);
        detalle.getStyleClass().add("card-detail");

        HBox etiquetas = new HBox(8);
        if (r.coincidenciaExacta()) {
            etiquetas.getChildren().add(tag("P = E  coincidencia exacta", "tag-ok"));
        } else {
            if (r.pSubconjuntoE()) {
                etiquetas.getChildren().add(tag("P ⊆ E  explica todos los síntomas", "tag-ok"));
            }
            if (r.eSubconjuntoP()) {
                etiquetas.getChildren().add(tag("E ⊆ P  cuadro completo", "tag-ok"));
            }
            if (r.coincidencias().isEmpty()) {
                etiquetas.getChildren().add(tag("P ∩ E = ∅  disjuntos", "tag-muted"));
            }
        }
        etiquetas.getChildren().add(tag("urgencia base: " + r.enfermedad().urgencia().etiqueta(), "tag-muted"));

        VBox card = new VBox(6, cabecera, detalle, etiquetas);
        card.getStyleClass().add("rank-card");
        if (seleccionada) {
            card.getStyleClass().add("rank-card-selected");
        }
        if (OperacionesConjuntos.sonDisjuntos(paciente(), r.enfermedad().sintomas())) {
            card.setOpacity(0.6);
        }
        card.setOnMouseClicked(e -> {
            enfoque = r.enfermedad();
            actualizar();
        });
        return card;
    }

    private Label tag(String texto, String estilo) {
        Label l = new Label(texto);
        l.getStyleClass().addAll("tag", estilo);
        return l;
    }
}
