package com.medset.ui;

import com.medset.logica.FormatoConjunto;
import com.medset.logica.OperacionesConjuntos;
import javafx.geometry.VPos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VennDiagrama extends Pane {

    /** Zonas de un diagrama de dos conjuntos que se pueden resaltar. */
    public enum Zona { SOLO_A, AMBOS, SOLO_B }

    private static final double ANCHO = 560;
    private static final double ALTO = 300;
    private static final double MARGEN = 8;
    private static final double RADIO = 112;
    private static final double CX_A = 210;
    private static final double CX_B = 350;
    private static final double CX_CENTRO = ANCHO / 2;
    private static final double CY = 155;
    private static final double Y_NOMBRE = CY - RADIO - 4;
    private static final int MAX_POR_ZONA = 6;

    private static final Color RESALTADO = Color.web("#2563EB", 0.30);
    private static final Color RESALTADO_ROJO = Color.web("#DC2626", 0.28);
    private static final Color BORDE = Color.web("#334155");
    private static final Color BORDE_UNIVERSO = Color.web("#CBD5E1");
    private static final Color TEXTO = Color.web("#0F172A");

    public VennDiagrama() {
        setPrefSize(ANCHO, ALTO);
        setMinSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);
        getStyleClass().add("venn");
    }

    /**
     * Dibuja dos conjuntos A y B con las zonas indicadas resaltadas.
     *
     * @param alerta si es {@code true} el resaltado es rojo (por ejemplo, un contraejemplo)
     */
    public void dibujar(Set<String> a, Set<String> b, String nombreA, String nombreB,
                        Set<Zona> resaltar, boolean alerta) {
        limpiar();
        getChildren().add(universo());

        Circle circuloA = new Circle(CX_A, CY, RADIO);
        Circle circuloB = new Circle(CX_B, CY, RADIO);
        Color color = alerta ? RESALTADO_ROJO : RESALTADO;
        if (resaltar.contains(Zona.SOLO_A)) {
            pintar(Shape.subtract(circuloA, circuloB), color);
        }
        if (resaltar.contains(Zona.AMBOS)) {
            pintar(Shape.intersect(circuloA, circuloB), color);
        }
        if (resaltar.contains(Zona.SOLO_B)) {
            pintar(Shape.subtract(circuloB, circuloA), color);
        }
        contorno(circuloA);
        contorno(circuloB);

        etiqueta(nombreA, CX_A - 70, Y_NOMBRE, 20);
        etiqueta(nombreB, CX_B + 70, Y_NOMBRE, 20);
        etiqueta("U", ANCHO - 30, 30, 18);

        elementos(OperacionesConjuntos.calcularDiferencia(a, b), CX_A - 52, 104);
        elementos(OperacionesConjuntos.calcularInterseccion(a, b), (CX_A + CX_B) / 2, 84);
        elementos(OperacionesConjuntos.calcularDiferencia(b, a), CX_B + 52, 104);
    }

    /** Dibuja un conjunto A dentro del universo U con su complemento A' resaltado. */
    public void dibujarComplemento(Set<String> a, Set<String> universo, String nombreA) {
        limpiar();
        getChildren().add(universo());

        Circle circulo = new Circle(CX_CENTRO, CY, RADIO);
        pintar(Shape.subtract(universo(), circulo), RESALTADO);
        contorno(circulo);

        etiqueta(nombreA, CX_CENTRO, Y_NOMBRE, 20);
        etiqueta("U", ANCHO - 30, 30, 18);

        elementos(a, CX_CENTRO, 190);
        // Los elementos de U − A se escriben a la izquierda, fuera del círculo.
        elementos(OperacionesConjuntos.calcularDiferencia(universo, a), 92, 140);
    }

    public void limpiar() {
        getChildren().clear();
    }

    private Rectangle universo() {
        Rectangle u = new Rectangle(MARGEN, MARGEN, ANCHO - 2 * MARGEN, ALTO - 2 * MARGEN);
        u.setArcWidth(18);
        u.setArcHeight(18);
        u.setFill(Color.WHITE);
        u.setStroke(BORDE_UNIVERSO);
        u.setStrokeWidth(1.5);
        return u;
    }

    private void pintar(Shape forma, Color color) {
        forma.setFill(color);
        forma.setStroke(null);
        getChildren().add(forma);
    }

    private void contorno(Circle c) {
        Circle borde = new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(BORDE);
        borde.setStrokeWidth(2);
        getChildren().add(borde);
    }

    private void etiqueta(String texto, double x, double y, int tamano) {
        Text t = new Text(texto);
        t.setFont(Font.font("System", FontWeight.BOLD, tamano));
        t.setFill(TEXTO);
        t.setX(x - t.getLayoutBounds().getWidth() / 2);
        t.setY(y);
        getChildren().add(t);
    }

    private void elementos(Set<String> conjunto, double cx, double ancho) {
        Text t = new Text(conjunto.isEmpty() ? "∅" : String.join("\n", recortar(conjunto)));
        t.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12.5));
        t.setFill(TEXTO);
        t.setTextAlignment(TextAlignment.CENTER);
        t.setTextOrigin(VPos.TOP);
        t.setWrappingWidth(ancho);
        t.setX(cx - ancho / 2);
        t.setY(CY - t.getLayoutBounds().getHeight() / 2);
        getChildren().add(t);
    }

    /** Si la zona tiene demasiados elementos, muestra los primeros y "(+n más)". */
    private List<String> recortar(Set<String> conjunto) {
        List<String> ordenados = FormatoConjunto.ordenar(conjunto);
        if (ordenados.size() <= MAX_POR_ZONA) {
            return ordenados;
        }
        int resto = ordenados.size() - (MAX_POR_ZONA - 1);
        List<String> visibles = new ArrayList<>(ordenados.subList(0, MAX_POR_ZONA - 1));
        visibles.add("(+" + resto + " más)");
        return visibles;
    }
}
