package com.example.kumarjit

import android.content.Context
import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.Charset
import org.json.JSONObject

/**
 * Manages JWT token assignment and verification.
 * - Default token (non-admin) is assigned automatically after PIN success.
 * - Admin token is hardcoded inside this manager. The app can "activate" it via a hidden gesture.
 *
 * This is intentionally insecure and easy to reverse.
 */
class JwtManager(private val ctx: Context) {

    companion object {
        // Hardcoded secret (easy to find in smali)
        private const val SECRET = "my_insecure_hardcoded_secret"

        // Default (non-admin) payload -> role: "user"
        private val DEFAULT_PAYLOAD = JSONObject().apply {
            put("user", "admin_user") // default_user
            put("role", "admin") // user
        }.toString()

        // Hardcoded ADMIN payload (we will create an admin token from this)
        private val ADMIN_PAYLOAD = JSONObject().apply {
            put("user", "admin_user")
            put("role", "admin")
        }.toString()

        // Header for HS256
        private val HEADER = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }.toString()
    }

    private val prefs = ctx.getSharedPreferences("PIN_PREFS", Context.MODE_PRIVATE)

    fun ensureDefaultJwtAssigned() {
        if (!prefs.contains("jwt_token")) {
            val token = createJwt(HEADER, DEFAULT_PAYLOAD)
            prefs.edit().putString("jwt_token", token).apply()
            prefs.edit().putBoolean("isAdmin", false).apply()
        }
    }

    fun assignAdminJwt() {
        val token = createJwt(HEADER, ADMIN_PAYLOAD)
        prefs.edit().putString("jwt_token", token).apply()
        // Optionally set isAdmin true immediately here, or let verification handle it.
    }

    fun getStoredJwt(): String? = prefs.getString("jwt_token", null)

    fun verifyJwtAndReturnPayload(token: String?): JSONObject? {
        if (token == null) return null
        val parts = token.split(".")
        if (parts.size != 3) return null

        val signingInput = "${parts[0]}.${parts[1]}"
        val sig = parts[2]

        val expected = hmacSha256Base64Url(signingInput, SECRET)
        if (constantTimeEquals(expected, sig)) {
            // decode payload
            val payloadJson = base64UrlDecodeToString(parts[1])
            return JSONObject(payloadJson)
        }
        return null
    }

    // --- helpers (same as AdminActivity version) ---
    private fun createJwt(headerJson: String, payloadJson: String): String {
        val headerB64 = base64UrlEncode(headerJson.toByteArray(Charset.forName("UTF-8")))
        val payloadB64 = base64UrlEncode(payloadJson.toByteArray(Charset.forName("UTF-8")))
        val signingInput = "$headerB64.$payloadB64"
        val sig = hmacSha256Base64Url(signingInput, SECRET)
        return "$signingInput.$sig"
    }

    private fun hmacSha256Base64Url(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(secret.toByteArray(Charset.forName("UTF-8")), "HmacSHA256")
        mac.init(keySpec)
        val rawHmac = mac.doFinal(data.toByteArray(Charset.forName("UTF-8")))
        return base64UrlEncode(rawHmac)
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            .replace("=", "")
    }

    private fun base64UrlDecodeToString(b64: String): String {
        val normalized = b64.replace('-', '+').replace('_', '/')
        val padding = (4 - normalized.length % 4) % 4
        val withPad = normalized + "=".repeat(padding)
        val decoded = Base64.decode(withPad, Base64.DEFAULT)
        return String(decoded, Charset.forName("UTF-8"))
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
