package no.roedt.google

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretVersionName
import io.quarkus.arc.profile.UnlessBuildProfile
import jakarta.annotation.PostConstruct
import jakarta.annotation.Priority
import jakarta.enterprise.context.Dependent
import no.roedt.SecretFactory
import org.eclipse.microprofile.config.inject.ConfigProperty

@Dependent
@Priority(100)
@UnlessBuildProfile("dev")
class GCPSecretFactory(
    @ConfigProperty(name = "secretManagerProjectId", defaultValue = "")
    var secretManagerProjectId: String,
) : SecretFactory {
    private lateinit var client: SecretManagerServiceClient

    @PostConstruct
    fun setup() {
        client = SecretManagerServiceClient.create()
    }


    override fun getHypersysClientId() = getSecretFromSecretManager(GCPSecretManagerKey.HYPERSYS_CLIENT_ID)

    override fun getHypersysClientSecret() = getSecretFromSecretManager(GCPSecretManagerKey.HYPERSYS_CLIENT_SECRET)

    private fun getSecretFromSecretManager(secretName: GCPSecretManagerKey): String {
        val secretVersionName = SecretVersionName.of(secretManagerProjectId, secretName.key, "latest")
        return client.accessSecretVersion(secretVersionName).payload.data.toStringUtf8()
    }
}

private enum class GCPSecretManagerKey(val key: String) {
    HYPERSYS_CLIENT_ID("hypersysClientId"),
    HYPERSYS_CLIENT_SECRET("hypersysClientSecret")
}