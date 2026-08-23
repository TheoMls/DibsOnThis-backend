package com.theomls.dibs.utils

import com.theomls.dibs.models.GroupConstants

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
        if (tries >= GroupConstants.UNIQUE_GENERATION_MAX_ATTEMPTS) {
            throw IllegalStateException("Failed to generate unique $whatUnique after $tries attempts.")
        }
    } while (exists)
    return code
}
