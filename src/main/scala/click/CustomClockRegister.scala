package click

import chisel3._

/**
 * A register which uses a custom clock signal instead of the global clock signal
 * Has an asynchronous, active-high reset signal
 * @param init The initial value of the register
 */
class CustomClockRegister[T <: Data](init: T) extends RawModule {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(AsyncReset())
    val in = Input(chiselTypeOf(init))
    val out = Output(chiselTypeOf(init))
  })

  io.out := withClockAndReset(io.clock, io.reset) {RegNext(io.in, init)}
}

/**
 * A 1-bit phase register for use in click elements
 * @param init The initial state of the register
 */
class PhaseRegister(init: Boolean) extends CustomClockRegister(init.B) {

}
