package com.toycert.http.controllers

import javax.ws.rs.core.MediaType
import javax.ws.rs.{GET, Path, Produces}

@Path("/api/v1/status")
class StatusController {
  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def get(): String = {
      "OK"
  }
}
