package click

import chisel3._

/**
 * A phase-decoupled click element
 * @param ri Initial value of the out.req signal
 */
class ClickElement(ri: Boolean = false) extends Module {
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val ackIn = Output(Bool())
    val reqOut = Output(Bool())
    val ackOut = Input(Bool())
    val click = Output(Clock())
  })

  val click = (io.reqIn ^ io.ackIn) & (io.reqOut ^ io.ackOut)

  /** Input phase register, driving ack on input channel */
  val Pi = withClock(click.asClock) { Module(new PhaseRegister(false)) }
  /** Output phase register, driving req on output channel */
  val Po = withClock(click.asClock) { Module(new PhaseRegister(ri)) }

  Pi.io.in := !Pi.io.out
  Po.io.in := !Pi.io.out

  io.ackIn := Pi.io.out
  io.reqOut := Po.io.out
  io.click := click.asClock
}
