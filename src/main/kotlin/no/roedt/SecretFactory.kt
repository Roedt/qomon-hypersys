package no.roedt

import io.quarkus.arc.profile.IfBuildProfile
import jakarta.enterprise.context.Dependent
import org.eclipse.microprofile.config.inject.ConfigProperty

interface SecretFactory {
    fun getHypersysClientId(): String
    fun getHypersysClientSecret(): String
}

@Dependent
@IfBuildProfile("dev")
class EnvSecretFactory(
    @ConfigProperty(name = "hypersysClientId")
    private var hypersysClientId: String,
    @ConfigProperty(name = "hypersysClientSecret")
    private var hypersysClientSecret: String,
) : SecretFactory {
    override fun getHypersysClientId() = hypersysClientId
    override fun getHypersysClientSecret() = hypersysClientSecret
}