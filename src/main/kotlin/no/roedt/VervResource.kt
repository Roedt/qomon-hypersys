package no.roedt

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.qomon.QomonService
import org.slf4j.LoggerFactory

@ApplicationScoped
@Path("/verv")
class VervResource(
    val vervService: VervService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    fun synkroniserVerv() {
        logger.info("Startar synkronisering av verv")
        val verv = vervService.oppdaterVerv()
        logger.info("Ferdig med synkronisering av verv. Ga ny rolle til ${verv.count { it == QomonService.Endring.NY_ROLLE }}, og inga endring på resterande ${verv.count { it == QomonService.Endring.INGEN }}")
    }
}