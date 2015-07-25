package clusterconsole.client.components

import clusterconsole.client.services.{ ClusterStoreActions, ClusterStore }
import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ ClusterForm, HostPort, DiscoveredCluster }
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import clusterconsole.client.services.Logger._

object ClusterFormComponent {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class EditClusterProps(cluster: ClusterForm, editHandler: ClusterForm => Unit)

  case class State(cluster: ClusterForm, seeds: Int, portValid: Boolean, submitEnabled: Boolean)

  class Backend(t: BackendScope[EditClusterProps, State]) {

    def updateClusterForm(newForm: ClusterForm) = {
      ClusterStoreActions.updateClusterForm(newForm)
    }

    def updateClusterName(e: ReactEventI): Unit = {
      t.modState { s =>
        val newState = s.copy(cluster = ClusterForm(e.currentTarget.value, s.cluster.seeds))
        updateClusterForm(newState.cluster)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }
    }

    def updateClusterSeedHost(index: Int)(e: ReactEventI): Unit = {
      t.modState { s =>
        val newState =
          s.copy(cluster = ClusterForm(s.cluster.name, seeds = {
            s.cluster.seeds.zipWithIndex.map {
              case (seed, i) =>
                if (index == i) {
                  (seed.copy(host = e.currentTarget.value), i)
                } else {
                  (seed, i)
                }
            }.map(_._1)
          }))

        updateClusterForm(newState.cluster)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }
    }

    def setPortValue(form: ClusterForm, v: String, index: Int): ClusterForm =
      ClusterForm(form.name, seeds = {
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
            val newState = s.copy(cluster = setPortValue(s.cluster, portValue.toString, index))
            updateClusterForm(newState.cluster)
            newState.copy(portValid = (portValue <= 65535), submitEnabled = getSubmitEnabled(newState))
          }

        } catch {
          case ex: Throwable =>
            t.modState(s =>
              s.copy(portValid = false, cluster = setPortValue(s.cluster, e.currentTarget.value.toString, index),
                submitEnabled = getSubmitEnabled(s)))
        }
      } else {
        t.modState { s =>
          val newState = s.copy(portValid = true, cluster = setPortValue(s.cluster, e.currentTarget.value.toString, index))
          updateClusterForm(newState.cluster)
          newState.copy(submitEnabled = getSubmitEnabled(newState))
        }

      }
    }

    def addSeedNodeToForm: Unit = {
      t.modState(s => s.copy(seeds = s.seeds + 1))
    }

    def getSubmitEnabled(s: State): Boolean = {
      s.cluster.name.length > 0 && s.cluster.seeds.forall(hp =>
        hp.host.length > 0 && hp.port != 0 && hp.port.toString.length > 0)
    }

  }

  def component = ReactComponentB[EditClusterProps]("ClusterForm")
    .initialStateP(P => {
      State(P.cluster, 0, true, false)
    }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
      div(cls := "row")(
        div(cls := "col-md-12")(
          h3("Cluster form"),
          form(
            div(cls := "form-group")(
              label("Cluster Name"),
              input(tpe := "text", cls := "form-control", value := S.cluster.name, onChange ==> B.updateClusterName)
            ),

            div(cls := "row col-md-12 form-group")(

              h3("Seeds"),
              {

                P.cluster.seeds.zipWithIndex.map {
                  case (eachSeed, index) =>
                    div(cls := "row", key := s"$index")(
                      div(cls := "form-group col-md-8")(
                        label("Seed host"),
                        input(tpe := "text", cls := "form-control", value := S.cluster.seeds.zipWithIndex.find { case (x, i) => i == index }.map(_._1.host).getOrElse(""),
                          onChange ==> B.updateClusterSeedHost(index))
                      ),
                      div(cls := s"form-group col-md-4 ${if (!S.portValid) "has-error" else ""}")(
                        label("Seed port"),
                        input(tpe := "text", cls := "form-control",
                          value := S.cluster.seeds.zipWithIndex.find { case (x, i) => i == index }.map(_._1.port.toString).getOrElse(""),
                          onChange ==> B.updateClusterSeedPort(index))
                      )
                    )
                }
              }
            ),
            div(cls := "form-group")(
              a(cls := "btn btn-default", disabled := !S.submitEnabled, onClick --> P.editHandler(S.cluster))("Discover")
            )
          )
        )
      )
    }).componentDidMount(x => x.modState(s => s.copy(cluster = x.props.cluster)))
    .build

  def apply(store: ClusterStore, editHandler: ClusterForm => Unit) = component(EditClusterProps(store.getClusterForm(), editHandler))

}
