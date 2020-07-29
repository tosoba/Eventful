package com.eventful.ticketmaster.model

data class TicketMasterErrorResponse(
    val fault: Fault?,
    val errors: List<Error>?
)
