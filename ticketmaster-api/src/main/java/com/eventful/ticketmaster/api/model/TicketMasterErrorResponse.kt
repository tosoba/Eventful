package com.eventful.ticketmaster.api.model

data class TicketMasterErrorResponse(val fault: Fault?, val errors: List<Error>?)
