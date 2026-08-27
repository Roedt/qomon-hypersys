package no.roedt

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.qomon.QomonService
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory

@ApplicationScoped
@Path("/lag")
class LagResource(
    val lagService: LagService,
    val qomonService: QomonService,
    @ConfigProperty(name = "quarkus.profile") val profile: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    fun synkroniserLag() {
        if (profile != "dev") {
            logger.info("Skal ikkje per no køyre med profil $profile. Avbryt derfor.")
            return
        }
        logger.info("Startar synkronisering av lag")
//        val nyeFolkILag = lagService.finnLagOgFolkAaOppdatere()
//        qomonService.oppdaterTeams(nyeFolkILag)
//        logger.info("Ferdig med synkronisering, oppdaterte folk i ${nyeFolkILag.size} lag")
    }
}