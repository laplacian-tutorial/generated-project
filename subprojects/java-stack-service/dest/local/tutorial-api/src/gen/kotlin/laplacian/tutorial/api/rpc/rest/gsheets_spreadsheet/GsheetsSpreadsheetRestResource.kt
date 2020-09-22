package laplacian.tutorial.api.rpc.rest.gsheets_spreadsheet

import java.util.concurrent.CompletableFuture

interface GsheetsSpreadsheetRestResource {
    fun getSpreadsheetById(
        request: GetSpreadsheetByIdRequest
    ): CompletableFuture<GetSpreadsheetByIdResponse>
}