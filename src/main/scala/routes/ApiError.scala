package routes

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class ApiError(error: String)

object ApiError {
  given Encoder[ApiError] = deriveEncoder[ApiError]
}
