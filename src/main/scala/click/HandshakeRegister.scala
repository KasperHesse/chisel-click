package click

import chisel3._

/**
 * A handshake register, used for implementing asynchronous pipelines
 * @param gen The type of data going into / coming out of the register
 * @param init The value that the register should initialized to
 * @param ri Initial value of the control circuit's out.req signal
 * @tparam T The type of data stored in the register
 *
 */
class HandshakeRegister[T <: Data](gen: T, init: T, ri: Boolean = false) extends Module {
  val io = IO(new HandshakeIO(gen))

  val click = Module(new ClickElement(ri))
  val reg = withClock(click.io.click) { Module(new CustomClockRegister(gen, init))}

  click.io.reqIn := io.in.req
  click.io.ackOut := io.out.ack
  io.in.ack := click.io.ackIn
  io.out.req := click.io.reqOut

  reg.io.in := io.in.data
  io.out.data := reg.io.out
}
