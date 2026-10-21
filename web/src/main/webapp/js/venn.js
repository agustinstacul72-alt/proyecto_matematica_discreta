/*
 * Diagrama de Venn de MedSet Web. Usa la misma geometría y el mismo ajuste de
 * texto que VennDiagrama.java: los conjuntos son óvalos que se estiran hacia
 * abajo cuando tienen muchos elementos, y el texto nunca sale de su zona.
 */
(function () {
    "use strict";

    const ANCHO = 560;
    const MARGEN = 8;
    const ESPACIO_ARRIBA = 40;
    const ESPACIO_ABAJO = 28;
    const RADIO_X = 150;
    const RADIO_X_COMPLEMENTO = 120;
    const RADIO_Y = 120;
    const CX_A = 200;
    const CX_B = 360;
    const CX_CENTRO = ANCHO / 2;
    const ESTIRAMIENTO_MAXIMO = 1.8;

    // Rectángulos que entran completos en cada zona cuando el óvalo no está estirado.
    const SOLO = { ancho: 132, alto: 120 };
    const AMBOS = { ancho: 96, alto: 120 };
    const DENTRO = { ancho: 170, alto: 160 };
    const FUERA = { ancho: 136, alto: 220 };

    const TAMANO_LETRA = 13;
    const TAMANO_VACIO = 22;
    const TAMANOS_DE_RESPALDO = [12, 11, 10];

    const RESALTADO = "rgba(37, 99, 235, 0.30)";
    const RESALTADO_ROJO = "rgba(220, 38, 38, 0.28)";
    const BORDE = "#334155";
    const BORDE_UNIVERSO = "#CBD5E1";

    const SVG = "http://www.w3.org/2000/svg";
    let contador = 0;

    function svg(nombre, atributos) {
        const nodo = document.createElementNS(SVG, nombre);
        for (const [clave, valor] of Object.entries(atributos || {})) {
            nodo.setAttribute(clave, valor);
        }
        return nodo;
    }

    function lineas(elementos) {
        return elementos.length === 0 ? ["∅"] : elementos;
    }

    /** Caja de texto con los elementos, uno por línea, centrados. */
    function cajaDeTexto(lista, tamano, ancho) {
        const div = document.createElement("div");
        div.className = "venn-texto";
        div.style.width = ancho + "px";
        div.style.fontSize = tamano + "px";
        for (const elemento of lista) {
            const linea = document.createElement("div");
            linea.textContent = elemento;
            div.appendChild(linea);
        }
        return div;
    }

    function medir(medidor, lista, tamano, ancho) {
        const div = cajaDeTexto(lista, tamano, ancho);
        medidor.appendChild(div);
        const medida = { ancho: div.scrollWidth, alto: div.offsetHeight };
        medidor.removeChild(div);
        return medida;
    }

    function cabe(medida, ancho, alto) {
        return medida.ancho <= ancho + 0.5 && medida.alto <= alto;
    }

    function recortar(lista, visibles) {
        return lista.slice(0, visibles).concat("(+" + (lista.length - visibles) + " más)");
    }

    /** Cuánto estirar los óvalos para que cada zona entre con la letra normal. */
    function estiramiento(medidor, zonas) {
        let necesario = 1;
        for (const [elementos, caja] of zonas) {
            const alto = medir(medidor, lineas(elementos), TAMANO_LETRA, caja.ancho).alto;
            necesario = Math.max(necesario, alto / caja.alto);
        }
        return Math.min(necesario, ESTIRAMIENTO_MAXIMO);
    }

    /** Elige letra y cantidad de elementos para que el texto entre en la caja. */
    function textoQueCabe(medidor, elementos, caja, altoDisponible) {
        const lista = lineas(elementos);
        const normal = elementos.length === 0 ? TAMANO_VACIO : TAMANO_LETRA;
        if (cabe(medir(medidor, lista, normal, caja.ancho), caja.ancho, altoDisponible)) {
            return { lista, tamano: normal };
        }
        for (const tamano of TAMANOS_DE_RESPALDO) {
            if (cabe(medir(medidor, lista, tamano, caja.ancho), caja.ancho, altoDisponible)) {
                return { lista, tamano };
            }
        }
        const menor = TAMANOS_DE_RESPALDO[TAMANOS_DE_RESPALDO.length - 1];
        let recortada = lista;
        for (let visibles = lista.length - 1; visibles >= 1; visibles--) {
            recortada = recortar(lista, visibles);
            if (cabe(medir(medidor, recortada, menor, caja.ancho), caja.ancho, altoDisponible)) {
                break;
            }
        }
        return { lista: recortada, tamano: menor };
    }

    class Venn {
        /** @param marco elemento donde se dibuja; el diagrama se achica si el marco es angosto */
        constructor(marco) {
            this.marco = marco;
            this.marco.classList.add("venn-marco");
            this.lienzo = document.createElement("div");
            this.lienzo.className = "venn";
            this.medidor = document.createElement("div");
            this.medidor.className = "venn-medidor";
            this.marco.append(this.lienzo, this.medidor);
            this.alto = 0;
            this.limpiar();
            new ResizeObserver(() => this.escalar()).observe(this.marco);
        }

        limpiar() {
            this.lienzo.replaceChildren();
            this.lienzo.style.height = "0px";
            this.alto = 0;
            this.marco.style.height = "0px";
        }

        /**
         * Dos conjuntos A y B.
         * @param d { soloA, ambos, soloB, resaltar: ["SOLO_A"|"AMBOS"|"SOLO_B"], alerta }
         */
        dibujar(d, nombreA, nombreB) {
            const k = estiramiento(this.medidor, [[d.soloA, SOLO], [d.ambos, AMBOS], [d.soloB, SOLO]]);
            const { dibujo, cy, ry } = this.preparar(k);
            const id = "venn" + (++contador);
            const a = { cx: CX_A, cy, rx: RADIO_X, ry };
            const b = { cx: CX_B, cy, rx: RADIO_X, ry };

            const defs = svg("defs");
            defs.append(
                recorte(id + "-a", a), recorte(id + "-b", b),
                mascaraSin(id + "-sin-b", b), mascaraSin(id + "-sin-a", a));
            dibujo.append(defs);

            const color = d.alerta ? RESALTADO_ROJO : RESALTADO;
            const resaltar = new Set(d.resaltar);
            if (resaltar.has("SOLO_A")) {
                dibujo.append(ovalo(a, { fill: color, mask: `url(#${id}-sin-b)` }));
            }
            if (resaltar.has("AMBOS")) {
                dibujo.append(ovalo(a, { fill: color, "clip-path": `url(#${id}-b)` }));
            }
            if (resaltar.has("SOLO_B")) {
                dibujo.append(ovalo(b, { fill: color, mask: `url(#${id}-sin-a)` }));
            }
            dibujo.append(contorno(a), contorno(b));

            this.nombre(nombreA, CX_A - 90, 20);
            this.nombre(nombreB, CX_B + 90, 20);
            this.nombre("U", ANCHO - 26, 18, 32);

            this.elementos(d.soloA, CX_A - 62, SOLO, k, cy);
            this.elementos(d.ambos, (CX_A + CX_B) / 2, AMBOS, k, cy);
            this.elementos(d.soloB, CX_B + 62, SOLO, k, cy);
        }

        /** Complemento: A dentro del universo U, con A' resaltado. @param d { dentro, fuera } */
        dibujarComplemento(d, nombreA) {
            const k = estiramiento(this.medidor, [[d.dentro, DENTRO], [d.fuera, FUERA]]);
            const { dibujo, cy, ry } = this.preparar(k);
            const id = "venn" + (++contador);
            const a = { cx: CX_CENTRO, cy, rx: RADIO_X_COMPLEMENTO, ry };

            const defs = svg("defs");
            defs.append(mascaraSin(id + "-sin-a", a));
            dibujo.append(defs);
            dibujo.append(svg("rect", {
                x: MARGEN, y: MARGEN, width: ANCHO - 2 * MARGEN, height: this.alto - 2 * MARGEN,
                rx: 9, fill: RESALTADO, mask: `url(#${id}-sin-a)`
            }));
            dibujo.append(contorno(a));

            this.nombre(nombreA, CX_CENTRO, 20);
            this.nombre("U", ANCHO - 26, 18, 32);

            this.elementos(d.dentro, CX_CENTRO, DENTRO, k, cy);
            // Los elementos de U − A se escriben a la izquierda, fuera del óvalo.
            this.elementos(d.fuera, MARGEN + 76, FUERA, k, cy);
        }

        preparar(k) {
            this.lienzo.replaceChildren();
            const ry = RADIO_Y * k;
            this.alto = ESPACIO_ARRIBA + 2 * ry + ESPACIO_ABAJO;
            this.lienzo.style.height = this.alto + "px";
            const dibujo = svg("svg", { width: ANCHO, height: this.alto, viewBox: `0 0 ${ANCHO} ${this.alto}` });
            dibujo.append(svg("rect", {
                x: MARGEN, y: MARGEN, width: ANCHO - 2 * MARGEN, height: this.alto - 2 * MARGEN,
                rx: 9, fill: "white", stroke: BORDE_UNIVERSO, "stroke-width": 1.5
            }));
            this.lienzo.append(dibujo);
            this.escalar();
            return { dibujo, cy: ESPACIO_ARRIBA + ry, ry };
        }

        nombre(texto, x, tamano, y = ESPACIO_ARRIBA - 6) {
            const div = document.createElement("div");
            div.className = "venn-nombre";
            div.textContent = texto;
            div.style.fontSize = tamano + "px";
            div.style.left = x + "px";
            div.style.top = (y - tamano * 1.05) + "px";
            this.lienzo.append(div);
        }

        elementos(elementos, cx, caja, k, cy) {
            const alto = caja.alto * k;
            const { lista, tamano } = textoQueCabe(this.medidor, elementos, caja, alto);
            const div = cajaDeTexto(lista, tamano, caja.ancho);
            div.style.left = (cx - caja.ancho / 2) + "px";
            this.lienzo.append(div);
            div.style.top = (cy - div.offsetHeight / 2) + "px";
        }

        /** En pantallas angostas (celular) el diagrama se achica para no salirse. */
        escalar() {
            const escala = Math.min(1, this.marco.clientWidth / ANCHO || 1);
            this.lienzo.style.transform = escala < 1 ? `scale(${escala})` : "";
            this.marco.style.height = (this.alto * escala) + "px";
        }
    }

    function ovalo(o, atributos) {
        return svg("ellipse", Object.assign({ cx: o.cx, cy: o.cy, rx: o.rx, ry: o.ry }, atributos));
    }

    function contorno(o) {
        return ovalo(o, { fill: "none", stroke: BORDE, "stroke-width": 2 });
    }

    function recorte(id, o) {
        const clip = svg("clipPath", { id });
        clip.append(ovalo(o, {}));
        return clip;
    }

    /** Máscara que deja ver todo salvo el óvalo (para A − B, B − A y U − A). */
    function mascaraSin(id, o) {
        const mascara = svg("mask", { id, maskUnits: "userSpaceOnUse", x: 0, y: 0, width: ANCHO, height: 2000 });
        mascara.append(svg("rect", { x: 0, y: 0, width: ANCHO, height: 2000, fill: "white" }), ovalo(o, { fill: "black" }));
        return mascara;
    }

    window.Venn = Venn;
})();
