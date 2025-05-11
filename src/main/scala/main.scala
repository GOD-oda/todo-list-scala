
import routes.TodoRoutes
import java.net.ServerSocket

// Entry point
@main
def main(): Unit = {
  println("Starting server at http://localhost:8080")

  // Create and start the server
  val serverSocket = TodoRoutes.startServer(8080)

  println("Server started. Press Ctrl+C to stop.")

  // Add shutdown hook to close the server socket
  sys.addShutdownHook {
    println("Shutting down server...")
    if (!serverSocket.isClosed) {
      serverSocket.close()
    }
    println("Server stopped.")
  }

  // Keep the main thread alive
  try {
    Thread.currentThread().join()
  } catch {
    case _: InterruptedException => // Exit
  }
}
