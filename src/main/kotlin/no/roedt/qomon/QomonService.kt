package no.roedt.qomon

import jakarta.enterprise.context.ApplicationScoped
import no.roedt.SecretFactory
import no.roedt.qomon.externalModel.RolesResponse
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class QomonService(
    @RestClient val klient: QomonRestClient,
    val secretFactory: SecretFactory
) {
    fun roller(): RolesResponse {
        val authorization = "Bearer ${secretFactory.getQomonApiKey()}"
        return klient.roller(
            authorization = authorization,
        )
    }
}

