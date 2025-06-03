package fr.example.iot_mini_project.cipher


class VigenereCipher(private val encryptKey: String, private val decryptKey: String = encryptKey) {
    private val alphabet = ('A'..'Z').toList()

    fun encrypt(message: String): String {
        val normalizedMessage = message.uppercase()
        val normalizedKey = encryptKey.uppercase()
        val result = StringBuilder()

        normalizedMessage.forEachIndexed { index, char ->
            if (char in alphabet) {
                val keyChar = normalizedKey[index % normalizedKey.length]
                val charPos = alphabet.indexOf(char)
                val keyPos = alphabet.indexOf(keyChar)
                val encryptedPos = (charPos + keyPos) % 26
                result.append(alphabet[encryptedPos])
            } else {
                result.append(char)
            }
        }

        println("Encrypted message: $result")

        return result.toString()
    }

    fun decrypt(encryptedMessage: String): String {
        val normalizedMessage = encryptedMessage.uppercase()
        val normalizedKey = decryptKey.uppercase()
        val result = StringBuilder()

        normalizedMessage.forEachIndexed { index, char ->
            if (char in alphabet) {
                val keyChar = normalizedKey[index % normalizedKey.length]
                val charPos = alphabet.indexOf(char)
                val keyPos = alphabet.indexOf(keyChar)
                val decryptedPos = (charPos - keyPos + 26) % 26
                result.append(alphabet[decryptedPos])
            } else {
                result.append(char)
            }
        }

        return result.toString()
    }
}