package com.example.ticketmasterapi.model

data class TicketMasterErrorResponse(
    val fault: Fault?,
    val errors: List<Error>?
)