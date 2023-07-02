package click

import chisel3._

/**
 * A register which uses a custom clock signal instead of the global clock signal.
 * Used such that we don't have to use withClock{...} every time we want a custom clock signal,
 * as that doesn't fit as well with the structural design of async. circuits.
 * Has an asynchronous, active-high reset signal
 * @param init The initial value of the register
 */
class CustomClockRegister[T <: Data](init: T) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val in = Input(chiselTypeOf(init))
    val out = Output(chiselTypeOf(init))
  })

  io.out := RegNext(io.in, WireInit(init))
}

/**
 * A 1-bit phase register for use in click elements
 * @param init The initial state of the register
 */
class PhaseRegister(init: Boolean) extends CustomClockRegister(init.B) {

}
