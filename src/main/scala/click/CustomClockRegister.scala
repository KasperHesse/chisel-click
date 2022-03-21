package click

import chisel3._

/**
 * A register which uses a custom clock signal instead of the global clock signal
 * @param gen The datatype stored in the register
 * @param init The initial value of the register
 */
class CustomClockRegister[T <: Data](gen: T, init: T) extends Module {
  val io = IO(new Bundle {
    val in = Input(gen)
    val out = Output(gen)
  })

  io.out := RegNext(io.in, init)
}

/**
 * A 1-bit phase register for use in click elements
 * @param init The initial state of the register
 */
class PhaseRegister(init: Boolean) extends CustomClockRegister(Bool(), init.B) {

}
