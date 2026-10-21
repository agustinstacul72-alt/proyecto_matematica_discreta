package com.medset.util;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Carga las vistas FXML y cambia de pantalla dentro de la misma ventana.
 */
public final class SceneManager {

    private static final String CARPETA_VISTAS = "/com/medset/view/";

    private SceneManager() {
    }

    /**
     * Carga la vista indicada.
     *
     * @param vista pantalla que se quiere cargar
     * @return nodo raíz de la vista
     * @throws IOException si el FXML no se puede leer
     */
    public static Parent cargar(Vista vista) throws IOException {
        return FXMLLoader.load(SceneManager.class.getResource(CARPETA_VISTAS + vista.archivo()));
    }

    /**
     * Reemplaza el contenido de la ventana desde la que se disparó el evento.
     * Se reutiliza la escena para conservar el tamaño de la ventana y no
     * volver a crear la escena en cada cambio de pantalla.
     *
     * @param evento evento de la interfaz (por ejemplo, el clic en un botón)
     * @param vista  pantalla que se quiere mostrar
     * @throws IOException si el FXML no se puede leer
     */
    public static void irA(Event evento, Vista vista) throws IOException {
        ((Node) evento.getSource()).getScene().setRoot(cargar(vista));
    }
}
