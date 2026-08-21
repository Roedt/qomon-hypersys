package no.roedt.hypersys

import io.quarkus.arc.properties.IfBuildProperty
import jakarta.enterprise.context.Dependent
import no.roedt.SecretFactory
import no.roedt.hypersys.externalModel.Organisasjonsledd
import no.roedt.hypersys.externalModel.Verv
import no.roedt.hypersys.externalModel.membership.Membership
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.collections.contains
import kotlin.io.encoding.Base64

interface HypersysService {
    fun hentFraHypersys(lag: String): Map<String, List<String?>>
    fun hentAlleLag(): List<Organisasjonsledd>
    fun hentAlleLagIHierarki(topplag: Int): Map<Organisasjonsledd, List<Organisasjonsledd>>
    fun hentBruker(id: Int): Any
    fun hentMedlemmer(id: Int): List<Membership>
    fun hentVerv(id: Int): List<Verv>
}

@Dependent
@IfBuildProperty(name = "hypersys.ekte", stringValue = "true")
class EkteHypersysService(
    @RestClient val hypersysKlient: HypersysRestClient,
    val secretFactory: SecretFactory,
) : HypersysService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun hentFraHypersys(lag: String): Map<String, List<String?>> {
        val bearerToken = "Bearer ${hentBearerToken().access_token}"

        val alleLag = hypersysKlient.hentAlleLokallag(bearerToken)
        logger.info("Fann ${alleLag.size} lag totalt")
        val foreldrelag = alleLag.single { l -> l.name == lag }.id

        val lagOgEposter = alleLag.filter { it.parent == foreldrelag }
            .associate { it.name to finnEposter(bearerToken, it) }
        logger.info("Fann ${lagOgEposter.size} aktuelle lag")
        // TODO: Den tomme return-en her er for å sikre at vi ikkje ved eit uhell faktisk lager ekte data
        return mapOf()
    }

    override fun hentAlleLag() = hypersysKlient.hentAlleLokallag("Bearer ${hentBearerToken().access_token}")

    override fun hentAlleLagIHierarki(topplag: Int): Map<Organisasjonsledd, List<Organisasjonsledd>> {
        val alleLag = hentAlleLag()
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

    override fun hentBruker(id: Int) = hypersysKlient.hentBruker("Bearer ${hentBearerToken().access_token}", id.toString())
    override fun hentMedlemmer(id: Int) = hypersysKlient.hentMedlemmerILag(hypersysLokallagId = id, aar = LocalDate.now().year, token = "Bearer ${hentBearerToken().access_token}")

    override fun hentVerv(id: Int) = hypersysKlient.hentVerv("Bearer ${hentBearerToken().access_token}", orgId = id.toString())

    private fun finnEposter(
        bearerToken: String,
        organisasjonsledd: Organisasjonsledd,
    ): List<String?> {
        val organsFraHS = hypersysKlient.hentAlleOrgan(token = bearerToken, orgId = organisasjonsledd.id.toString())

        val detaljerOsloorgan = organsFraHS["organs"]!!
            .filter { it.organ_type == "Lagsstyre" }
            .map {
                hypersysKlient.hentOrgan(
                    token = bearerToken,
                    orgId = it.id.toString(),
                    organId = it.id.toString()
                )
            }
            .singleOrNull { it.members.isNotEmpty() }

        val eposter = detaljerOsloorgan?.members?.map { m -> m.email }?.distinct() ?: listOf()
        return eposter
    }

    private fun hentBearerToken(): GyldigSystemToken {
        val id = secretFactory.getHypersysClientId()
        val secret = secretFactory.getHypersysClientSecret()
        return hypersysKlient.tokenSystem(base64Credentials = "Basic ${Base64.encode("$id:$secret".toByteArray())}")
    }
}

@Dependent
@IfBuildProperty(name = "hypersys.ekte", stringValue = "false")
class FakeHypersysService : HypersysService {
    override fun hentFraHypersys(lag: String): Map<String, List<String?>> = mapOf("Testlag2" to listOf("raudtosloteknisk@gmail.com"))
    override fun hentAlleLag(): List<Organisasjonsledd> = emptyList()
    override fun hentAlleLagIHierarki(topplag: Int): Map<Organisasjonsledd, List<Organisasjonsledd>> = emptyMap()
    override fun hentBruker(id: Int): Any = id // TODO ved typing
    override fun hentMedlemmer(id: Int): List<Membership> = emptyList()
    override fun hentVerv(id: Int): List<Verv> = emptyList()
 }