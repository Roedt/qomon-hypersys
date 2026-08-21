package no.roedt

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.qomon.QomonService
import org.slf4j.LoggerFactory

@ApplicationScoped
@Path("/lag")
class LagResource(
    val lagService: LagService,
    val qomonService: QomonService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    fun synkroniserLag() {
        logger.info("Startar synkronisering av lag")
        val nyeFolkILag = lagService.finnLagOgFolkAaOppdatere()
        qomonService.oppdaterTeams(nyeFolkILag)
        logger.info("Ferdig med synkronisering, oppdaterte folk i ${nyeFolkILag.size} lag")
    }
}