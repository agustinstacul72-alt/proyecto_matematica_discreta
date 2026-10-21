package com.medset.web;

import com.medset.logica.CalculadoraConjuntos;
import com.medset.logica.FormatoConjunto;
import com.medset.logica.OperacionesConjuntos;
import com.medset.logica.Operacion;
import com.medset.logica.ResultadoOperacion;
import com.medset.logica.ZonaVenn;
import com.medset.modelo.CatalogoClinico;
import com.medset.modelo.Enfermedad;
import com.medset.modelo.EvaluacionTriage;
import com.medset.modelo.JuegoDetective;
import com.medset.modelo.MotorTriage;
import com.medset.modelo.NivelUrgencia;
import com.medset.modelo.ResultadoAnalisis;
import com.medset.modelo.TextosJuego;
import com.medset.modelo.TextosTriage;
import com.medset.modelo.TipoPaciente;
import org.teavm.jso.JSExport;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Puente entre la página web y la misma lógica de la app de escritorio.
 * Cada función exportada devuelve un texto JSON con todo lo que la pantalla
 * muestra, ya calculado y con los textos armados en Java.
 */
public final class MedSetWeb {

    /** Separa los elementos de una lista que llega desde la página. */
    private static final String SEPARADOR = "\n";

    private static final MotorTriage MOTOR = new MotorTriage();

    private static final JuegoDetective JUEGO = new JuegoDetective();
    private static final List<String> REGISTRO = new ArrayList<>();
    private static final List<String> SI = new ArrayList<>();
    private static final List<String> NO = new ArrayList<>();
    private static String mensaje = "";
    private static String estiloMensaje = "";
    private static int puntajeTotal = 0;
    private static int rondas = 0;

    private MedSetWeb() {
    }

    public static void main(String[] args) {
    }

    // ---------------------------------------------------------------- Calculadora

    @JSExport
    public static String operaciones() {
        Json lista = Json.lista();
        for (Operacion op : Operacion.values()) {
            lista.elemento(Json.objeto()
                    .campo("id", op.name())
                    .campo("simbolo", op.simbolo())
                    .campo("definicion", op.definicion())
                    .campo("usaUniverso", op.usaUniverso())
                    .campo("ejemploA", op.ejemploA())
                    .campo("ejemploSegundo", op.ejemploSegundo())
                    .cerrar());
        }
        return lista.cerrar();
    }

    @JSExport
    public static String calcular(String operacion, String textoA, String textoSegundo) {
        ResultadoOperacion r = CalculadoraConjuntos.calcular(Operacion.valueOf(operacion), textoA, textoSegundo);
        Json json = Json.objeto().campo("resultado", r.resultado()).campo("detalle", r.detalle());
        ResultadoOperacion.Diagrama d = r.diagrama();
        if (d == null) {
            json.crudo("diagrama", "null");
        } else if (d.esComplemento()) {
            json.crudo("diagrama", Json.objeto()
                    .campo("complemento", true)
                    .campo("dentro", ordenar(d.a()))
                    .campo("fuera", ordenar(OperacionesConjuntos.calcularDiferencia(d.universo(), d.a())))
                    .cerrar());
        } else {
            json.crudo("diagrama", diagramaDos(d.a(), d.b(), d.resaltar(), d.alerta()));
        }
        return json.cerrar();
    }

    // ---------------------------------------------------------------- Triage

    @JSExport
    public static String catalogo() {
        Json edades = Json.lista();
        for (TipoPaciente t : TipoPaciente.gruposDeEdad()) {
            edades.elemento(Json.objeto().campo("id", t.name()).campo("texto", TextosTriage.botonEdad(t)).cerrar());
        }
        Json condiciones = Json.lista();
        for (TipoPaciente t : TipoPaciente.condiciones()) {
            condiciones.elemento(Json.objeto().campo("id", t.name()).campo("texto", t.nombre()).cerrar());
        }
        Json ejemplos = Json.lista();
        for (Set<String> ejemplo : TextosTriage.PACIENTES_DE_EJEMPLO) {
            ejemplos.elemento(Json.textos(new ArrayList<>(ejemplo)));
        }
        List<String> enfermedades = new ArrayList<>();
        for (Enfermedad e : CatalogoClinico.ENFERMEDADES) {
            enfermedades.add(e.nombre());
        }
        return Json.objeto()
                .campo("universo", new ArrayList<>(CatalogoClinico.UNIVERSO))
                .campo("alarma", new ArrayList<>(CatalogoClinico.SINTOMAS_DE_ALARMA))
                .crudo("edades", edades.cerrar())
                .crudo("condiciones", condiciones.cerrar())
                .crudo("ejemplos", ejemplos.cerrar())
                .campo("enfermedades", enfermedades)
                .campo("sinDatos", TextosTriage.SIN_DATOS)
                .campo("sinDatosIndicacion", TextosTriage.SIN_DATOS_INDICACION)
                .cerrar();
    }

    /**
     * @param sintomas síntomas marcados, separados por saltos de línea
     * @param perfil   grupo de edad y condiciones (nombres de {@link TipoPaciente}), separados por saltos de línea
     */
    @JSExport
    public static String evaluarTriage(String sintomas, String perfil) {
        Set<String> p = new LinkedHashSet<>(partir(sintomas));
        Set<TipoPaciente> tipos = EnumSet.noneOf(TipoPaciente.class);
        for (String t : partir(perfil)) {
            tipos.add(TipoPaciente.valueOf(t));
        }
        EvaluacionTriage e = MOTOR.evaluar(p, tipos);
        NivelUrgencia nivel = e.nivel();

        Json ranking = Json.lista();
        for (ResultadoAnalisis r : e.ranking()) {
            Json etiquetas = Json.lista();
            for (TextosTriage.Etiqueta et : TextosTriage.etiquetas(r)) {
                etiquetas.elemento(Json.objeto().campo("texto", et.texto()).campo("ok", et.ok()).cerrar());
            }
            ranking.elemento(Json.objeto()
                    .campo("nombre", r.enfermedad().nombre())
                    .campo("similitud", r.similitud())
                    .campo("porcentaje", TextosTriage.porcentaje(r))
                    .campo("detalle", TextosTriage.detalle(r))
                    .campo("atenuada", TextosTriage.atenuada(r))
                    .crudo("etiquetas", etiquetas.cerrar())
                    .campo("tituloVenn", TextosTriage.tituloVenn(r.enfermedad()))
                    .crudo("venn", diagramaDos(p, r.enfermedad().sintomas(), EnumSet.of(ZonaVenn.AMBOS), false))
                    .cerrar());
        }
        return Json.objeto()
                .campo("riesgos", TextosTriage.riesgos(e))
                .campo("paciente", TextosTriage.paciente(p))
                .campo("complemento", TextosTriage.complemento(p))
                .campo("alarmas", TextosTriage.alarmas(e))
                .campo("vacio", p.isEmpty())
                .campo("urgencia", TextosTriage.urgencia(nivel))
                .campo("estiloUrgencia", TextosTriage.estiloUrgencia(nivel))
                .campo("indicacion", nivel.indicacion())
                .campo("regla", e.regla())
                .crudo("ranking", ranking.cerrar())
                .cerrar();
    }

    // ---------------------------------------------------------------- Juego

    /** Al entrar a la pantalla del juego el puntaje total vuelve a cero, como en la app de escritorio. */
    @JSExport
    public static String juegoIniciar() {
        puntajeTotal = 0;
        rondas = 0;
        return juegoNuevaRonda();
    }

    @JSExport
    public static String juegoNuevaRonda() {
        JUEGO.nuevaPartida();
        REGISTRO.clear();
        SI.clear();
        NO.clear();
        registrar(TextosJuego.registroInicio(JUEGO));
        mensaje(TextosJuego.MENSAJE_INICIAL, "");
        return estadoJuego();
    }

    @JSExport
    public static String juegoPreguntar(String sintoma) {
        if (!JUEGO.terminado() && !SI.contains(sintoma) && !NO.contains(sintoma)) {
            int antes = JUEGO.candidatos().size();
            boolean si = JUEGO.preguntar(sintoma);
            (si ? SI : NO).add(sintoma);
            List<Enfermedad> candidatos = JUEGO.candidatos();
            registrar(TextosJuego.registroPregunta(sintoma, si, antes, candidatos));
            mensaje = TextosJuego.respuestaPregunta(sintoma, si, candidatos);
        }
        return estadoJuego();
    }

    @JSExport
    public static String juegoAdivinar(String nombre) {
        Enfermedad e = buscar(nombre);
        if (!JUEGO.terminado() && e != null) {
            if (JUEGO.adivinar(e)) {
                puntajeTotal += JUEGO.puntos();
                rondas++;
                registrar(TextosJuego.registroAcierto(e));
                mensaje(TextosJuego.mensajeAcierto(e, JUEGO.puntos()), "game-win");
            } else {
                registrar(TextosJuego.registroError(e));
                if (JUEGO.terminado()) {
                    terminarPerdiendo(TextosJuego.SIN_PUNTOS);
                } else {
                    mensaje = TextosJuego.mensajeError(e);
                }
            }
        }
        return estadoJuego();
    }

    @JSExport
    public static String juegoPista() {
        if (!JUEGO.terminado()) {
            mensaje = TextosJuego.pista(JUEGO);
        }
        return estadoJuego();
    }

    @JSExport
    public static String juegoRendirse() {
        if (!JUEGO.terminado()) {
            JUEGO.rendirse();
            rondas++;
            terminarPerdiendo(TextosJuego.RENDICION);
        }
        return estadoJuego();
    }

    private static void terminarPerdiendo(String motivo) {
        Enfermedad x = JUEGO.secreta();
        mensaje(TextosJuego.mensajeDerrota(motivo, x), "game-lose");
        registrar(TextosJuego.registroRespuesta(x));
    }

    private static String estadoJuego() {
        boolean terminado = JUEGO.terminado();
        Json preguntas = Json.lista();
        for (String s : CatalogoClinico.UNIVERSO) {
            String estado = SI.contains(s) ? "si" : NO.contains(s) ? "no" : "libre";
            String texto = estado.equals("libre") ? s : TextosJuego.botonPreguntado(s, estado.equals("si"));
            preguntas.elemento(Json.objeto().campo("sintoma", s).campo("texto", texto).campo("estado", estado).cerrar());
        }
        List<Enfermedad> vivos = JUEGO.candidatos();
        Json candidatos = Json.lista();
        for (Enfermedad e : JUEGO.catalogo()) {
            boolean revelada = terminado && e.equals(JUEGO.secreta());
            candidatos.elemento(Json.objeto()
                    .campo("nombre", e.nombre())
                    .campo("vivo", vivos.contains(e))
                    .campo("revelada", revelada)
                    .campo("sintomas", revelada ? FormatoConjunto.formatear(e.sintomas()) : "")
                    .cerrar());
        }
        return Json.objeto()
                .campo("puntos", TextosJuego.puntos(JUEGO))
                .campo("total", TextosJuego.total(puntajeTotal, rondas))
                .campo("titulo", TextosJuego.tituloCandidatos(JUEGO))
                .campo("terminado", terminado)
                .campo("mensaje", mensaje)
                .campo("estiloMensaje", estiloMensaje)
                .campo("registro", REGISTRO)
                .crudo("preguntas", preguntas.cerrar())
                .crudo("candidatos", candidatos.cerrar())
                .cerrar();
    }

    private static void registrar(String linea) {
        REGISTRO.add(TextosJuego.lineaRegistro(REGISTRO.size() + 1, linea));
    }

    private static void mensaje(String texto, String estilo) {
        mensaje = texto;
        estiloMensaje = estilo;
    }

    private static Enfermedad buscar(String nombre) {
        for (Enfermedad e : JUEGO.catalogo()) {
            if (e.nombre().equals(nombre)) {
                return e;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- Comunes

    /** Zonas de un diagrama de dos conjuntos, con sus elementos ya ordenados. */
    private static String diagramaDos(Set<String> a, Set<String> b, Set<ZonaVenn> resaltar, boolean alerta) {
        List<String> zonas = new ArrayList<>();
        for (ZonaVenn z : resaltar) {
            zonas.add(z.name());
        }
        return Json.objeto()
                .campo("complemento", false)
                .campo("soloA", ordenar(OperacionesConjuntos.calcularDiferencia(a, b)))
                .campo("ambos", ordenar(OperacionesConjuntos.calcularInterseccion(a, b)))
                .campo("soloB", ordenar(OperacionesConjuntos.calcularDiferencia(b, a)))
                .campo("resaltar", zonas)
                .campo("alerta", alerta)
                .cerrar();
    }

    private static List<String> ordenar(Set<String> conjunto) {
        return FormatoConjunto.ordenar(conjunto);
    }

    private static List<String> partir(String texto) {
        List<String> partes = new ArrayList<>();
        if (texto == null || texto.isEmpty()) {
            return partes;
        }
        for (String parte : texto.split(SEPARADOR)) {
            if (!parte.isEmpty()) {
                partes.add(parte);
            }
        }
        return partes;
    }
}
