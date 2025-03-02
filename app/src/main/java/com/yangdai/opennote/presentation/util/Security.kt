package com.yangdai.opennote.presentation.util

private const val KEY = "OpenNoteBackupKey@COPYRIGHT-YANGSCODEHUB"

/**
 * Obfuscates the backup data using XOR with a key
 * @param data The JSON string to obfuscate
 * @return Obfuscated data with format marker
 */
fun encryptBackupData(data: String): String {
    // Add a marker to identify obfuscated data
    val marker = "ENC:"

    // XOR obfuscation with the key
    val obfuscated = obfuscateWithXOR(data)

    return marker + obfuscated
}

/**
 * Attempts to de-obfuscate the data if it's obfuscated, otherwise returns it as-is
 * @param data The potentially obfuscated data
 * @return Original JSON string
 */
fun decryptBackupDataWithCompatibility(data: String): String {
    // Check if data is obfuscated by looking for the marker
    if (!data.startsWith("ENC:")) {
        return data // Not obfuscated, return as-is for backward compatibility
    }

    // Remove marker
    val obfuscatedText = data.substring(4)

    // De-obfuscate using XOR with the same key
    return obfuscateWithXOR(obfuscatedText)
}

/**
 * XOR-based obfuscation/de-obfuscation function
 * Same function works for both encryption and decryption due to XOR properties
 */
private fun obfuscateWithXOR(input: String): String {
    val result = StringBuilder()
    val inputChars = input.toCharArray()
    val keyChars = KEY.toCharArray()

    for (i in inputChars.indices) {
        val keyChar = keyChars[i % keyChars.size]
        val obfuscatedChar = inputChars[i].code xor keyChar.code
        result.append(obfuscatedChar.toChar())
    }

    return result.toString()
}
