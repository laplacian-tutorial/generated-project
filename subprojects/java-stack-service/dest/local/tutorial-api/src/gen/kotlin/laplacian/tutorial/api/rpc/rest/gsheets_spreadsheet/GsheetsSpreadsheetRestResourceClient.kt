package laplacian.tutorial.api.rpc.rest.gsheets_spreadsheet

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.HttpMethod
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Component
class GsheetsSpreadsheetRestResourceClient(
    @Value("\${http_client.google_sheets_api.base_url}")
    private val baseUrl: String,
    @Value("\${http_client.google_sheets_api.api_key}")
    private val apiKey: String,
): GsheetsSpreadsheetRestResource {
    override fun getSpreadsheetById(
        request: GetSpreadsheetByIdRequest
    ): CompletableFuture<GetSpreadsheetByIdResponse> =
        client
        .method(HttpMethod.GET)
        .uri {
            it
            .path("/spreadsheets/{spreadsheetId}")
            .queryParam("key", apiKey)
            .build(mapOf(
                "spreadsheetId" to request.spreadsheetId,
            ))
        }
        .retrieve()
        .bodyToMono(GetSpreadsheetByIdResponse::class.java)
        .toFuture()

    private val client = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()
}