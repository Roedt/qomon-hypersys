package no.roedt.qomon

import jakarta.enterprise.context.ApplicationScoped
import no.roedt.SecretFactory
import no.roedt.hypersys.externalModel.HypersysMedlemId
import no.roedt.hypersys.externalModel.Organisasjonsledd
import no.roedt.qomon.externalModel.CreateTeamRequest
import no.roedt.qomon.externalModel.CreateTeamResponseData
import no.roedt.qomon.externalModel.PatchTeamData
import no.roedt.qomon.externalModel.PatchTeamRequest
import no.roedt.qomon.externalModel.Role
import no.roedt.qomon.externalModel.RolePatchRequest
import no.roedt.qomon.externalModel.RolesResponse
import no.roedt.qomon.externalModel.Team
import no.roedt.qomon.externalModel.TeamUser
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory

@ApplicationScoped
class QomonService(
    @RestClient val klient: QomonRestClient,
    val secretFactory: SecretFactory
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun roller(): RolesResponse {
        val authorization = authorization()
        return klient.roller(
            authorization = authorization,
        )
    }

    fun opprettLag(alleLag: Collection<Organisasjonsledd>) {
        val lagIQomon = hentTeamFraQomon()

        val manglendeLag = alleLag.filterNot { hs -> lagIQomon.map { l -> l.name }.contains(hs.name) }

        val qomonlagSomIkkjeErIHypersys = lagIQomon.filterNot { hs -> alleLag.map { l -> l.name }.contains(hs.name) }
        require(qomonlagSomIkkjeErIHypersys.isEmpty()) {
            "Det fins lag i Qomon som ikkje matchar med Hypersys: $qomonlagSomIkkjeErIHypersys"
        }

        logger.info("${manglendeLag.size} fins ikkje i Qomon, opprettar dei")
        val authorization = authorization()
        manglendeLag
            .forEach {
                logger.info("Opprettar lag ${it.name} i Qomon")
                val request = CreateTeamRequest(CreateTeamResponseData(Team(id = it.id, name = it.name)))
                klient.opprettTeam(authorization, request)
                logger.info("Oppretta lag ${it.name} i Qomon")
        }
        logger.info("Ferdig med å opprette ${manglendeLag.size} lag i Qomon")
    }

    fun hentTeamFraQomon(): List<Team> {
        val manuelleUnntak = setOf(
            "Rødt Norge", "Sentralt ansatte", "Admingruppa", "S33", "Testlag2 for e-postsjekk"
        )
        return klient.teams(authorization()).data.teams.filterNot { it.name in manuelleUnntak }
            .map { team -> overstyrteNavn[QomonTeamNavn(team.name)]?.let { overstyrt -> team.copy(name = overstyrt.navn) } ?: team }
    }

    private fun authorization(): String = "Bearer ${secretFactory.getQomonApiKey()}"

    fun hentFolk() = klient.getUsers(authorization())

    fun oppdaterTeams(nyeFolkILag: Map<QomonTeamId, List<Pair<HypersysMedlemId, QomonBrukerId>>>) {
        val authorization = authorization()
        nyeFolkILag.forEach {
            logger.info("Skal oppdatere folk i team med qomon-teamid ${it.key}")
            val respons = klient.team(authorization, it.key.id)
            val dagensTeam = respons.data.team
            val oppdatertUsers = (dagensTeam.users + nyeFolkILag.getOrDefault(it.key, mutableListOf()).map { TeamUser(it.second.id) }).distinct()

            if (oppdatertUsers != dagensTeam.users) {
                val oppdatert = dagensTeam.copy(users = oppdatertUsers)
                val patchTeamRequest = PatchTeamRequest(PatchTeamData(oppdatert))
                klient.oppdaterTeam(authorization, patchTeamRequest)
                logger.info("La til ${it.value.size} til team ${it.key}")
            } else {
                logger.info("Ingen endringar i brukarar for lag ${it.key}. Gjer ingenting med dei.")
            }

        }
    }

    fun giRolle(nyRolle: Role, id: QomonBrukerId) {
        val noverandeRolle = klient.getUser(authorization(), id.id)
        val roleData = noverandeRolle.data.user.role_data
        if (roleData.order == 4) {
            println("Personen med id ${id.id} har rolla aktivist. Oppgraderer til organisator")
//            klient.giRolle(authorization(), RolePatchRequest(
//                roleId = nyRolle.order,
//                userId = id.id
//            ))
        }
        else if (roleData.order in setOf(0, 1, 2, 3)) {
            println("Personen med id ${id.id} har allereie rolle $roleData. Gjer ingenting, for vi vil ikkje nedgradere.")
            return
        } else {
            throw IllegalStateException("Personen med id ${id.id} hadde uforventa rolle i Qomon: $roleData. Veit ikkje korleis vi skal handtere dette, så kastar exception")
        }
    }
}

private val overstyrteNavn: Map<QomonTeamNavn, HypersysLagNavn> = mapOf(
    QomonTeamNavn("Kinn - kommuneorganisasjon") to HypersysLagNavn("Kinn - kommuneorganisasjon i Rødt")
)

@JvmInline
value class QomonTeamNavn(val navn: String)

@JvmInline
value class HypersysLagNavn(val navn: String)
