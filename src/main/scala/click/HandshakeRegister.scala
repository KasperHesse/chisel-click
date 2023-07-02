package click

import chisel3._

/**
 * A handshake register, used for implementing asynchronous pipelines
 * @param init The value that the register should initialized to
 * @param ro Initial value of the control circuit's out.req signal
 * @tparam T The type of data stored in the register
 *
 */
class HandshakeRegister[T <: Data](init: T, ro: Boolean = false)
                                  (implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new HandshakeIO(chiselTypeOf(init)))

  val click = Module(new ClickElement(ro))
  val reg = Module(new CustomClockRegister(init))

  reg.clock := click.io.click
  reg.io.in := io.in.data

  click.io.reqIn := io.in.req
  click.io.ackOut := io.out.ack

  io.in.ack := click.io.ackIn
  io.out.req := simDelay(click.io.reqOut, conf.REG_DELAY)
  io.out.data := reg.io.out
}
