package clusterconsole.client.components

import clusterconsole.client.services.ClusterService
import clusterconsole.client.style.Bootstrap.{ Button, Modal }
import clusterconsole.client.style.{ Icon, GlobalStyles }
import clusterconsole.http.{ ClusterForm, HostPort, DiscoveredCluster }
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom._
import clusterconsole.client.services.Logger._

object ClusterFormComponent {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class EditClusterProps(clusterForm: ClusterForm, editHandler: ClusterForm => Unit, closeForm: () => Unit)

  case class State(clusterForm: ClusterForm, seeds: Int, portValid: Boolean, submitEnabled: Boolean)

  class Backend(t: BackendScope[EditClusterProps, State]) {

    def updateClusterForm(newForm: ClusterForm) = {
      ClusterService.updateClusterForm(newForm)
    }

    def updateClusterName(e: ReactEventI): Unit = {
      t.modState { s =>
        val newState = s.copy(clusterForm = ClusterForm(e.currentTarget.value, s.clusterForm.selfHost, s.clusterForm.seeds))
        updateClusterForm(newState.clusterForm)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }
    }

    def updateClusterSelfHost(e: ReactEventI): Unit = {
      t.modState { s =>
        val newState = s.copy(clusterForm = ClusterForm(s.clusterForm.name, e.currentTarget.value, s.clusterForm.seeds))
        updateClusterForm(newState.clusterForm)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }
    }

    def updateClusterSeedHost(index: Int)(e: ReactEventI): Unit = {
      t.modState { s =>
        val newState =
          s.copy(clusterForm = ClusterForm(s.clusterForm.name, s.clusterForm.selfHost, seeds = {
            s.clusterForm.seeds.zipWithIndex.map {
              case (seed, i) =>
                if (index == i) {
                  (seed.copy(host = e.currentTarget.value), i)
                } else {
                  (seed, i)
                }
            }.map(_._1)
          }))

        updateClusterForm(newState.clusterForm)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }
    }

    def setPortValue(form: ClusterForm, v: String, index: Int): ClusterForm =
      ClusterForm(form.name, form.selfHost, seeds = {
        form.seeds.zipWithIndex.map {
          case (seed, i) =>
            if (index == i) {
              (seed.copy(port = v), i)
            } else {
              (seed, i)
            }
        }.map(_._1)
      })

    def updateClusterSeedPort(index: Int)(e: ReactEventI): Unit = {
      if (e.currentTarget.value.length > 0) {
        try {
          val portValue = e.currentTarget.value.toInt
          t.modState { s =>
            val newState = s.copy(clusterForm = setPortValue(s.clusterForm, portValue.toString, index))
            updateClusterForm(newState.clusterForm)
            newState.copy(portValid = (portValue <= 65535), submitEnabled = getSubmitEnabled(newState))
          }

        } catch {
          case ex: Throwable =>
            t.modState(s =>
              s.copy(portValid = false, clusterForm = setPortValue(s.clusterForm, e.currentTarget.value.toString, index),
                submitEnabled = getSubmitEnabled(s)))
        }
      } else {
        t.modState { s =>
          val newState = s.copy(portValid = true, clusterForm = setPortValue(s.clusterForm, e.currentTarget.value.toString, index))
          updateClusterForm(newState.clusterForm)
          newState.copy(submitEnabled = getSubmitEnabled(newState))
        }

      }
    }

    def addSeedNodeToForm: Unit = {
      t.modState(s => s.copy(seeds = s.seeds + 1))
    }

    def getSubmitEnabled(s: State): Boolean = {
      s.clusterForm.name.length > 0 && s.clusterForm.seeds.forall(hp =>
        hp.host.length > 0 && hp.port != 0 && hp.port.toString.length > 0)
    }

    def hide() = {
      t.modState(_.copy(clusterForm = ClusterForm.initial))
      t.props.closeForm
    }

  }

  def component = ReactComponentB[EditClusterProps]("ClusterForm")
    .initialStateP(P => {
      State(P.clusterForm, 0, true, false)
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {

      Modal(Modal.Props(be => span(button(tpe := "button", cls := "pull-right", onClick --> be.hide(), Icon.close), h4(color := "black")("Discover Cluster")),
        be => span(Button(Button.Props(() => { P.editHandler(S.clusterForm); be.hide() }), "OK")),
        () => B.hide),
        form(
          div(cls := "form-group col-md-8")(
            label()("Cluster Name"),
            input(tpe := "text", cls := "form-control", value := S.clusterForm.name, onChange ==> B.updateClusterName)
          ),
          div(cls := "col-md-12 form-group") {

            P.clusterForm.seeds.zipWithIndex.map {
              case (eachSeed, index) =>
                div(cls := "row", key := s"$index")(
                  div(cls := "form-group col-md-4")(
                    label()("App host"),
                    input(tpe := "text", cls := "form-control", value := S.clusterForm.selfHost, onChange ==> B.updateClusterSelfHost)
                  ),
                  div(cls := "form-group col-md-4")(
                    label()("Seed Host"),
                    input(tpe := "text", cls := "form-control", value := S.clusterForm.seeds.zipWithIndex.find { case (x, i) => i == index }.map(_._1.host).getOrElse(""),
                      onChange ==> B.updateClusterSeedHost(index))
                  ),

                  div(cls := s"form-group col-md-2 ${if (!S.portValid) "has-error" else ""}")(
                    label()("Port"),
                    input(tpe := "text", cls := "form-control",
                      value := S.clusterForm.seeds.zipWithIndex.find { case (x, i) => i == index }.map(_._1.port.toString).getOrElse(""),
                      onChange ==> B.updateClusterSeedPort(index))
                  )
                )
            }
          }
        )
      )
    }).componentDidMount(x => x.modState(s => s.copy(clusterForm = x.props.clusterForm)))
    .build

  def apply(store: ClusterService,
    editHandler: ClusterForm => Unit,
    closeForm: () => Unit) = component(EditClusterProps(store.getClusterForm(), editHandler, closeForm))

}
