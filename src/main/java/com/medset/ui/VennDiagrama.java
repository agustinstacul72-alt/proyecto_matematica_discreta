package com.medset.ui;

import com.medset.logica.FormatoConjunto;
import com.medset.logica.OperacionesConjuntos;
import javafx.geometry.VPos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Diagrama de Venn dibujado con figuras JavaFX. Los conjuntos son óvalos que se
 * estiran hacia abajo cuando tienen muchos elementos, para que el texto siempre
 * quede dentro de su zona.
 */
public class VennDiagrama extends Pane {

    /** Zonas de un diagrama de dos conjuntos que se pueden resaltar. */
    public enum Zona { SOLO_A, AMBOS, SOLO_B }

    private static final double ANCHO = 560;
    private static final double MARGEN = 8;
    private static final double ESPACIO_ARRIBA = 40;
    private static final double ESPACIO_ABAJO = 28;
    private static final double RADIO_X = 150;
    private static final double RADIO_X_COMPLEMENTO = 120;
    private static final double RADIO_Y = 120;
    private static final double CX_A = 200;
    private static final double CX_B = 360;
    private static final double CX_CENTRO = ANCHO / 2;
    /** Cuánto puede estirarse el óvalo como máximo (1,8 veces su alto). */
    private static final double ESTIRAMIENTO_MAXIMO = 1.8;

    // Rectángulos que entran completos dentro de cada zona cuando el óvalo no está estirado.
    // Al estirar el óvalo, el alto de cada rectángulo crece en la misma proporción.
    private static final Caja SOLO = new Caja(132, 120);
    private static final Caja AMBOS = new Caja(96, 120);
    private static final Caja DENTRO = new Caja(170, 160);
    private static final Caja FUERA = new Caja(136, 220);

    private static final String FUENTE = "Segoe UI";
    private static final double TAMANO_LETRA = 13;
    /** El símbolo ∅ se dibuja más grande para que se lea bien solo en su zona. */
    private static final double TAMANO_VACIO = 22;
    /** Tamaños más chicos que se prueban si el óvalo ya no puede crecer. */
    private static final double[] TAMANOS_DE_RESPALDO = {12, 11, 10};

    private static final Color RESALTADO = Color.web("#2563EB", 0.30);
    private static final Color RESALTADO_ROJO = Color.web("#DC2626", 0.28);
    private static final Color BORDE = Color.web("#334155");
    private static final Color BORDE_UNIVERSO = Color.web("#CBD5E1");
    private static final Color TEXTO = Color.web("#0F172A");

    private record Caja(double ancho, double alto) {
    }

    private double radioY = RADIO_Y;

    public VennDiagrama() {
        getStyleClass().add("venn");
        ajustarTamano(1);
    }

    /**
     * Dibuja dos conjuntos A y B con las zonas indicadas resaltadas.
     *
     * @param alerta si es {@code true} el resaltado es rojo (por ejemplo, un contraejemplo)
     */
    public void dibujar(Set<String> a, Set<String> b, String nombreA, String nombreB,
                        Set<Zona> resaltar, boolean alerta) {
        Set<String> soloA = OperacionesConjuntos.calcularDiferencia(a, b);
        Set<String> ambos = OperacionesConjuntos.calcularInterseccion(a, b);
        Set<String> soloB = OperacionesConjuntos.calcularDiferencia(b, a);

        limpiar();
        ajustarTamano(estiramiento(soloA, SOLO, ambos, AMBOS, soloB, SOLO));
        getChildren().add(universo());

        Ellipse conjuntoA = ovalo(CX_A, RADIO_X);
        Ellipse conjuntoB = ovalo(CX_B, RADIO_X);
        Color color = alerta ? RESALTADO_ROJO : RESALTADO;
        if (resaltar.contains(Zona.SOLO_A)) {
            pintar(Shape.subtract(conjuntoA, conjuntoB), color);
        }
        if (resaltar.contains(Zona.AMBOS)) {
            pintar(Shape.intersect(conjuntoA, conjuntoB), color);
        }
        if (resaltar.contains(Zona.SOLO_B)) {
            pintar(Shape.subtract(conjuntoB, conjuntoA), color);
        }
        contorno(conjuntoA);
        contorno(conjuntoB);

        etiqueta(nombreA, CX_A - 90, ESPACIO_ARRIBA - 6, 20);
        etiqueta(nombreB, CX_B + 90, ESPACIO_ARRIBA - 6, 20);
        etiqueta("U", ANCHO - 26, 32, 18);

        elementos(soloA, CX_A - 62, SOLO);
        elementos(ambos, (CX_A + CX_B) / 2, AMBOS);
        elementos(soloB, CX_B + 62, SOLO);
    }

    /** Dibuja un conjunto A dentro del universo U con su complemento A' resaltado. */
    public void dibujarComplemento(Set<String> a, Set<String> universo, String nombreA) {
        Set<String> fuera = OperacionesConjuntos.calcularDiferencia(universo, a);

        limpiar();
        ajustarTamano(estiramiento(a, DENTRO, fuera, FUERA));
        getChildren().add(universo());

        Ellipse conjunto = ovalo(CX_CENTRO, RADIO_X_COMPLEMENTO);
        pintar(Shape.subtract(universo(), conjunto), RESALTADO);
        contorno(conjunto);

        etiqueta(nombreA, CX_CENTRO, ESPACIO_ARRIBA - 6, 20);
        etiqueta("U", ANCHO - 26, 32, 18);

        elementos(a, CX_CENTRO, DENTRO);
        // Los elementos de U − A se escriben a la izquierda, fuera del óvalo.
        elementos(fuera, MARGEN + 76, FUERA);
    }

    public void limpiar() {
        getChildren().clear();
    }

    /**
     * Cuánto hay que estirar los óvalos para que cada conjunto entre en su zona
     * con la letra normal. Recibe pares (conjunto, caja de su zona).
     */
    private static double estiramiento(Object... conjuntosYCajas) {
        double necesario = 1;
        for (int i = 0; i < conjuntosYCajas.length; i += 2) {
            @SuppressWarnings("unchecked")
            Set<String> conjunto = (Set<String>) conjuntosYCajas[i];
            Caja caja = (Caja) conjuntosYCajas[i + 1];
            double alto = texto(lineas(conjunto), TAMANO_LETRA, caja.ancho()).getLayoutBounds().getHeight();
            necesario = Math.max(necesario, alto / caja.alto());
        }
        return Math.min(necesario, ESTIRAMIENTO_MAXIMO);
    }

    private void ajustarTamano(double estiramiento) {
        radioY = RADIO_Y * estiramiento;
        double alto = ESPACIO_ARRIBA + 2 * radioY + ESPACIO_ABAJO;
        setPrefSize(ANCHO, alto);
        setMinSize(ANCHO, alto);
        setMaxSize(ANCHO, alto);
    }

    private double centroY() {
        return ESPACIO_ARRIBA + radioY;
    }

    private double estiramientoActual() {
        return radioY / RADIO_Y;
    }

    private Ellipse ovalo(double cx, double radioX) {
        return new Ellipse(cx, centroY(), radioX, radioY);
    }

    private Rectangle universo() {
        Rectangle u = new Rectangle(MARGEN, MARGEN, ANCHO - 2 * MARGEN, getPrefHeight() - 2 * MARGEN);
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

    private void contorno(Ellipse e) {
        Ellipse borde = new Ellipse(e.getCenterX(), e.getCenterY(), e.getRadiusX(), e.getRadiusY());
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(BORDE);
        borde.setStrokeWidth(2);
        getChildren().add(borde);
    }

    private void etiqueta(String texto, double x, double y, int tamano) {
        Text t = new Text(texto);
        t.setFont(Font.font(FUENTE, FontWeight.BOLD, tamano));
        t.setFill(TEXTO);
        t.setX(x - t.getLayoutBounds().getWidth() / 2);
        t.setY(y);
        getChildren().add(t);
    }

    /**
     * Escribe los elementos centrados en la zona sin salirse de su caja. Si el
     * óvalo ya llegó a su tamaño máximo y aun así no entran, primero achica la
     * letra y después muestra los primeros elementos y "(+n más)".
     */
    private void elementos(Set<String> conjunto, double cx, Caja caja) {
        double alto = caja.alto() * estiramientoActual();
        List<String> lineas = lineas(conjunto);
        Text t = texto(lineas, conjunto.isEmpty() ? TAMANO_VACIO : TAMANO_LETRA, caja.ancho());
        if (!cabe(t, caja.ancho(), alto)) {
            t = textoQueCabe(lineas, caja.ancho(), alto);
        }
        t.setX(cx - caja.ancho() / 2);
        t.setY(centroY() - t.getLayoutBounds().getHeight() / 2);
        getChildren().add(t);
    }

    private static Text textoQueCabe(List<String> lineas, double ancho, double alto) {
        for (double tamano : TAMANOS_DE_RESPALDO) {
            Text t = texto(lineas, tamano, ancho);
            if (cabe(t, ancho, alto)) {
                return t;
            }
        }
        double menor = TAMANOS_DE_RESPALDO[TAMANOS_DE_RESPALDO.length - 1];
        Text t = texto(lineas, menor, ancho);
        for (int visibles = lineas.size() - 1; visibles >= 1 && !cabe(t, ancho, alto); visibles--) {
            t = texto(recortar(lineas, visibles), menor, ancho);
        }
        return t;
    }

    private static List<String> lineas(Set<String> conjunto) {
        return conjunto.isEmpty() ? List.of("∅") : FormatoConjunto.ordenar(conjunto);
    }

    private static Text texto(List<String> lineas, double tamano, double ancho) {
        Text t = new Text(String.join("\n", lineas));
        t.setFont(Font.font(FUENTE, FontWeight.SEMI_BOLD, tamano));
        t.setFill(TEXTO);
        t.setTextAlignment(TextAlignment.CENTER);
        t.setTextOrigin(VPos.TOP);
        t.setLineSpacing(1.5);
        t.setWrappingWidth(ancho);
        return t;
    }

    private static boolean cabe(Text t, double ancho, double alto) {
        return t.getLayoutBounds().getWidth() <= ancho + 0.5 && t.getLayoutBounds().getHeight() <= alto;
    }

    /** Los primeros {@code visibles} elementos y una línea "(+n más)" con los que faltan. */
    private static List<String> recortar(List<String> lineas, int visibles) {
        List<String> recortadas = new ArrayList<>(lineas.subList(0, visibles));
        recortadas.add("(+" + (lineas.size() - visibles) + " más)");
        return recortadas;
    }
}
