package com.softwells.fanops.config;

import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sirve {@code index.html} para las rutas de navegación del frontend, de forma que las resuelva su
 * router en lugar de acabar en un 404 del servidor.
 *
 * Hace falta porque el frontend dejó de enrutar por hash: antes toda la navegación vivía detrás
 * del {@code #} y el servidor solo veía {@code /}, mientras que ahora
 * {@code /mi-pena/auth/login} llega como una petición real. Es también lo que permite que el
 * dominio de cada peña sea un segmento de la ruta.
 *
 * <p>Se implementa como resolutor de recursos y no como un controlador con un patrón comodín a
 * propósito: el patrón necesitaría una expresión regular con anticipación negativa para excluir
 * los prefijos de la API, y el analizador de patrones de Spring no la admite. Aquí el reparto es
 * explícito y, sobre todo, solo entra en juego cuando el fichero pedido no existe, con lo que no
 * se interpone en nada que ya funcionara.
 *
 * <p>El reparto se apoya en un invariante: <strong>todo lo que cuelga de {@code /api} es API y
 * todo lo demás es navegación del frontend</strong>. Por eso la autenticación se movió de
 * {@code /auth} a {@code /api/auth}: el frontend tiene sus propias rutas en {@code /auth/login} y
 * {@code /auth/register}, y con ambas cosas en el mismo prefijo no había forma de saber si una
 * petición era una llamada a la API o una navegación.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    /** Dónde vive el frontend compilado dentro del jar (lo copia ahí el Dockerfile). */
    private static final String RAIZ_ESTATICOS = "classpath:/static/";

    private static final String INDEX = "static/index.html";

    /**
     * Prefijos que nunca son navegación del frontend. Una petición a algo inexistente bajo estos
     * prefijos debe devolver 404 y no el HTML del frontend: si devolviera el HTML con un 200, un
     * error de la API se manifestaría en el cliente como una respuesta JSON ilegible y depurarlo
     * sería un infierno.
     */
    private static final List<String> PREFIJOS_NO_NAVEGABLES =
            List.of("api/", "management/", "v2/", "v3/", "swagger-ui/", "swagger-resources/",
                    "configuration/", "webjars/");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(RAIZ_ESTATICOS)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String rutaSolicitada, Resource ubicacion)
                            throws IOException {
                        // Un directorio (la raíz llega como ruta vacía) se resuelve al index. Sin
                        // este caso se devolvería el propio directorio, que "existe" pero se sirve
                        // como un cuerpo vacío.
                        if (esDirectorio(rutaSolicitada)) {
                            return indexONada();
                        }

                        Resource fichero = ubicacion.createRelative(rutaSolicitada);

                        // El fichero existe (un bundle, una imagen, el propio index.html): se
                        // sirve tal cual, que es el comportamiento de siempre.
                        if (fichero.exists() && fichero.isReadable()) {
                            return fichero;
                        }

                        if (esPrefijoNoNavegable(rutaSolicitada) || pareceFichero(rutaSolicitada)) {
                            return null; // 404 de verdad
                        }

                        // Ruta de navegación del frontend: se devuelve index.html y el router
                        // decide qué pintar.
                        return indexONada();
                    }
                });
    }

    /**
     * El index.html del frontend, o {@code null} si no está empaquetado.
     *
     * Devolver null en ese caso da un 404 limpio en lugar de un error del servidor. Ocurre al
     * arrancar el backend en local sin haber compilado el frontend antes: ahí el frontend se sirve
     * desde su propio servidor de desarrollo y el backend no tiene que servir ninguna página.
     */
    private static Resource indexONada() {
        Resource index = new ClassPathResource(INDEX);
        return index.exists() ? index : null;
    }

    /** La raíz llega como ruta vacía; el resto de directorios, terminados en barra. */
    private static boolean esDirectorio(String rutaSolicitada) {
        return rutaSolicitada.isEmpty() || rutaSolicitada.endsWith("/");
    }

    private static boolean esPrefijoNoNavegable(String rutaSolicitada) {
        return PREFIJOS_NO_NAVEGABLES.stream().anyMatch(rutaSolicitada::startsWith);
    }

    /**
     * Un último segmento con punto es un fichero que no está (un bundle con hash que ya no
     * existe, una imagen mal referenciada). Devolver index.html en ese caso escondería el
     * problema detrás de un 200 con HTML donde el navegador espera un script o una imagen.
     */
    private static boolean pareceFichero(String rutaSolicitada) {
        int ultimaBarra = rutaSolicitada.lastIndexOf('/');
        return rutaSolicitada.indexOf('.', ultimaBarra + 1) >= 0;
    }
}
