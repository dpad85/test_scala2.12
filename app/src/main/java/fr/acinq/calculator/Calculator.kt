package fr.acinq.calculator

import akka.actor.UntypedActor
import org.greenrobot.eventbus.EventBus

class CalculatorPayload(val payload: Int)

data class Add(val x: Int)
data class Subtract(val x: Int)
object Reset

class Calculator : UntypedActor() {
  var state: Int = 0

  override fun onReceive(event: Any?) {
    when (event) {
      is Add -> state += event.x
      is Subtract -> state -= event.x
      is Reset -> state = 0
    }
    EventBus.getDefault().post(CalculatorPayload(state))
  }
}