package clusterconsole.client.components

import clusterconsole.client.style.GlobalStyles
import clusterconsole.http.{ClusterForm, HostPort, DiscoveredCluster}
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react._
import clusterconsole.client.services.Logger._


object ClusterFormComponent {

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  case class EditClusterProps(cluster: ClusterForm, editHandler: ClusterForm => Unit)

  case class State(cluster: ClusterForm, seeds: Int, portValid:Boolean)

  class Backend(t: BackendScope[EditClusterProps, State]) {

    def updateClusterName(e: ReactEventI):Unit = {
      t.modState(s =>
        s.copy(cluster = ClusterForm(e.currentTarget.value, s.cluster.seeds)))
    }

    def updateClusterSeedHost(index:Int)(e: ReactEventI):Unit = {
      t.modState(s =>
        s.copy(cluster = ClusterForm(s.cluster.name, seeds = {

          s.cluster.seeds.zipWithIndex.map{  case(seed,i) =>
            if(index == i){
              (seed.copy(host = e.currentTarget.value),i)
            }else{
              (seed,i)
            }
          }.map(_._1)
        })))
    }

    def updateClusterSeedPort(index:Int)(e: ReactEventI):Unit = {

      if(e.currentTarget.value.length > 0){
        try{

          val portValue = e.currentTarget.value.toInt
          t.modState(s =>
            s.copy(cluster = ClusterForm(s.cluster.name, seeds = {
              s.cluster.seeds.zipWithIndex.map{  case(seed,i) =>
                if(index == i){
                  (seed.copy(port = portValue),i)
                }else{
                  (seed,i)
                }
              }.map(_._1)
            })))

        }catch {
          case e:Throwable => t.modState(s => s.copy(portValid = false))
        }
      }else{
        t.modState(s => s.copy(portValid = true))

      }

    }


    def addSeedNodeToForm:Unit = {
      t.modState(s => s.copy(seeds = s.seeds + 1))
      log.debug("*******************  here " )
    }


    def submitEnabled:Boolean = {
      t.state.cluster.name.length > 0 &&  t.state.cluster.seeds.forall(hp => hp.host.length > 0 && hp.port != 0)
    }

    
//    def canSubmit:Boolean = t.st
    
    
  }


  def component = ReactComponentB[EditClusterProps]("ClusterForm")
    .initialStateP(P => {

    log.debug("---------- initialStateP")

    State(P.cluster,1, true)

  }) // initial state
    .backend(new Backend(_))
    .render((P, S, B) => {
    div(cls := "row")(
      div(cls := "col-md-12")(
        h3("Cluster form"),
        form(
          div(cls := "form-group")(
            label("Cluster Name"),
            input(tpe := "text", cls := "form-control", onChange ==> B.updateClusterName)
          ),

          div(cls:="row col-md-12 form-group")(

            h3("Seeds"),
            {

              log.debug(" length " + S.portValid)

              P.cluster.seeds.zipWithIndex.map{ case(eachSeed,index) =>
                div(cls:="row", key:=s"$index")(
                  div(cls := "form-group col-md-8")(
                    label("Seed host"),
                    input(tpe := "text", cls := "form-control", onChange ==> B.updateClusterSeedHost(index))
                  ),
                  div(cls := s"form-group col-md-4 ${if(!S.portValid) "has-error" else ""}")(
                    label("Seed port"),
                    input(tpe := "text", cls := "form-control", onChange ==> B.updateClusterSeedPort(index))
                  )
                )
              }
            }
          ),
          div(cls := "form-group")(
              button( cls := "btn btn-default", disabled:=s"${B.submitEnabled}", onClick --> P.editHandler(S.cluster))("Discover"),
            span(paddingRight:=10)(""),
              button( cls := "btn btn-default", onClick --> B.addSeedNodeToForm)("Add Seed Node")
          )
        )
      )
    )
  }).build

  def apply(cluster: ClusterForm, editHandler: ClusterForm => Unit) = component(EditClusterProps(cluster, editHandler))


}
