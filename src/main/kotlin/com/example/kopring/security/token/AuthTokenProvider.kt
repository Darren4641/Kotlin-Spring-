package com.example.kopring.security.token

import com.example.kopring.security.RoleType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


@Component
class AuthTokenProvider (
    @Value("\${jwt.access.key}")
    private val key : String,
    @Value("\${jwt.access.validtime}")
    private val accessTokenValidTime : Long,
) {

    companion object {
        private const val AUTHORITIES_KEY : String = "roles"
    }

    private val secretKey : SecretKey by lazy { SecretKeySpec(key.toByteArray(), 0, key.toByteArray().size, "HmacSHA256") }

    fun createToken(id : String) : String {
        val now = Date()
        return Jwts.builder()
            .setSubject(id)
            .claim(AUTHORITIES_KEY, listOf(RoleType.USER.role))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .setExpiration(Date(now.time + accessTokenValidTime))
            .compact()
    }

    fun createToken(id: String, roles: MutableCollection<out GrantedAuthority>) : String {
        val now = Date()
        return Jwts.builder()
            .setSubject(id)
            .claim(AUTHORITIES_KEY, roles)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .setExpiration(Date(now.time + accessTokenValidTime))
            .compact()
    }


    fun getAuthentication(token : String) : Authentication {
        val claims : Claims = getTokenClaims(token)
        var roles = claims.get(AUTHORITIES_KEY).toString()
        roles = deleteBracket(roles)
        val roleNames = roles.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val authorities: Collection<GrantedAuthority?> = Arrays.stream(roleNames)
            .map { role: String? -> SimpleGrantedAuthority(role) }
            .collect(Collectors.toList())
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }


    private fun getTokenClaims(token : String): Claims {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (ex : Exception) {
            throw ex
        }
    }

    private fun deleteBracket(rolesToString : String) : String = rolesToString.replace("[", "").replace("]", "")
}