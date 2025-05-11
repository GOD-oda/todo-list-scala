package service

import domain.Todo
import repository.TodoRepository
import java.util.UUID

class TodoService(repository: TodoRepository) {
  def getAllTodos(): List[Todo] = repository.findAll()

  def getTodoById(id: String): Option[Todo] = repository.findById(id)

  def createTodo(title: String): Todo = {
    val todo = Todo(title)
    repository.save(todo)
  }

  def updateTodo(id: String, title: String): Option[Todo] = {
    repository.findById(id).map { todo =>
      val updated = todo.copy(title = title)
      repository.save(updated)
    }
  }

  def deleteTodo(id: String): Boolean = repository.delete(id)
}

object TodoService {
  def apply(repository: TodoRepository): TodoService = new TodoService(repository)
}
