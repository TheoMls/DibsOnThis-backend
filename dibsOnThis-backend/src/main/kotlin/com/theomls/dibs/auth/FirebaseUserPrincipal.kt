package com.theomls.dibs.auth

/**
 * Holds the authenticated user's Firebase UID during a request.
 */
data class FirebaseUserPrincipal(val uid: String)