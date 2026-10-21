package com.medset.medsetai.ui;

import com.medset.medsetai.logica.FormatoConjunto;
import com.medset.medsetai.logica.OperacionesConjuntos;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class VennDiagrama extends Pane {


    public enum Region { SOLO_A, AMBOS, SOLO_B, FUERA }

    private static final double ANCHO = 560;
    private static final double ALTO = 300;
    private static final double RADIO = 112;
    private static final double CX_A = 210;
    private static final double CX_B = 350;
    private static final double CY = 155;
    private static final int MAX_POR_REGION = 6;

    private static final Color RESALTADO = Color.web("#2563EB", 0.30);
    private static final Color RESALTADO_ROJO = Color.web("#DC2626", 0.28);
    private static final Color BORDE = Color.web("#334155");

    public VennDiagrama() {
        setPrefSize(ANCHO, ALTO);
        setMinSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);
        getStyleClass().add("venn");
    }


    public void dibujar(Set<String> a, Set<String> b, Set<String> universo, String nombreA, String nombreB,
                        EnumSet<Region> resaltar, boolean alerta) {
        getChildren().clear();
        boolean hayB = b != null;
        Set<String> conjuntoB = hayB ? b : Set.of();

        Rectangle u = new Rectangle(8, 8, ANCHO - 16, ALTO - 16);
        u.setArcWidth(18);
        u.setArcHeight(18);
        u.setFill(Color.WHITE);
        u.setStroke(Color.web("#CBD5E1"));
        u.setStrokeWidth(1.5);
        getChildren().add(u);

        double cxA = hayB ? CX_A : ANCHO / 2;
        Circle circuloA = new Circle(cxA, CY, RADIO);
        Circle circuloB = new Circle(CX_B, CY, RADIO);
        Color color = alerta ? RESALTADO_ROJO : RESALTADO;

        if (resaltar.contains(Region.FUERA)) {
            Shape fuera = Shape.subtract(new Rectangle(8, 8, ANCHO - 16, ALTO - 16),
                    hayB ? Shape.union(circuloA, circuloB) : circuloA);
            pintar(fuera, color);
        }
        if (resaltar.contains(Region.SOLO_A)) {
            pintar(hayB ? Shape.subtract(circuloA, circuloB) : copia(circuloA), color);
        }
        if (hayB && resaltar.contains(Region.AMBOS)) {
            pintar(Shape.intersect(circuloA, circuloB), color);
        }
        if (hayB && resaltar.contains(Region.SOLO_B)) {
            pintar(Shape.subtract(circuloB, circuloA), color);
        }

        contorno(circuloA);
        if (hayB) {
            contorno(circuloB);
        }

        etiqueta(nombreA, cxA - (hayB ? 70 : 0), CY - RADIO - 8 + 4, 20);
        if (hayB) {
            etiqueta(nombreB, CX_B + 70, CY - RADIO - 8 + 4, 20);
        }
        etiqueta("U", ANCHO - 30, 30, 18);

        Set<String> soloA = OperacionesConjuntos.calcularDiferencia(a, conjuntoB);
        Set<String> ambos = OperacionesConjuntos.calcularInterseccion(a, conjuntoB);
        Set<String> soloB = OperacionesConjuntos.calcularDiferencia(conjuntoB, a);

        if (hayB) {
            elementos(soloA, CX_A - 52, 104);
            elementos(ambos, (CX_A + CX_B) / 2, 84);
            elementos(soloB, CX_B + 52, 104);
        } else {
            elementos(soloA, cxA, 190);
        }
        if (universo != null && !hayB) {
            // Con un solo conjunto, los elementos de U − A se escriben a la izquierda, fuera del círculo.
            elementos(OperacionesConjuntos.calcularDiferencia(universo, a), 92, 140);
        } else if (universo != null) {
            Set<String> fuera = OperacionesConjuntos.calcularDiferencia(universo,
                    OperacionesConjuntos.calcularUnion(a, conjuntoB));
            Text t = new Text(16, ALTO - 26, recortar(fuera, 8));
            t.setFont(Font.font("System", 12));
            t.setFill(Color.web("#64748B"));
            t.setWrappingWidth(ANCHO - 40);
            getChildren().add(t);
        }
    }

    private void pintar(Shape forma, Color color) {
        forma.setFill(color);
        forma.setStroke(null);
        getChildren().add(forma);
    }

    private Circle copia(Circle c) {
        return new Circle(c.getCenterX(), c.getCenterY(), c.getRadius());
    }

    private void contorno(Circle c) {
        Circle borde = copia(c);
        borde.setFill(Color.TRANSPARENT);
        borde.setStroke(BORDE);
        borde.setStrokeWidth(2);
        getChildren().add(borde);
    }

    private void etiqueta(String texto, double x, double y, int tamano) {
        Text t = new Text(texto);
        t.setFont(Font.font("System", FontWeight.BOLD, tamano));
        t.setFill(Color.web("#0F172A"));
        t.setX(x - t.getLayoutBounds().getWidth() / 2);
        t.setY(y);
        getChildren().add(t);
    }

    private void elementos(Set<String> conjunto, double cx, double ancho) {
        Text t = new Text(conjunto.isEmpty() ? "∅" : String.join("\n", lista(conjunto)));
        t.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12.5));
        t.setFill(Color.web("#0F172A"));
        t.setTextAlignment(TextAlignment.CENTER);
        t.setTextOrigin(VPos.TOP);
        t.setWrappingWidth(ancho);
        t.setX(cx - ancho / 2);
        t.setY(CY - t.getLayoutBounds().getHeight() / 2);
        getChildren().add(t);
    }

    private List<String> lista(Set<String> conjunto) {
        List<String> ordenados = FormatoConjunto.ordenar(conjunto);
        if (ordenados.size() > MAX_POR_REGION) {
            int resto = ordenados.size() - (MAX_POR_REGION - 1);
            ordenados = new java.util.ArrayList<>(ordenados.subList(0, MAX_POR_REGION - 1));
            ordenados.add("(+" + resto + " más)");
        }
        return ordenados;
    }

    private String recortar(Set<String> fuera, int max) {
        if (fuera.isEmpty()) {
            return "Fuera de los conjuntos: ∅";
        }
        List<String> l = FormatoConjunto.ordenar(fuera);
        String base = String.join(", ", l.size() > max ? l.subList(0, max) : l);
        return "Fuera de los conjuntos: " + base + (l.size() > max ? ", …" : "");
    }
}
