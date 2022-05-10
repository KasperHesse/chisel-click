package examples

import chisel3._
import click._

/**
 * An N-stage fifo
 * @param N The number of stages in the fifo
 * @param dataWidth The datatype being passed through the FIFO
 * @param conf
 */
class Fifo[T <: Data](N: Int, dataWidth: Int)(implicit conf: ClickConfig) extends RawModule {
  val io = IO(new HandshakeIO(UInt(dataWidth.W)))

  val stages = for(i <- 0 until N) yield {
    Module(new HandshakeRegister(0.U(dataWidth.W)))
  }
  for(i <- 0 until stages.length - 1) {
    stages(i).io.out <> stages(i+1).io.in
    stages(i+1).io.in.req := simDelay(stages(i).io.out.req, conf.REG_DELAY)
    stages(i).io.reset := io.reset
  }
  stages(N-1).io.reset := io.reset
  io.in <> stages(0).io.in
  stages(0).io.in.req := simDelay(io.in.req, conf.REG_DELAY)
  io.out <> stages(N-1).io.out
}
