package no.roedt

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.hypersys.EkteHypersysService
import no.roedt.hypersys.HypersysService
import no.roedt.qomon.QomonService
import org.slf4j.LoggerFactory

@ApplicationScoped
@Path("/")
class Resource(
    val hypersysService: HypersysService,
    val qomonService: QomonService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Path("integrer")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun integrer() {
        val fraHypersys: Map<String, List<String?>> = hypersysService.hentFraHypersys("Rødt Oslo")

        if (hypersysService is EkteHypersysService) {
            logger.info("Gjer intenting mot ekte hypersys for no, returnerer")
            return
        } else {
            logger.info("Bruker fake hypersys. Held fram.")
        }

        val roller = qomonService.roller()


        if (fraHypersys.size > 1 && fraHypersys.keys.firstOrNull()?.startsWith("Testlag") == false) {
            logger.info("Skal ikkje køyre på ordentleg per no")
            throw IllegalStateException("Forventa ikkje ekte hypersysdata")
        }
    }


    @Path("ping")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun ping() = "pong"
}