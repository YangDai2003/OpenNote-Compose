package com.yangdai.opennote.presentation.util

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.readRawBytes
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebDavClient(
    private val baseUrl: String,
    private val username1: String,
    private val password1: String
) {
    private val client = HttpClient(Android) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username1, password1)
                }
                realm = "WebDAV"
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }

    /**
     * Tests connection to the WebDAV server
     * @return Result with success/failure information
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = client.request(baseUrl) {
                method = HttpMethod.Options
            }

            return@withContext if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Connection failed with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists files and directories at a given path
     */
    suspend fun listDirectory(path: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = client.request("$baseUrl/$path") {
                method = HttpMethod("PROPFIND")
                headers {
                    append(HttpHeaders.Depth, "1")
                }
            }

            if (response.status.isSuccess()) {
                // Parse the XML response to extract file/directory paths
                // This is a simplified example - actual XML parsing would be needed
                val responseBody = response.bodyAsText()
                // Basic parsing logic could go here
                val files = responseBody.split("<d:href>")
                    .drop(1)
                    .map { it.substringBefore("</d:href>") }

                Result.success(files)
            } else {
                Result.failure(Exception("Failed to list directory: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a file to the WebDAV server
     */
    suspend fun uploadFile(remotePath: String, content: ByteArray): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.put("$baseUrl/$remotePath") {
                    setBody(content)
                }

                if (response.status.isSuccess()) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to upload file: ${response.status}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Downloads a file from the WebDAV server
     */
    suspend fun downloadFile(remotePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$baseUrl/$remotePath")

            if (response.status.isSuccess()) {
                Result.success(response.readRawBytes())
            } else {
                Result.failure(Exception("Failed to download file: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a directory on the WebDAV server
     */
    suspend fun createDirectory(remotePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = client.request("$baseUrl/$remotePath") {
                method = HttpMethod("MKCOL")
            }

            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to create directory: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}
