/*
 * Pantallas de MedSet Web. Todos los cálculos y textos vienen del código Java
 * de la app de escritorio (compilado a js/medset.js); aquí solo se dibujan.
 */
(function () {
    "use strict";

    // Funciones exportadas por MedSetWeb.java. Devuelven texto JSON.
    const java = {
        operaciones: () => JSON.parse(window.operaciones()),
        calcular: (op, a, segundo) => JSON.parse(window.calcular(op, a, segundo)),
        catalogo: () => JSON.parse(window.catalogo()),
        evaluarTriage: (sintomas, perfil) => JSON.parse(window.evaluarTriage(sintomas.join("\n"), perfil.join("\n"))),
        juegoIniciar: () => JSON.parse(window.juegoIniciar()),
        juegoNuevaRonda: () => JSON.parse(window.juegoNuevaRonda()),
        juegoPreguntar: s => JSON.parse(window.juegoPreguntar(s)),
        juegoAdivinar: n => JSON.parse(window.juegoAdivinar(n)),
        juegoPista: () => JSON.parse(window.juegoPista()),
        juegoRendirse: () => JSON.parse(window.juegoRendirse()),
    };
    window.main([]);

    const app = document.getElementById("app");

    /** Crea un elemento: h("div.clase", {atributos}, hijos...). */
    function h(etiqueta, atributos, ...hijos) {
        const [nombre, ...clases] = etiqueta.split(".");
        const nodo = document.createElement(nombre);
        if (clases.length) {
            nodo.className = clases.join(" ");
        }
        for (const [clave, valor] of Object.entries(atributos || {})) {
            if (clave.startsWith("on")) {
                nodo.addEventListener(clave.slice(2), valor);
            } else if (clave === "texto") {
                nodo.textContent = valor;
            } else if (valor === true) {
                nodo.setAttribute(clave, "");
            } else if (valor !== false && valor != null) {
                nodo.setAttribute(clave, valor);
            }
        }
        nodo.append(...hijos.flat().filter(x => x != null));
        return nodo;
    }

    function barraSuperior(titulo, ...derecha) {
        return h("header.top-bar", {},
            h("button.secondary-button", { type: "button", texto: "← Inicio", onclick: () => ir("inicio") }),
            h("h1.top-bar-title", { texto: titulo }),
            h("span.top-bar-space"),
            ...derecha);
    }

    // ===================================================================== Inicio

    function inicio() {
        const modulo = (destino, titulo, simbolos, desc) =>
            h("button.module-card", { type: "button", onclick: () => ir(destino) },
                h("span.module-title", { texto: titulo }),
                h("span.module-symbols", { texto: simbolos }),
                h("span.module-desc", { texto: desc }));
        return h("section.welcome", {},
            h("div.welcome-logos", {},
                h("img", { src: "img/medset-logo.png", alt: "Logo de MedSet" }),
                h("img", { src: "img/jala-logo.png", alt: "Jala University" })),
            h("h1.welcome-title", { texto: "MedSet" }),
            h("p.welcome-tagline", { texto: "Teoría de conjuntos aplicada al triage de síntomas" }),
            h("p.welcome-subtitle", {
                texto: "Un paciente es un conjunto P de síntomas, una enfermedad es un conjunto E y todo vive dentro del universo U."
            }),
            h("div.modules", {},
                modulo("calculadora", "1 · Calculadora", "∪  ∩  −  Δ  ⊆  A'",
                    "Operaciones con definición formal, cardinalidad y diagrama de Venn."),
                modulo("triage", "2 · Triage", "P ∩ E   J(P, E)",
                    "Marcá síntomas y el sistema ordena enfermedades y asigna la urgencia."),
                modulo("juego", "3 · Juego detective", "¿s ∈ X?",
                    "Descubrí la enfermedad secreta con la menor cantidad de preguntas.")),
            h("button.secondary-button", { type: "button", texto: "Integrantes", onclick: () => ir("integrantes") }),
            h("p.welcome-disclaimer", {
                texto: "Proyecto educativo de Matemáticas Discretas · Grupo 6 · No reemplaza una consulta médica."
            }));
    }

    // ===================================================================== Integrantes

    const ALUMNOS = ["Daniel Tueros Cueva", "Brisa Abigail Alvarez", "Stacul Dario Agustin",
        "Dayana Lucia Posada Narváez", "Agustin Tomas Gammuto", "Jose David Carranza Angarita"];

    function integrantes() {
        return h("section.credits", {},
            h("img.credits-logo", { src: "img/jala-logo.png", alt: "Jala University" }),
            h("div.credits-course", { texto: "Matemáticas Discretas" }),
            h("div.credits-group", {}, "Grupo", h("b", { texto: "6" })),
            h("div.credits-section", {},
                h("div.credits-heading", { texto: "PROFESOR" }),
                h("div.credits-professor", { texto: "Rafael Pastor Martínez Vargas" })),
            h("div.credits-section", {},
                h("div.credits-heading", { texto: "ALUMNOS" }),
                ALUMNOS.map(nombre => h("div.credits-student", { texto: nombre }))),
            h("button.secondary-button", { type: "button", texto: "Volver", onclick: () => ir("inicio") }));
    }

    // ===================================================================== Calculadora

    const NOMBRES_OPERACION = {
        UNION: "Unión", INTERSECCION: "Intersección", DIFERENCIA: "Diferencia",
        SIMETRICA: "Dif. simétrica", SUBCONJUNTO: "Subconjunto", COMPLEMENTO: "Complemento",
    };
    const SIMBOLOS_TARJETA = { UNION: "∪", INTERSECCION: "∩", DIFERENCIA: "−", SIMETRICA: "Δ", SUBCONJUNTO: "⊆", COMPLEMENTO: "A'" };

    function calculadora() {
        const operaciones = java.operaciones();
        let actual = operaciones[0];

        const titulo = h("h2.section-title");
        const definicion = h("p.definition-text");
        const campoA = h("input.form-input", { placeholder: "Ej: 1, 2, 3, 4", "aria-label": "Conjunto A" });
        const campoB = h("input.form-input", { placeholder: "Ej: 3, 4, 5", "aria-label": "Conjunto B" });
        const campoU = h("input.form-input", { placeholder: "Ej: 1, 2, 3, 4, 5, 6, 7, 8", "aria-label": "Universo U" });
        const grupoB = h("div.stack.gap-10", {}, h("label.field-label", { texto: "Conjunto B" }), campoB);
        const grupoU = h("div.stack.gap-10", {}, h("label.field-label", { texto: "Universo U" }), campoU);
        const resultado = h("p.result-text");
        const detalle = h("p.detail-text");
        const marcoVenn = h("div");

        const segundo = () => (actual.usaUniverso ? campoU : campoB);
        const enter = e => { if (e.key === "Enter") { calcular(); } };
        campoA.addEventListener("keydown", enter);
        campoB.addEventListener("keydown", enter);
        campoU.addEventListener("keydown", enter);

        const selector = h("div.selector", {},
            h("h2.panel-title", { texto: "Elegí una operación" }),
            h("p.panel-description", { texto: "Cada operación muestra su definición formal, su cardinalidad y su diagrama de Venn." }),
            h("div.ops", {}, operaciones.map(op =>
                h("button.op-card", { type: "button", onclick: () => seleccionar(op) },
                    h("span.op-symbol", { texto: SIMBOLOS_TARJETA[op.id] }),
                    h("span.op-name", { texto: NOMBRES_OPERACION[op.id] })))));

        const formulario = h("div.calc.oculto", {},
            h("div.calc-form", {},
                h("div.row", {},
                    h("button.small-button", { type: "button", texto: "← Operaciones", onclick: volver }),
                    h("button.small-button", { type: "button", texto: "Cargar ejemplo", onclick: cargarEjemplo })),
                titulo, definicion,
                h("label.field-label", { texto: "Conjunto A" }), campoA,
                grupoB, grupoU,
                h("button.primary-button", { type: "button", texto: "Calcular", onclick: calcular }),
                resultado, detalle),
            h("div.calc-venn", {},
                h("div.field-label", { texto: "Diagrama de Venn" }),
                marcoVenn,
                h("p.panel-description", { texto: "La zona azul es el resultado. En ⊆, la zona roja muestra los contraejemplos." })));

        const vista = h("div", {},
            barraSuperior("Módulo 1 · Calculadora de conjuntos",
                h("span.top-bar-status", { texto: "Funciona con números o palabras: 1, 2, 3  ó  fiebre, tos" })),
            h("div.content", {}, selector, formulario));
        const venn = new Venn(marcoVenn);

        function seleccionar(op) {
            actual = op;
            titulo.textContent = op.simbolo;
            definicion.textContent = op.definicion;
            limpiarResultado();
            grupoB.classList.toggle("oculto", op.usaUniverso);
            grupoU.classList.toggle("oculto", !op.usaUniverso);
            selector.classList.add("oculto");
            formulario.classList.remove("oculto");
            cargarEjemplo();
        }

        function volver() {
            campoA.value = campoB.value = campoU.value = "";
            limpiarResultado();
            selector.classList.remove("oculto");
            formulario.classList.add("oculto");
        }

        function cargarEjemplo() {
            campoA.value = actual.ejemploA;
            segundo().value = actual.ejemploSegundo;
            calcular();
        }

        function calcular() {
            const r = java.calcular(actual.id, campoA.value, segundo().value);
            resultado.textContent = r.resultado;
            detalle.textContent = r.detalle;
            const d = r.diagrama;
            if (!d) {
                return;
            }
            if (d.complemento) {
                venn.dibujarComplemento(d, "A");
            } else {
                venn.dibujar(d, "A", "B");
            }
        }

        function limpiarResultado() {
            resultado.textContent = "";
            detalle.textContent = "";
            venn.limpiar();
        }

        return vista;
    }

    // ===================================================================== Triage

    function triage() {
        const catalogo = java.catalogo();
        const alarma = new Set(catalogo.alarma);
        const marcados = new Set();
        const condiciones = new Set();
        let edad = "ADULTO";
        let enfoque = null;
        let ejemplo = 0;

        const tipoTexto = h("p.detail-text");
        const pTexto = h("p.set-text");
        const complementoTexto = h("p.detail-text");
        const alarmaTexto = h("p.detail-text");
        const insignia = h("span.badge");
        const indicacion = h("p.detail-text");
        const regla = h("p.rule-text");
        const tituloVenn = h("div.field-label");
        const marcoVenn = h("div");
        const ranking = h("div.stack.gap-10");

        const chipsEdad = catalogo.edades.map(t =>
            h("button.chip", { type: "button", texto: t.texto, "aria-pressed": "false", onclick: () => { edad = t.id; actualizar(); } }));
        const chipsCondicion = catalogo.condiciones.map(t =>
            h("button.chip", {
                type: "button", texto: t.texto, "aria-pressed": "false",
                onclick: () => { condiciones.has(t.id) ? condiciones.delete(t.id) : condiciones.add(t.id); actualizar(); }
            }));
        const chipsSintoma = catalogo.universo.map(s =>
            h("button.chip" + (alarma.has(s) ? ".chip-alarma" : ""), {
                type: "button", texto: s, "aria-pressed": "false",
                onclick: () => { marcados.has(s) ? marcados.delete(s) : marcados.add(s); enfoque = null; actualizar(); }
            }));

        const vista = h("div", {},
            barraSuperior("Módulo 2 · Triage con conjuntos",
                h("span.top-bar-status", { texto: "Herramienta educativa: no reemplaza una consulta médica." })),
            h("div.split", {},
                h("aside.lab-left.stack.gap-12", {},
                    h("h2.section-title", { texto: "Tipo de paciente" }),
                    h("p.panel-description", {
                        texto: "Cada tipo tiene su conjunto de síntomas de riesgo. G es la unión de los del grupo de edad y las condiciones. Si P ∩ G ≠ ∅, la urgencia sube un nivel."
                    }),
                    h("div.field-label", { texto: "Grupo de edad" }),
                    h("div.chips", {}, chipsEdad),
                    h("div.field-label", { texto: "Condiciones (opcional, se pueden combinar)" }),
                    h("div.chips", {}, chipsCondicion),
                    tipoTexto,
                    h("h2.section-title", { texto: "Síntomas del paciente (P)" }),
                    h("p.panel-description", { texto: "Tocá los síntomas que presenta el paciente. Cada uno es un elemento del universo U." }),
                    h("div.chips", {}, chipsSintoma),
                    h("div.row", {},
                        h("button.small-button", { type: "button", texto: "Paciente de ejemplo", onclick: pacienteEjemplo }),
                        h("button.small-button", { type: "button", texto: "Limpiar", onclick: () => cargar([]) })),
                    pTexto, complementoTexto, alarmaTexto),
                h("div.content.stack.gap-14", {},
                    h("div.row", {}, insignia, indicacion),
                    regla,
                    tituloVenn,
                    marcoVenn,
                    h("div.field-label", { texto: "Ranking de enfermedades (de mayor a menor similitud de Jaccard)" }),
                    h("p.panel-description", { texto: "J(P, E) = |P ∩ E| / |P ∪ E|. Tocá una tarjeta para verla en el diagrama de Venn." }),
                    ranking)));
        const venn = new Venn(marcoVenn);

        function pacienteEjemplo() {
            cargar(catalogo.ejemplos[ejemplo]);
            ejemplo = (ejemplo + 1) % catalogo.ejemplos.length;
        }

        function cargar(sintomas) {
            marcados.clear();
            sintomas.forEach(s => marcados.add(s));
            enfoque = null;
            actualizar();
        }

        function marcar(chip, activo) {
            chip.classList.toggle("seleccionado", activo);
            chip.setAttribute("aria-pressed", String(activo));
        }

        function actualizar() {
            catalogo.edades.forEach((t, i) => marcar(chipsEdad[i], t.id === edad));
            catalogo.condiciones.forEach((t, i) => marcar(chipsCondicion[i], condiciones.has(t.id)));
            catalogo.universo.forEach((s, i) => marcar(chipsSintoma[i], marcados.has(s)));

            // P en el orden del universo, como en la app de escritorio.
            const p = catalogo.universo.filter(s => marcados.has(s));
            const e = java.evaluarTriage(p, [edad, ...catalogo.condiciones.map(t => t.id).filter(id => condiciones.has(id))]);

            tipoTexto.textContent = e.riesgos;
            pTexto.textContent = e.paciente;
            complementoTexto.textContent = e.complemento;
            alarmaTexto.textContent = e.alarmas;
            ranking.replaceChildren();

            if (e.vacio) {
                insignia.textContent = catalogo.sinDatos;
                insignia.className = "badge badge-neutral";
                indicacion.textContent = catalogo.sinDatosIndicacion;
                regla.textContent = "";
                regla.classList.add("oculto");
                tituloVenn.textContent = "";
                venn.limpiar();
                return;
            }

            insignia.textContent = e.urgencia;
            insignia.className = "badge " + e.estiloUrgencia;
            indicacion.textContent = e.indicacion;
            regla.textContent = e.regla;
            regla.classList.remove("oculto");

            if (enfoque === null) {
                enfoque = e.ranking[0].nombre;
            }
            for (const r of e.ranking) {
                ranking.append(tarjeta(r, r.nombre === enfoque));
            }
            const elegido = e.ranking.find(r => r.nombre === enfoque);
            tituloVenn.textContent = elegido.tituloVenn;
            venn.dibujar(elegido.venn, "P", "E");
        }

        function tarjeta(r, seleccionada) {
            const clases = "button.rank-card" + (seleccionada ? ".rank-card-selected" : "") + (r.atenuada ? ".rank-card-dim" : "");
            return h(clases, { type: "button", onclick: () => { enfoque = r.nombre; actualizar(); } },
                h("span.card-head", {},
                    h("span.card-title", { texto: r.nombre }),
                    h("span.jaccard-bar", { role: "progressbar", "aria-valuenow": Math.round(r.similitud * 100), "aria-valuemin": 0, "aria-valuemax": 100 },
                        h("span", { style: `width: ${r.similitud * 100}%` })),
                    h("span.card-pct", { texto: r.porcentaje })),
                h("span.card-detail", { texto: r.detalle }),
                h("span.tags", {}, r.etiquetas.map(t => h("span.tag." + (t.ok ? "tag-ok" : "tag-muted"), { texto: t.texto }))));
        }

        actualizar();
        return vista;
    }

    // ===================================================================== Juego

    function juego() {
        const puntos = h("span.score");
        const total = h("span.top-bar-status");
        const tituloCandidatos = h("h2.section-title");
        const candidatos = h("div.stack.gap-10");
        const preguntas = h("div.chips");
        const pista = h("button.small-button", { type: "button", texto: "💡 Pista (pregunta óptima)", onclick: () => mostrar(java.juegoPista()) });
        const rendirse = h("button.small-button", { type: "button", texto: "Rendirse", onclick: () => mostrar(java.juegoRendirse()) });
        const mensaje = h("p.game-message");
        const registro = h("div.log-box");

        const vista = h("div", {},
            barraSuperior("Módulo 3 · Juego: Detective de síntomas", puntos, total),
            h("div.split.split-juego", {},
                h("aside.lab-left.stack.gap-10", {},
                    tituloCandidatos,
                    h("p.panel-description", {
                        texto: "C empieza siendo todo el catálogo D. Cuando creas saberlo, tocá «¡Es esta!» (un error cuesta 25 puntos)."
                    }),
                    candidatos),
                h("div.content.stack.gap-14", {},
                    h("h2.section-title", { texto: "El sistema eligió una enfermedad secreta X. Descubrila preguntando por síntomas." }),
                    h("p.panel-description", {
                        texto: "Cada pregunta es una prueba de pertenencia «¿s ∈ X?» y cuesta 10 puntos. Elegí síntomas que dividan bien a los candidatos."
                    }),
                    preguntas,
                    h("div.row", {}, pista, rendirse,
                        h("button.primary-button", { type: "button", texto: "Nueva ronda", onclick: () => mostrar(java.juegoNuevaRonda()) })),
                    mensaje,
                    h("div.field-label", { texto: "Razonamiento con conjuntos" }),
                    registro)));

        function mostrar(estado) {
            puntos.textContent = estado.puntos;
            total.textContent = estado.total;
            tituloCandidatos.textContent = estado.titulo;
            pista.disabled = rendirse.disabled = estado.terminado;
            mensaje.textContent = estado.mensaje;
            mensaje.className = "game-message" + (estado.estiloMensaje ? " " + estado.estiloMensaje : "");

            preguntas.replaceChildren(...estado.preguntas.map(p =>
                h("button.chip" + (p.estado === "si" ? ".chip-si" : p.estado === "no" ? ".chip-no" : ""), {
                    type: "button", texto: p.texto,
                    disabled: p.estado !== "libre" || estado.terminado,
                    onclick: () => mostrar(java.juegoPreguntar(p.sintoma))
                })));

            candidatos.replaceChildren(...estado.candidatos.map(c =>
                h("div.rank-card" + (c.revelada ? ".rank-card-selected" : !c.vivo ? ".rank-card-out" : ""), {},
                    h("div.candidate-row", {},
                        h("span.card-title", { texto: c.nombre }),
                        c.vivo && !estado.terminado
                            ? h("button.small-button", { type: "button", texto: "¡Es esta!", onclick: () => mostrar(java.juegoAdivinar(c.nombre)) })
                            : null),
                    c.revelada ? h("p.card-detail", { texto: c.sintomas }) : null)));

            registro.replaceChildren(...estado.registro.map(linea => h("p.log-line", { texto: linea })));
        }

        mostrar(java.juegoIniciar());
        return vista;
    }

    // ===================================================================== Navegación

    const VISTAS = { inicio, calculadora, triage, juego, integrantes };

    function ir(nombre) {
        location.hash = nombre === "inicio" ? "" : nombre;
    }

    function mostrarVista() {
        const nombre = location.hash.slice(1) || "inicio";
        // Cada vez que se entra a una pantalla empieza de cero, como en la app de escritorio.
        app.replaceChildren((VISTAS[nombre] || inicio)());
        window.scrollTo(0, 0);
    }

    window.addEventListener("hashchange", mostrarVista);
    mostrarVista();
})();
