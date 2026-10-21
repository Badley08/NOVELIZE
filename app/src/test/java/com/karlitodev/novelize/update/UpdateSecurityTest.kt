package com.karlitodev.novelize.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class UpdateSecurityTest {

    @Test
    fun testSha256FingerprintGeneration() {
        val certBytes = "LuberisseKarlBradOfficialKey2026".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(certBytes).joinToString("") { "%02x".format(it) }

        // SHA-256 must produce 64 hex characters
        assertEquals(64, hash.length)

        val sameBytes = "LuberisseKarlBradOfficialKey2026".toByteArray(Charsets.UTF_8)
        val sameHash = digest.digest(sameBytes).joinToString("") { "%02x".format(it) }
        assertEquals(hash, sameHash)

        val forgedBytes = "UntrustedAttackerKey".toByteArray(Charsets.UTF_8)
        val forgedHash = digest.digest(forgedBytes).joinToString("") { "%02x".format(it) }
        assertFalse("Forged certificate must not match official author signature", hash == forgedHash)
    }

    @Test
    fun testSecurityRejectedState() {
        val errorMsg = "Mise à jour refusée : signature non reconnue."
        val rejectedState = UpdateState.SecurityRejected(errorMsg)
        assertEquals(errorMsg, rejectedState.reason)
    }
}
