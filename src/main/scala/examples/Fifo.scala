package examples

import chisel3._
import click._

class Fifo(N: Int) extends RawModule {
  val io = IO(new HandshakeIO(UInt(8.W)))

  val stages = for(i <- 0 until N) yield {
    Module(new HandshakeRegister(0.U(8.W)))
  }
  val delays = for(i <- 0 until N-1) yield {
    Module(new DelayElementSim(i+1))
  }
  for(i <- 0 until stages.length - 1) {
    stages(i).io.out <> stages(i+1).io.in
    delays(i).io.reqIn := stages(i).io.out.req
    stages(i+1).io.in.req := delays(i).io.reqOut
    stages(i).io.reset := io.reset
  }
  stages(N-1).io.reset := io.reset
  io.in <> stages(0).io.in
  io.out <> stages(N-1).io.out
}
