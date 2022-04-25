package click

import chisel3._

/**
 * A handshake register, used for implementing asynchronous pipelines
 * @param init The value that the register should initialized to
 * @param ri Initial value of the control circuit's out.req signal
 * @tparam T The type of data stored in the register
 *
 */
class HandshakeRegister[T <: Data](init: T, ri: Boolean = false) extends RawModule {
  val io = IO(new HandshakeIO(chiselTypeOf(init)))

  val click = Module(new ClickElement(ri))
  val reg = Module(new CustomClockRegister(init))

  reg.io.clock := click.io.click
  reg.io.reset := io.reset
  reg.io.in := io.in.data

  click.io.reqIn := io.in.req
  click.io.ackOut := io.out.ack
  click.io.reset := io.reset

  io.in.ack := click.io.ackIn
  io.out.req := click.io.reqOut
  io.out.data := reg.io.out
}
