package clusterconsole.client.d3

import scala.scalajs.js

package D3 {

  trait Selectors extends js.Object {
    def select(selector: String): Selection = js.native
    def selectAll(selector: String): Selection = js.native
  }

  trait Base extends Selectors {
    var layout: Layout.Layout = js.native
  }

  trait Selection extends js.Array[js.Any] with Selectors {
    def data[A](values: js.Array[A]): UpdateSelection = js.native
    def call(callback: js.Function, args: js.Any*): Selection = js.native
  }

  trait UpdateSelection extends Selection
}

package Layout {

  trait Layout extends js.Object {
    def force(): ForceLayout = js.native
  }

  trait GraphNode extends js.Object {
    var id: Double = js.native
    var index: Double = js.native
    var name: String = js.native
    var px: Double = js.native
    var py: Double = js.native
    var size: Double = js.native
    var weight: Double = js.native
    var x: Double = js.native
    var y: Double = js.native
    var subindex: Double = js.native
    var startAngle: Double = js.native
    var endAngle: Double = js.native
    var value: Double = js.native
    var fixed: Boolean = js.native
    var children: js.Array[GraphNode] = js.native
    var _children: js.Array[GraphNode] = js.native
    var parent: GraphNode = js.native
    var depth: Double = js.native
  }

  trait GraphLink extends js.Object {
    var source: GraphNode = js.native
    var target: GraphNode = js.native
  }

  trait ForceLayout extends js.Function {
    def apply(): ForceLayout = js.native
    def size(): Double = js.native
    def size(mysize: js.Array[Double]): ForceLayout = js.native
    def size(accessor: js.Function2[js.Any, Double, js.Any]): ForceLayout = js.native
    def linkDistance(): Double = js.native
    def linkDistance(number: Double): ForceLayout = js.native
    def linkDistance(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def linkStrength(): Double = js.native
    def linkStrength(number: Double): ForceLayout = js.native
    def linkStrength(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def friction(): Double = js.native
    def friction(number: Double): ForceLayout = js.native
    def friction(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def alpha(): Double = js.native
    def alpha(number: Double): ForceLayout = js.native
    def alpha(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def charge(): Double = js.native
    def charge(number: Double): ForceLayout = js.native
    def chargeDistance(number: Double): ForceLayout = js.native
    def charge(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def theta(): Double = js.native
    def theta(number: Double): ForceLayout = js.native
    def theta(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def gravity(): Double = js.native
    def gravity(number: Double): ForceLayout = js.native
    def gravity(accessor: js.Function2[js.Any, Double, Double]): ForceLayout = js.native
    def links(): js.Array[GraphLink] = js.native
    def links[A <: GraphLink](arLinks: js.Array[A]): ForceLayout = js.native
    def nodes[A <: GraphNode](): js.Array[A] = js.native
    def nodes[A <: GraphNode](arNodes: js.Array[A]): ForceLayout = js.native
    def start(): ForceLayout = js.native
    def resume(): ForceLayout = js.native
    def stop(): ForceLayout = js.native
    def tick(): ForceLayout = js.native
    def on(`type`: String, listener: js.Function0[Unit]): ForceLayout = js.native
    def drag(): Behavior.Drag = js.native
  }

}

package Behavior {

  trait Behavior extends js.Object {
    def drag(): Drag = js.native
  }
  trait Drag extends js.Function {
    def apply(): js.Dynamic = js.native
    var on: js.Function2[String, js.Function2[js.Any, Double, Any], Drag] = js.native
    def origin(): js.Dynamic = js.native
    def origin(origin: js.Any = js.native): Drag = js.native
  }

}

