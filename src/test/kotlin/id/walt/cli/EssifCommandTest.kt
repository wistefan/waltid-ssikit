package id.walt.cli

import com.github.ajalt.clikt.core.PrintHelpMessage
import id.walt.crypto.KeyAlgorithm
import id.walt.model.DidMethod
import id.walt.model.DidUrl
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.services.ecosystems.essif.timestamp.Timestamp
import id.walt.services.ecosystems.essif.timestamp.WaltIdTimestampService
import id.walt.services.key.KeyService
import id.walt.test.RESOURCES_PATH
import io.kotest.assertions.retry
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.mpp.log
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class EssifCommandTest : StringSpec({

    val bearerToken = File("data/ebsi/bearer-token.txt")
    val enableTests = bearerToken.exists()

    ServiceMatrix("service-matrix.properties")

    // DID used for onboarding
    val key = KeyService.getService().generate(KeyAlgorithm.EdDSA_Ed25519)
    val ethKey = KeyService.getService().generate(KeyAlgorithm.ECDSA_Secp256k1)
    val did = DidService.create(DidMethod.ebsi, keyAlias = key.id)
    val identifier = DidUrl.from(did).identifier

    "1. onboard --help" {
        val e = shouldThrow<PrintHelpMessage> {
            EssifOnboardingCommand().parse(listOf("--help"))
        }
        val message = e.command.getFormattedHelp()
        println(message)
        message shouldContain "BEARER-TOKEN-FILE"
        message shouldContain "-d, --did"
    }

    /**
     * Before running the following tests a valid bearer token needs to be place in file data/ebsi/bearer-token.txt.
     * The token can be retrieved from https://app-pilot.ebsi.eu/users-onboarding/v2/
     */
    "2. onboard --did".config(enabled = enableTests) {
        if (!bearerToken.exists()) throw Exception("Bearer Token from https://app-pilot.ebsi.eu/users-onboarding/v2/ should be placed in file data/ebsi/bearer-token.txt")

        println("Generating verifiable authorization...")
        EssifOnboardingCommand().parse(listOf("--did", did, File("data/ebsi/bearer-token.txt").absolutePath))
        File("data/ebsi/${identifier}/verifiable-authorization.json").exists() shouldBe true
    }

    "3. auth-api --did".config(enabled = enableTests) {
        println("Starting auth...")
        EssifAuthCommand().parse(listOf("--did", did))
        File("data/ebsi/${identifier}/ebsi_access_token.json").exists() shouldBe true
        File("data/ebsi/${identifier}/ake1_enc.json").exists() shouldBe true
    }

    "4. did register --did".config(enabled = enableTests) {
        retry(9, 2.minutes, delay = 4.seconds) {
            println("Registering did")
            shouldNotThrowAny {
                EssifDidRegisterCommand().parse(listOf("--did", did, "--eth-key", ethKey.id))
            }
        }
    }

    var transactionHash: String? = null
    "5. Insert timestamp".config(enabled = enableTests) {
        retry(9, 2.minutes, delay = 4.seconds) {
            println("Inserting timestamp.")
            shouldNotThrowAny {

                EssifTimestampCreateCommand().parse(
                    listOf(
                        "--did",
                        did,
                        "--eth-key",
                        ethKey.id,
                        "${RESOURCES_PATH}/ebsi/test-data.json"
                    )
                )

                transactionHash =
                    WaltIdTimestampService().createTimestamp(did, ethKey.id, "{\"test\": \"${UUID.randomUUID()}\"}")
                log { "ESSIFCOMMANDTEST: $transactionHash" }
                transactionHash.shouldNotBeEmpty()
                transactionHash.shouldNotBeBlank()
            }
        }
    }

    "6. Get timestamp transaction hash".config(enabled = enableTests) {
        val timestamp =
            WaltIdTimestampService().getByTransactionHash("0xc6411b4fa8a86d21443db963649efd1a32d794147f21e7d98fda4519086f9f3a"/*transactionHash!!*/)
        validateTimestamp(timestamp)

        WaltIdTimestampService().getByTransactionHash("do not exist") shouldBe null

        EssifTimestampGetCommand().parse(
            listOf(
                "--timestamp-txhash",
                "0xc6411b4fa8a86d21443db963649efd1a32d794147f21e7d98fda4519086f9f3a"
            )
        )
    }

    "7. Get by timestamp Id".config(enabled = enableTests) {
        val timestamp =
            WaltIdTimestampService().getByTimestampId("uEiBrUuxV-ybYtSQ4oiczDhFkmUR7Wk6QtF26SXaD9h9RTg"/*timestampId!!*/)
        validateTimestamp(timestamp)
        EssifTimestampGetCommand().parse(listOf("--timestamp-id", "uEiBrUuxV-ybYtSQ4oiczDhFkmUR7Wk6QtF26SXaD9h9RTg"))
    }

    // TODO: ESSIF backend issue
    "8. essif tir get -r".config(enabled = false) {
        EssifTirGetIssuerCommand().parse(listOf("--did", "did:ebsi:224AEY73SGS1gpTvbt5TNTTPdNj8GU6NAq2AVBFmasQbntCt", "-r"))
    }

    // TODO: ESSIF backend issue
    "9. essif tir get -t".config(enabled = false) {
        EssifTirGetIssuerCommand().parse(listOf("--did", "did:ebsi:224AEY73SGS1gpTvbt5TNTTPdNj8GU6NAq2AVBFmasQbntCt", "-t"))
    }
})

private fun validateTimestamp(timestamp: Timestamp?) {
    println("Validating timestamp: $timestamp")

    timestamp shouldNotBe null
    timestamp!!.timestampId shouldBe "uEiBrUuxV-ybYtSQ4oiczDhFkmUR7Wk6QtF26SXaD9h9RTg"
    timestamp.hash shouldBe "mEiDF2GjIksERie3tGpV4JHS1VPhNwRWjJA45NLCwlw9ZIA"
    timestamp.transactionHash shouldBe "0xc6411b4fa8a86d21443db963649efd1a32d794147f21e7d98fda4519086f9f3a"
    timestamp.timestampedBy shouldBe "0x69e48d89bf5e09588E858D757323b4abBBB3f814"
}
