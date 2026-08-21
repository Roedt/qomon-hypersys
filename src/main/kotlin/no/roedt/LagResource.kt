package no.roedt

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import no.roedt.hypersys.HypersysService
import no.roedt.hypersys.externalModel.HypersysLagId
import no.roedt.hypersys.externalModel.HypersysMedlemId
import no.roedt.hypersys.externalModel.Organisasjonsledd
import no.roedt.hypersys.externalModel.membership.Membership
import no.roedt.qomon.QomonBrukerId
import no.roedt.qomon.QomonService
import no.roedt.qomon.QomonTeamId
import no.roedt.qomon.externalModel.User
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import kotlin.collections.forEach

@ApplicationScoped
@Path("/lag")
class LagResource(
    val hypersysService: HypersysService,
    val qomonService: QomonService,
    @ConfigProperty(name = "topplag") val topplag: Int,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    fun synkroniserLag() {
        logger.info("Startar synkronisering av lag")
        val nyeFolkILag = finnLagOgFolkAaOppdatere()
        qomonService.oppdaterTeams(nyeFolkILag)
        logger.info("Ferdig med synkronisering, oppdaterte folk i ${nyeFolkILag.size} lag")
    }

    private fun finnLagOgFolkAaOppdatere(): Map<QomonTeamId, List<Pair<HypersysMedlemId, QomonBrukerId>>> {
        val alleLag = hentAlleLagFraHypersys()
        qomonService.opprettLag(alleLag.keys)

        val folkIQomon = qomonService.hentFolk().data.users

        val folkFraQomonIHypersys: List<Membership> =
            hypersysService.hentMedlemmer(topplag).filter { folkIQomon.map { it.mail }.contains(it.email) }

        val nyeFolkILag = mutableMapOf<QomonTeamId, MutableList<Pair<HypersysMedlemId, QomonBrukerId>>>()

        folkFraQomonIHypersys.forEach { person ->
            val alleLagAaLeggeTilI = alleLag.entries.first { it.key.id == person.organisation_id }
            val medlemId = HypersysMedlemId(person.member_id)
            val qomonBrukerId = folkIQomon.finnQomonId(person)

            nyeFolkILag.putAndAdd(finnQomonTeam(HypersysLagId(alleLagAaLeggeTilI.key.id)), medlemId, qomonBrukerId)
            alleLagAaLeggeTilI.value.forEach {
                nyeFolkILag.putAndAdd(finnQomonTeam(HypersysLagId(it.id)), medlemId, qomonBrukerId)
            }
        }

        return nyeFolkILag
    }

    private fun hentAlleLagFraHypersys(): Map<Organisasjonsledd, List<Organisasjonsledd>> {
        val alleLag = hypersysService.hentAlleLag()
        val toppnivaaLaget = alleLag.first { it.id == topplag }
        val lagsstruktur = mutableMapOf<Organisasjonsledd, List<Organisasjonsledd>>()
        lagsstruktur[toppnivaaLaget] = emptyList()

        // Fylke
        val lagPaaNivaa1: List<Organisasjonsledd> = alleLag.filter { it.parent == topplag }
        lagPaaNivaa1.forEach { lagsstruktur[it] = listOf(toppnivaaLaget) }

        // Kommunelag
        val lagPaaNivaa2 = alleLag.filter { it.parent in lagPaaNivaa1.map { it.id } }
        lagPaaNivaa2.forEach { lag ->
            val forelder = lagsstruktur.entries.firstOrNull { it.key.id == lag.parent }
            val grandforeldre: List<Organisasjonsledd> = forelder?.value ?: emptyList()
            lagsstruktur[lag] = grandforeldre + listOfNotNull(forelder?.key)
        }

        // Lokallag
        val lagPaaNivaa3 = alleLag.filter { it.parent in lagPaaNivaa2.map { it.id } }
        lagPaaNivaa3.forEach { lag ->
            val forelder = lagsstruktur.entries.firstOrNull { it.key.id == lag.parent }
            val grandforeldre: List<Organisasjonsledd> = forelder?.value ?: emptyList()
            lagsstruktur[lag] = grandforeldre + listOfNotNull(forelder?.key)
        }

        return lagsstruktur
    }

    val overstyringer = mapOf( // Frå Hypersys til Qomon
        100783 to 10360, // Bergenhus
        100430 to 10273, // Oslo
        100584 to 10406, // Finnmark
        100445 to 10409, // Troms
        100444 to 10410, // Nordland
        100442 to 10411, // Trøndelag
        100441 to 10412, // M&R
        100439 to 10413, // Hordaland
        100440 to 10414, // S&F
        100438 to 10415, // Rogaland
        100437 to 10416, // Agder
        100432 to 10417, // Oppland
        100431 to 10418, // Hedmark
        100725 to 10419, // Buskerud
        100435 to 10420, // Ve&Te
        100726 to 10421, // Akershus
        100727 to 10422 // Østfold
    )

    fun finnQomonTeam(lagIHypersys: HypersysLagId): QomonTeamId = QomonTeamId((overstyringer[lagIHypersys.id] ?: lagIHypersys.id))
}

private fun MutableMap<QomonTeamId, MutableList<Pair<HypersysMedlemId, QomonBrukerId>>>.putAndAdd(
    key: QomonTeamId,
    hypersysMedlemId: HypersysMedlemId,
    qomonBrukerId: QomonBrukerId
) {
    this.putIfAbsent(key, mutableListOf())
    this[key]!!.add(Pair(hypersysMedlemId, qomonBrukerId))
}

private fun List<User>.finnQomonId(hypersys: Membership) = QomonBrukerId(this.first { q -> q.mail == hypersys.email }.id)