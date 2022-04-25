package click

import chisel3._

/**
 * A phase-decoupled click element
 * @param ri Initial value of the out.req signal
 */
class ClickElement(ri: Boolean = false) extends RawModule {
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val ackIn = Output(Bool())
    val reqOut = Output(Bool())
    val ackOut = Input(Bool())
    val reset = Input(AsyncReset())
    val click = Output(Clock())
  })

  val click = (io.reqIn ^ io.ackIn) & !(io.reqOut ^ io.ackOut)

  /** Input phase register, driving ack on input channel */
  val Pi = Module(new PhaseRegister(false))
  /** Output phase register, driving req on output channel */
  val Po = Module(new PhaseRegister(ri))

  Pi.io.in := !Pi.io.out
  Po.io.in := !Pi.io.out

  Pi.io.clock := click.asClock
  Po.io.clock := click.asClock
  Pi.io.reset := io.reset
  Po.io.reset := io.reset

  io.ackIn := Pi.io.out
  io.reqOut := Po.io.out
  io.click := click.asClock
}
