package com.medset.web;

import java.util.Collection;

/** Arma texto JSON sin librerías externas, para pasarle datos a la página. */
final class Json {

    private final StringBuilder sb = new StringBuilder();
    private boolean primero = true;

    private Json(char apertura) {
        sb.append(apertura);
    }

    static Json objeto() {
        return new Json('{');
    }

    static Json lista() {
        return new Json('[');
    }

    Json campo(String nombre, String valor) {
        return clave(nombre).valor(valor);
    }

    Json campo(String nombre, boolean valor) {
        clave(nombre).sb.append(valor);
        return this;
    }

    Json campo(String nombre, double valor) {
        clave(nombre).sb.append(valor);
        return this;
    }

    Json campo(String nombre, Collection<String> valores) {
        clave(nombre).sb.append(textos(valores));
        return this;
    }

    /** Agrega un valor ya escrito en JSON (otro objeto o lista). */
    Json crudo(String nombre, String json) {
        clave(nombre).sb.append(json);
        return this;
    }

    Json elemento(String json) {
        separar();
        sb.append(json);
        return this;
    }

    String cerrar() {
        return sb.append(sb.charAt(0) == '{' ? '}' : ']').toString();
    }

    static String textos(Collection<String> valores) {
        Json lista = lista();
        for (String v : valores) {
            lista.separar();
            lista.valor(v);
        }
        return lista.cerrar();
    }

    private Json clave(String nombre) {
        separar();
        escribirTexto(nombre);
        sb.append(':');
        return this;
    }

    private Json valor(String texto) {
        if (texto == null) {
            sb.append("null");
        } else {
            escribirTexto(texto);
        }
        return this;
    }

    private void separar() {
        if (!primero) {
            sb.append(',');
        }
        primero = false;
    }

    private void escribirTexto(String texto) {
        sb.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u").append("0000", hex.length(), 4).append(hex);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
