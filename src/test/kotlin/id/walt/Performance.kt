package id.walt
/*
import id.walt.credentials.w3c.W3CIssuer
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import id.walt.test.getTemplate
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis



class Performance : StringSpec({
    ServiceMatrix("service-matrix.properties")
    val issuerWebDid: String = DidService.create(DidMethod.web)

    //val credentialService: JwtCredentialService = JwtCredentialService.getService()
    val signatory = Signatory.getService()
    val template = getTemplate("ebsi-attestation")

    template.issuer = W3CIssuer(issuerWebDid)
    template.credentialSubject!!.id = issuerWebDid // self signed

    "Issue 1" {
        val jobs = ArrayList<Job>()
        repeat(100000) {
            jobs.add(GlobalScope.launch {
                println("" + measureTimeMillis {
                    signatory.issue("VerifiableId", ProofConfig(issuerDid = issuerWebDid, proofType = ProofType.JWT))
                } + " ms")
            })
        }

        jobs.forEach { it.join() }
    }

})
*/
