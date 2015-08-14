package clusterconsole.client.components

import clusterconsole.client.services.{ ClusterService, Logger }
import clusterconsole.client.style.Bootstrap.{ Button, Modal }
import clusterconsole.client.style.{ GlobalStyles, Icon }
import clusterconsole.http._
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import Logger._
import org.scalajs.dom.raw.{ HTMLOptionElement, HTMLSelectElement }

object RolesFormComponent {

  case class Props(cluster: DiscoveredCluster, closeForm: () => Unit)

  case class State(
    dependencies: Seq[RoleDependency],
    roles: Seq[String],
    selectedRoles: Seq[String],
    dependsOnRoles: Seq[String],
    dependencyName: Option[String],
    dependencyType: ClusterDependency)

  class Backend(t: BackendScope[Props, State]) {

    def hide() = {
      log.debug("RolesFormComponent hide()")
      t.props.closeForm
    }

    def selectRole(e: ReactMouseEvent) = {

      val select = e.currentTarget.asInstanceOf[HTMLSelectElement]

      val options: Seq[HTMLOptionElement] = t.state.roles.indices.map(i => select.childNodes(i).asInstanceOf[HTMLOptionElement])

      val selectedRoles = options.filter(_.selected).flatMap(r => t.state.roles.find(e => e == r.value))

      t.modState(_.copy(selectedRoles = selectedRoles))
    }

    def dependsOnRole(e: ReactMouseEvent) = {
      val select = e.currentTarget.asInstanceOf[HTMLSelectElement]

      log.debug("dependsOnRole select: " + select)

      val options: Seq[HTMLOptionElement] = t.state.roles.filter(e =>
        !t.state.selectedRoles.contains(e)).indices.map(i =>
        select.childNodes(i).asInstanceOf[HTMLOptionElement])

      val dependsOnRoles = options.filter(_.selected).flatMap(r => t.state.roles.find(e => e == r.value))

      t.modState(_.copy(dependsOnRoles = dependsOnRoles))
    }

    def selectType(e: ReactMouseEvent) =
      t.modState(_.copy(dependencyType = {
        e.currentTarget.asInstanceOf[HTMLSelectElement].selectedIndex match {
          case 0 => DistributedRouter(t.state.dependencyName.getOrElse(""))
          case 1 => ClusterSharded(t.state.dependencyName.getOrElse(""))
          case 2 => Manual(t.state.dependencyName.getOrElse(""))
          case _ => Manual(t.state.dependencyName.getOrElse(""))
        }

      }
      ))

    def updateDepName(e: ReactEventI) = {
      t.modState(_.copy(dependencyName = {
        if (e.currentTarget.value.length > 0) {
          log.debug("--- " + e.currentTarget.value)
          Some(e.currentTarget.value)
        } else {
          None
        }
      }))
    }

    def canSubmit: Boolean =
      t.state.selectedRoles.length > 0 &&
        t.state.dependsOnRoles.length > 0 &&
        t.state.dependencyName.isDefined

    def addDep(e: ReactMouseEvent) = {
      (for {
        name <- t.state.dependencyName
      } yield {
        RoleDependency(t.state.selectedRoles, t.state.dependsOnRoles, t.state.dependencyType.updateName(name))
      }).foreach(rd =>
        t.modState(_.copy(dependencies =
          t.state.dependencies :+ rd)))

      e.preventDefault()
    }

    def submitForm() = {
      ClusterService.updateClusterDependencies(t.props.cluster.copy(dependencies = t.state.dependencies))
      t.props.closeForm
    }

  }

  val component = ReactComponentB[Props]("DiscoveringClusterComponent")
    .initialStateP(P =>
      State(P.cluster.dependencies, P.cluster.getRoles, Seq.empty[String], Seq.empty[String], None, DistributedRouter(""))
    )
    .backend(new Backend(_))
    .render((P, S, B) =>

      Modal(Modal.Props(
        header = be => span(button(tpe := "button", cls := "pull-right", onClick --> be.hide(), Icon.close), h4(color := "black")("Describe Dependencies")),
        footer = be => span(Button(Button.Props(() => {
          B.submitForm();
          be.hide()
        }), "OK")),
        closed = () => P.closeForm()),

        if (S.dependencies.nonEmpty) {
          div(cls := "row")(
            div(cls := "col-md-12")(
              div(cls := "panel panel-primary")(
                div(cls := "panel-heading")("Existing Dependencies"),
                div(cls := "panel-body")(
                  S.dependencies.map(d =>
                    div(
                      span(d.tpe.name + ": "),
                      span(d.tpe.typeName + ": "),
                      span(d.roles.mkString(",")),
                      span("-->"),
                      span(d.dependsOn.mkString(",")))
                  )
                )
              )
            )
          )
        } else {
          span("")
        },
        div(cls := "row")(
          div(cls := "col-md-12")(
            div(cls := "panel panel-primary")(
              div(cls := "panel-heading")("Add Dependencies"),
              div(cls := "panel-body")(
                form(
                  div(cls := "row")(
                    div(cls := "form-group col-md-4")(
                      label("Roles(s)"),
                      select(name := "selectRole", multiple := "multiple", cls := "form-control", height := { (S.roles.length * 20) + "px" }, onChange ==> B.selectRole)(
                        S.roles.map(r => option(value := r)(r))
                      )
                    ),
                    div(cls := "form-group col-md-3")(
                      label("Depend(s) On")
                    ),

                    S.selectedRoles.headOption.map(selectedRole =>
                      div(cls := "form-group col-md-4")(
                        label("Role(s)"),
                        select(name := "dependsOnRole", multiple := "multiple", cls := "form-control", height := { (S.roles.length * 20) + "px" }, onChange ==> B.dependsOnRole)(
                          S.roles.filter(e => !S.selectedRoles.contains(e)).map(r => option(value := r)(r))
                        )
                      )
                    ).getOrElse(span(""))
                  ),
                  div(cls := "row", paddingTop := "20px")(
                    div(cls := "col-md-12")(
                      div(cls := "row")(
                        S.selectedRoles.headOption.map(selectedRole =>
                          div(cls := "col-md-7")(
                            div(cls := "col-md-8")(S.selectedRoles.mkString(",")),
                            div(cls := "col-md-4")("-->")
                          )
                        ).getOrElse(EmptyTag),
                        S.dependsOnRoles.headOption.map(dependsOnRole =>
                          div(cls := "col-md-5")(
                            span(S.dependsOnRoles.mkString(",")))
                        ).getOrElse(EmptyTag)
                      )
                    )
                  ),
                  div(cls := "row", paddingTop := "20px")(
                    div(cls := "form-group col-md-9")(
                      label("Dependency Name"),
                      input(tpe := "text", cls := "form-control", onChange ==> B.updateDepName)
                    ),
                    div(cls := "form-group col-md-4")(
                      label("Dependency Type"),
                      select(onChange ==> B.selectType)(
                        option("DistributedRouter"),
                        option("ClusterSharded"),
                        option("Manual")
                      )
                    )
                  ),
                  div(cls := "row")(
                    div(cls := "col-md-12")(
                      button(cls := "btn btn-submit", onClick ==> B.addDep, disabled := {
                        !B.canSubmit
                      })("Add dependency")
                    )
                  )
                )
              )
            )
          )
        )
      )
    ).build

  def apply(cluster: DiscoveredCluster, closeForm: () => Unit) = component(Props(cluster, closeForm))

}
