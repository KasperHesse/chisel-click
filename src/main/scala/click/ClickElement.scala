package click

import chisel3._

/**
 * A phase-decoupled click element
 * @param ro Initial value of the out.req signal
 */
class ClickElement(ro: Boolean = false) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val ackIn = Output(Bool())
    val reqOut = Output(Bool())
    val ackOut = Input(Bool())
    val click = Output(Clock())
  })

  val click = (io.reqIn ^ io.ackIn) & !(io.reqOut ^ io.ackOut)

  /** Input phase register, driving ack on input channel */
  val Pi = Module(new PhaseRegister(false))
  /** Output phase register, driving req on output channel */
  val Po = Module(new PhaseRegister(ro))

  Pi.io.in := !Pi.io.out
  Po.io.in := !Po.io.out

  Pi.clock := click.asClock
  Po.clock := click.asClock

  io.ackIn := Pi.io.out
  io.reqOut := Po.io.out
  io.click := click.asClock
}
