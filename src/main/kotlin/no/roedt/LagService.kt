package no.roedt

import jakarta.enterprise.context.Dependent
import no.roedt.hypersys.HypersysService
import no.roedt.hypersys.externalModel.HypersysLagId
import no.roedt.hypersys.externalModel.HypersysMedlemId
import no.roedt.hypersys.externalModel.membership.Membership
import no.roedt.qomon.QomonBrukerId
import no.roedt.qomon.QomonService
import no.roedt.qomon.QomonTeamId
import no.roedt.qomon.externalModel.finnQomonId
import org.eclipse.microprofile.config.inject.ConfigProperty
import kotlin.collections.forEach

@Dependent
class LagService(
    val hypersysService: HypersysService,
    val qomonService: QomonService,
    @ConfigProperty(name = "topplag") val topplag: Int,
) {

    fun finnLagOgFolkAaOppdatere(): Map<QomonTeamId, List<Pair<HypersysMedlemId, QomonBrukerId>>> {
        val alleLag = hypersysService.hentAlleLagIHierarki(topplag)
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