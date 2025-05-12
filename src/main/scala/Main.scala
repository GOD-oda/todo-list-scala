
import routes.TodoRoutes
import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    for {
      _ <- IO.println("Starting server at http://localhost:8080")

      _ <- TodoRoutes.startServer(8080).use { server =>
        for {
          _ <- IO.println("Server started. Press Ctrl+C to stop.")

          _ <- IO.never
        } yield ()
      }
    } yield ()
  }
}
