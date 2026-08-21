package no.roedt

import jakarta.enterprise.context.Dependent
import no.roedt.hypersys.HypersysService
import no.roedt.hypersys.Hypersysverv
import no.roedt.qomon.QomonService
import no.roedt.qomon.externalModel.finnQomonId
import org.eclipse.microprofile.config.inject.ConfigProperty

@Dependent
class VervService(
    val hypersysService: HypersysService,
    val qomonService: QomonService,
    @ConfigProperty(name = "topplag") val topplag: Int
) {
    fun oppdaterVerv(): List<QomonService.Endring> {
        val interessanteVerv = setOf(
            Hypersysverv.Lagsleder,
            Hypersysverv.Fylkesleder,
            Hypersysverv.Nestleder,
            Hypersysverv.Styremedlem,
            Hypersysverv.StyremedlemMedKurs,
            Hypersysverv.StyrelederIKommuneorganisasjon,
            Hypersysverv.StyremedlemIKommuneOrganisasjon,
            Hypersysverv.Lyttekaptein,
            Hypersysverv.Oekonomiansvarleg
        )
        val personerAaGiVerv = hypersysService.hentAlleLagIHierarki(topplag).keys.filterNot { it.id == topplag }.flatMap { lag ->
            println("Finn folk med verv i lag ${lag.name} (${lag.id})")
            val verv = hypersysService.hentVerv(lag.id).filter { verv -> verv.role_type in interessanteVerv.map { it.id } }
            hypersysService.hentMedlemmer(lag.id).filter { medlem -> medlem.name in verv.map { it.name } }.also { println("Fann ${it.size} medlemmar med relevante verv i ${lag.name} (${lag.id}") }
        }.distinct()

        val nyRolle = qomonService.roller().data.organisator()

        val folkIQomon = qomonService.hentFolk().data.users

        val folkFraQomonIHypersys = personerAaGiVerv.filter { folkIQomon.map { it.mail }.contains(it.email) }

        val endringer = folkFraQomonIHypersys.map { person ->
            val qomonBrukerId = folkIQomon.finnQomonId(person)
            qomonService.giRolle(nyRolle, qomonBrukerId)
        }
        return endringer
    }
}