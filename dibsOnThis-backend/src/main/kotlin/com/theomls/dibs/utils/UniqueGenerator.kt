package com.theomls.dibs.utils

const val UNIQUE_GENERATION_MAX_ATTEMPTS = 50

suspend fun generateUnique(
    generator: () -> String,
    existsChecker: suspend (String) -> Boolean,
    whatUnique: String
): String {
    var code: String
    var exists: Boolean
    var tries = 0
    do {
        code = generator()
        exists = existsChecker(code)
        tries++
        if (tries >= UNIQUE_GENERATION_MAX_ATTEMPTS) {
            throw IllegalStateException("Failed to generate unique $whatUnique after $tries attempts.")
        }
    } while (exists)
    return code
}
