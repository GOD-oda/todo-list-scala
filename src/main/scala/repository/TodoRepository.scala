package repository

import domain.Todo
import java.util.UUID
import scala.collection.mutable

trait TodoRepository {
  def findAll(): List[Todo]
  def findById(id: String): Option[Todo]
  def save(todo: Todo): Todo
  def delete(id: String): Boolean
}

class InMemoryTodoRepository extends TodoRepository {
  private val todos: mutable.Map[String, Todo] = mutable.Map.empty

  override def findAll(): List[Todo] = todos.values.toList

  override def findById(id: String): Option[Todo] = todos.get(id)

  override def save(todo: Todo): Todo = {
    todos.put(todo.id, todo)
    todo
  }

  override def delete(id: String): Boolean = {
    todos.remove(id).isDefined
  }
}

object TodoRepository {
  def inMemory(): TodoRepository = {
    val repo = new InMemoryTodoRepository()
    repo.save(Todo("Learn Scala"))
    repo.save(Todo("Build a REST API"))
    repo.save(Todo("Write tests"))
    repo
  }
}
