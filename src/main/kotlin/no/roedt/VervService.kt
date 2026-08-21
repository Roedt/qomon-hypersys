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
        val personerAaGiVerv = hypersysService.finnPersonerMedVerv(topplag, interessanteVerv)

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