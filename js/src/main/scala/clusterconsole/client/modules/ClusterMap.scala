package clusterconsole.client.modules

import clusterconsole.client.ClusterConsoleApp.Loc
import clusterconsole.client.services.{ClusterMemberStore, RefreshClusterMembers, MainDispatcher}
import clusterconsole.shared.ClusterMember
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.extra.router2.RouterCtl
import japgolly.scalajs.react.vdom.all._

import rx._
import rx.ops._


object ClusterMap {

  case class Props(todos: Rx[Seq[ClusterMember]], router: RouterCtl[Loc])

  case class State(selectedItem: Option[ClusterMember] = None, showTodoForm: Boolean = false)

  abstract class RxObserver[BS <: BackendScope[_, _]](scope: BS) extends OnUnmount {
    protected def observe[T](rx: Rx[T]): Unit = {
      val obs = rx.foreach(_ => scope.forceUpdate())
      // stop observing when unmounted
      onUnmount(obs.kill())
    }
  }

  class Backend(t: BackendScope[Props, State]) extends RxObserver(t) {
    def mounted(): Unit = {
      // hook up to TodoStore changes
      observe(t.props.todos)
      // dispatch a message to refresh the todos, which will cause TodoStore to fetch todos from the server
      MainDispatcher.dispatch(RefreshClusterMembers)
    }

//    def editTodo(item: Option[ClusterMember]): Unit = {
//      // activate the todo dialog
//      t.modState(s => s.copy(selectedItem = item, showTodoForm = true))
//    }
//
//    def deleteTodo(item: TodoItem): Unit = {
//      TodoActions.deleteTodo(item)
//    }
//
//    def todoEdited(item: TodoItem, cancelled: Boolean): Unit = {
//      if (cancelled) {
//        // nothing to do here
//        log.debug("Todo editing cancelled")
//      } else {
//        log.debug(s"Todo edited: $item")
//        TodoActions.updateTodo(item)
//      }
//      // hide the todo dialog
//      t.modState(s => s.copy(showTodoForm = false))
//    }
  }

  // create the React component for ToDo management
  val component = ReactComponentB[Props]("TODO")
    .initialState(State()) // initial state from TodoStore
    .backend(new Backend(_))
    .render((P, S, B) => {
    div("Cluster members")
//    Panel(Panel.Props("What needs to be done"), TodoList(TodoListProps(P.todos(), TodoActions.updateTodo, item => B.editTodo(Some(item)), B.deleteTodo)),
//      Button(Button.Props(() => B.editTodo(None)), Icon.plusSquare, " New"),
//      // if the dialog is open, add it to the panel
//      if (S.showTodoForm) TodoForm(TodoForm.Props(S.selectedItem, B.todoEdited))
//      else // otherwise add an empty placeholder
//        Seq.empty[ReactElement])
  })
    .componentDidMount(_.backend.mounted())
    .configure(OnUnmount.install)
    .build

  /** Returns a function with router location system while using our own props */
  def apply(store: ClusterMemberStore) = (router: RouterCtl[Loc]) => {
    component(Props(store.clusterMembers, router))
  }


}
